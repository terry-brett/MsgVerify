package org.contextguard.lib.MLKit.messageClassification.heuristics

fun String.normalise(): String {
    return this
        .lowercase()
        .replace(Regex("\\s+"), " ")
        .trim()
}

fun String.extractEmailAddress(): String? {
    val regex = Regex(
        "([A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,})",
        RegexOption.IGNORE_CASE
    )

    val match = regex.find(this)
    return match?.groupValues?.get(1)?.lowercase()
}

fun String?.hasAdultContentPatterns(): Boolean {
    if (this.isNullOrBlank()) return false

    val adultTerms = listOf(
        "xxx", "p[o0]rn", "nude", "nudes", "c[u0]m", "s[e3]x",
        "sexcam", "camgirl", "escort", "filthy", "hardcore", "cum",
        "sexy", "laid"
    )

    val slangTerms = listOf(
        "shag", "hookup", "flirt", "horny", "hot\\s+pics",
        "rude\\s+chat", "private\\s+line", "gettin\\s+it", "dirty"
    )

    val patternString = "\\b(" + (adultTerms + slangTerms).joinToString("|") + ")\\b"

    val adultRegex = Regex(patternString, RegexOption.IGNORE_CASE)

    return adultRegex.containsMatchIn(this)
}

fun String?.hasCredentialVerificationPatterns(): Boolean {
    val msg = (this ?: "").normalise()

    // Suppress normal OTP notifications
    val otpNoticeRegex = Regex(
        "\\byour\\s+(otp|verification\\s+code)\\s+is\\b",
        RegexOption.IGNORE_CASE
    )
    if (otpNoticeRegex.containsMatchIn(msg)) {
        return false
    }

    val politeRequestRegex = Regex("\\b(please|kindly)\\b", RegexOption.IGNORE_CASE)
    val ctaRegex = Regex("\\b(click|visit|reply|enter|submit)\\b", RegexOption.IGNORE_CASE)

    val request =
        politeRequestRegex.containsMatchIn(msg) ||
                ctaRegex.containsMatchIn(msg) ||
                msg.containsUrl()

    return _ACTION_RE.containsMatchIn(msg) &&
            _NOUN_RE.containsMatchIn(msg) &&
            request
}

fun String?.hasUrgencyOrIntimidationPatterns(): Boolean {
    if (this.isNullOrBlank()) return false

    val msg = this.lowercase()

    val hasKeywordMatch = _URGENCY_PATTERN_RE.containsMatchIn(msg)

    val hasImpliedThreat = msg.contains("blocked") &&
            (msg.contains("not updated") || msg.contains("contact"))

    return hasKeywordMatch || hasImpliedThreat
}

fun String.hasTooGoodToBeTruePatterns(): Boolean {
    val msg = this.normalise()

    val contact =
        _PHONE_LIKE_RE.containsMatchIn(this) ||
                _SHORTCODE_RE.containsMatchIn(this) ||
                this.containsUrl()

    if (!contact) return false

    // Pattern A: urgent + prize/win + explicit currency symbol
    if (
        Regex("\\burgent\\b", RegexOption.IGNORE_CASE).containsMatchIn(msg) &&
        Regex("\\b(prize|award|won|winner)\\b", RegexOption.IGNORE_CASE).containsMatchIn(msg) &&
        Regex("[£$€]").containsMatchIn(this)
    ) {
        return true
    }

    // Pattern B: selected + prize/award + money (symbol or "5000 pounds", etc.)
    val money =
        Regex("[£$€]").containsMatchIn(this) ||
                Regex(
                    "\\b\\d{3,}\\s*(pounds|gbp|usd|eur|inr|nok|sek|dkk)\\b",
                    RegexOption.IGNORE_CASE
                ).containsMatchIn(msg)

    if (
        Regex("\\bselected\\b", RegexOption.IGNORE_CASE).containsMatchIn(msg) &&
        money &&
        Regex("\\b(receive|award|prize)\\b", RegexOption.IGNORE_CASE).containsMatchIn(msg)
    ) {
        return true
    }

    return false
}

fun String.hasMarketingPatterns(): Boolean {
    val msg = this.normalise()

    // Strong opt-out / compliance (common in marketing SMS)
    if (_OPT_OUT_RE.containsMatchIn(msg)) {
        return true
    }

    // Suppress pure OTP / verification messages (transactional)
    if (_OTP_RE.containsMatchIn(msg)) {
        return false
    }

    val promo = _PROMO_RE.containsMatchIn(msg)
    val telecom = _TELECOM_RE.containsMatchIn(msg)
    val contest = _CONTEST_RE.containsMatchIn(msg)

    val cta = _CTA_RE.containsMatchIn(msg)
    val contact = _PHONE_LIKE_RE.containsMatchIn(this) ||
            _SHORTCODE_RE.containsMatchIn(this) ||
            this.containsUrl()

    return (promo || telecom || contest) && (cta || contact)
}

fun String.extractEmailDomain(): String? {
    if (this.isBlank() || !this.contains("@")) return null
    return this.substringAfterLast("@").lowercase()
}

fun String?.domainLooksLikeBrand(brandText: String?): Boolean {
    val domain = this
    if (domain.isNullOrBlank() || brandText.isNullOrBlank()) return false

    val d = domain.lowercase().replace(".", "")

    val stopWords = setOf(
        "the", "and", "of", "inc", "corp", "corporation",
        "ltd", "limited", "llc", "plc"
    )

    val tokens = brandText
        .lowercase()
        .split(Regex("[\\s,&()\\-]+"))
        .filter { it.isNotBlank() && it !in stopWords && it.length >= 3 }

    return tokens.any { token ->
        token.replace(".", "") in d
    }
}

fun String?.checkImpersonation(sender: String? = null): Boolean {
    val raw = this ?: ""
    val isEmailLike = sender != null || raw.length > 800
    val text = if (isEmailLike) raw.extractPrimaryEmailText() else raw

    if (_TECH_BULLETIN_RE.containsMatchIn(text)) return false

    val msg = text.normalise()
    val hasLink = text.containsUrlLoose()
    val hasPhone = _PHONE_LIKE_RE.containsMatchIn(text) || _SHORTCODE_RE.containsMatchIn(text)
    val hasEmailInBody = _EMAIL_RE.containsMatchIn(text)
    val hasContactWord = Regex("\\b(call|dial|contact|reply|text|email|chat)\\b")
        .containsMatchIn(msg)
    val contact = hasLink || hasPhone || hasEmailInBody || hasContactWord

    if (_BANK_ACTION_RE.containsMatchIn(text) &&
        _BANK_CARD_RE.containsMatchIn(text) &&
        hasPhone && contact
    ) {
        if (!Regex("\\bhas noticed\\b.*\\bdebit card\\b.*\\bused\\b", RegexOption.IGNORE_CASE)
                .containsMatchIn(text)
        ) {
            return true
        }
    }

    if (_APPLE_CLAIM_RE.containsMatchIn(text)) {
        val appleAction = _APPLE_ACTION_RE.containsMatchIn(text) ||
                _APPLE_EXPIRE_RE.containsMatchIn(text) ||
                _APPLE_DUE_EXPIRE_RE.containsMatchIn(text)
        if (appleAction && (contact || hasLink)) {
            if (text.containsUrl() && sender == null) {
                if (!_URL_RE.containsMatchIn(text) && hasLink) return true
            } else {
                return true
            }
        }
    }

    if (_BOA_RE.containsMatchIn(text) &&
        _BOA_SUSP_RE.containsMatchIn(text) &&
        _BOA_LOGIN_RE.containsMatchIn(text) &&
        _BOA_RESTORE_RE.containsMatchIn(text) &&
        hasLink
    ) return true

    if (_TAX_AGENCY_RE.containsMatchIn(text) &&
        _REFUND_RE.containsMatchIn(text) &&
        hasLink
    ) return true

    if (_PAYTM_RE.containsMatchIn(text) &&
        _PAYTM_CTX_RE.containsMatchIn(text) &&
        hasLink
    ) return true

    if (_ORDER_CANCEL_RE.containsMatchIn(text) &&
        hasLink &&
        _ORDER_CTA_RE.containsMatchIn(text)
    ) return true

    if (Regex("\\b(orange|vodafone|tmobile|t-mobile|o2)\\s+(customer|subscriber|user)", RegexOption.IGNORE_CASE)
            .containsMatchIn(msg) && contact
    ) return true

    val isPrizeScamMatch = _PRIZE_SCAM_RE.containsMatchIn(msg)
    val containsPrizeValue = _PRIZE_VALUE_RE.containsMatchIn(msg)

    if (isPrizeScamMatch && containsPrizeValue && contact) return true

    if (sender != null) {
        val sdom = (sender.extractEmailDomain() ?: "").lowercase()
        if (sdom.endsWith(".gov") || sdom.endsWith(".gov.uk") ||
            sdom.endsWith(".mil") || sdom.endsWith(".edu")
        ) {
            if (!_EMAIL_ACTION_RE.containsMatchIn(raw)) return false
        }

        if (_ORG_CLAIM_RE.containsMatchIn(raw)) {
            if (contact && _EMAIL_ACTION_RE.containsMatchIn(raw)) return true
        }

        if (Regex(
                "\\b(from\\s+the\\s+desk\\s+of|office\\s+of|attn\\b|attention\\b|" +
                        "director|chairman|vice\\s+president|ambassador|attorney|barrister)\\b",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(raw)
        ) {
            if (contact && (sdom in _FREE_EMAIL_RE || sdom.isEmpty())) return true
        }
    }

    return false
}

fun String?.extractPrimaryEmailText(): String {
    var t = this ?: ""

    // Stop at common quote markers
    val m = _QUOTE_CUTOFF_RE.find(t)
    if (m != null) {
        t = t.substring(0, m.range.first)
    }

    // Drop quoted lines starting with ">"
    val lines = t.lines().filter { !it.trimStart().startsWith(">") }

    return lines.joinToString("\n").trim()
}

fun String?.containsUrlLoose(): Boolean {
    val text = this ?: ""
    for (token in text.split(" ")) {
        if (_URL_RE.containsMatchIn(token) || _LOOSE_URL_RE.containsMatchIn(token)) {
            return true
        }
    }
    return false
}

fun String.containsUrl(): Boolean {
    for (token in this.split(" ")) {
        if (_URL_RE.containsMatchIn(token)) {
            return true
        }
    }
    return false
}

fun String?.asksForFinancialOrPersonalInfo(): Boolean {
    private const val DECISION_THRESHOLD = 3
    if (this.isNullOrBlank()) return false

    val msg = this.normalise()

    // Exclusions: Credential verification
    val credentialRequest = _CRED_REQUEST_RE.containsMatchIn(msg) || _CRED_REQUEST_V2_RE.containsMatchIn(msg)

    if (credentialRequest) return false

    // Exclusions: vague security update
    val vagueSecurity = Regex("\\bupdate\\s+(your\\s+)?security\\s+details\\b", RegexOption.IGNORE_CASE)
        .containsMatchIn(msg) &&
            !Regex(
                "\\b(name|address|phone|mobile|postcode|account\\s+number|credit\\s+card|ssn)\\b",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(msg)

    if (vagueSecurity) return false

    // Exclusions: Telecom promotional offers
    val telecomPromo = Regex("\\b(o2|orange|vodafone|t-?mobile|three|ee|verizon|at&t|sprint)\\b", RegexOption.IGNORE_CASE)
        .containsMatchIn(msg) &&
            Regex("\\b(free\\s+call\\s+credit|free\\s+credit|reward|points|cashback|loyalty)\\b", RegexOption.IGNORE_CASE)
                .containsMatchIn(msg)

    if (telecomPromo) return false

    // Exclusions: casual dating/flirting with example
    val casualDatingWithExample = Regex(
        "\\b(meet\\s+someone|find\\s+a\\s+date|flirt).{0,60}\\b(eg|e\\.g\\.|example)\\s+\\w+\\s+\\d+",
        RegexOption.IGNORE_CASE
    ).containsMatchIn(msg)

    if (casualDatingWithExample) return false

    // === Composite scoring ===
    var score = 0

    // Pattern 1: Sensitive financial info
    val sensitiveFin = Regex(
        "\\b(account\\s+number|credit\\s+card|debit\\s+card|cvv|cvc|ssn|social\\s+security|" +
                "sort\\s+code|routing\\s+number|iban|bank\\s+details|credit\\s+card\\s+number)\\b",
        RegexOption.IGNORE_CASE
    )
    if (sensitiveFin.containsMatchIn(this) &&
        Regex("\\b(provide|send|enter|verify|confirm|update|share|give|submit)\\b", RegexOption.IGNORE_CASE)
            .containsMatchIn(msg)
    ) {
        score += 6
    }

    // Pattern 2: Multiple personal info fields
    val hasName = Regex("\\b(your\\s+)?name\\b", RegexOption.IGNORE_CASE).containsMatchIn(msg)
    val hasAddress = Regex("\\b(your\\s+)?(address|house\\s+no)\\b", RegexOption.IGNORE_CASE).containsMatchIn(msg)
    val hasPhone = Regex("\\b(your\\s+)?(phone|mobile|cell|number)\\b", RegexOption.IGNORE_CASE).containsMatchIn(msg)
    val hasAgeDob = Regex("\\b(your\\s+)?(age|dob|date\\s+of\\s+birth|d\\.o\\.b\\.?)\\b", RegexOption.IGNORE_CASE).containsMatchIn(msg)
    val hasPostcode = Regex("\\b(your\\s+)?(postcode|zip\\s*code|postal\\s*code)\\b", RegexOption.IGNORE_CASE).containsMatchIn(msg)

    val fieldCount = listOf(hasName, hasAddress, hasPhone, hasAgeDob, hasPostcode).count { it }

    val hasRequestVerb = Regex("\\b(send|reply|provide|text|forward|email|give|share)\\b", RegexOption.IGNORE_CASE)
        .containsMatchIn(msg)

    if (fieldCount >= 3 && hasRequestVerb) score += 6
    else if (fieldCount == 2 && hasRequestVerb &&
        ((hasName && hasAddress) || (hasAddress && hasAgeDob))
    ) score += 5

    // Pattern 3: Prize/lottery + multi-field info request
    val prizePattern = _PRIZE_SCAM_KEYWORDS_RE.containsMatchIn(msg)
    val explicitFields = Regex(
        "\\b(send|provide|reply|forward).{0,80}(name.{0,40}(address|phone|number|age|postcode)|" +
                "address.{0,40}(name|phone|number|age)|phone.{0,40}(name|address))\\b",
        RegexOption.IGNORE_CASE
    ).containsMatchIn(msg)
    if (prizePattern && explicitFields) score += 6

    // Pattern 4: Urgent request for account info
    val urgentInfoRequest = _URGENT_INFO_REQUEST_RE.containsMatchIn(msg)
    val hasUrgency = Regex("\\b(urgent|immediately|now|today|asap)\\b", RegexOption.IGNORE_CASE).containsMatchIn(msg)
    if (urgentInfoRequest) {
        score += 4
        if (hasUrgency) score += 1
    }

    // Pattern 5: Account access restricted
    val restricted = Regex("\\b(suspended|locked|restricted|limited|blocked|disabled|frozen).{0,80}(account|access|card)\\b", RegexOption.IGNORE_CASE)
        .containsMatchIn(msg)
    val specificInfoNeeded = Regex(
        "\\b(provide|verify|confirm|update).{0,50}" +
                "\\b(account\\s+number|personal\\s+information|identity|contact\\s+details|billing\\s+information|name|address|phone)\\b",
        RegexOption.IGNORE_CASE
    ).containsMatchIn(msg)
    if (restricted && specificInfoNeeded) score += 4

    // Pattern 6: Direct request for info ASAP
    val directNeed = Regex(
        "\\bi\\s+(need|require|want|ask).{0,50}(your|you).{0,50}" +
                "(address|dob|phone|mobile|postcode|details|information)\\b",
        RegexOption.IGNORE_CASE
    ).containsMatchIn(msg)
    if (directNeed && fieldCount >= 2) score += 5

    // Pattern 7: Start-of-message request
    val startsWithRequest = Regex("^(reply|send|text|provide)\\s+(with|us)\\b", RegexOption.IGNORE_CASE)
        .containsMatchIn(msg)
    if (startsWithRequest && fieldCount >= 2) score += 3

    // Pattern 8: Advance-fee / 419 cues
    val advanceFeeCues = Regex(
        "\\b(seek\\s+(your\\s+)?cooperation|can\\s+you\\s+assist|need\\s+(your\\s+)?assistance|" +
                "require\\s+(your\\s+)?cooperation|business\\s+proposal)\\b",
        RegexOption.IGNORE_CASE
    ).containsMatchIn(msg)
    val financialTransfer = Regex(
        "\\b(transfer.{0,30}(fund|money|amount)|fund.{0,30}transfer|" +
                "bank\\s+account.{0,50}(transfer|details)|sum\\s+of.{0,30}(million|thousand|usd|gbp|eur))\\b",
        RegexOption.IGNORE_CASE
    ).containsMatchIn(msg)
    val contactFinancial = Regex(
        "\\b(contact|reply|respond|email\\s+me).{0,50}(fund|transfer|claim|bank|account|payment)\\b",
        RegexOption.IGNORE_CASE
    ).containsMatchIn(msg) || Regex(
        "\\b(fund|transfer|claim|bank|account|payment).{0,50}(contact|reply|respond|email\\s+me)\\b",
        RegexOption.IGNORE_CASE
    ).containsMatchIn(msg)

    if (advanceFeeCues || (financialTransfer && contactFinancial)) score += 3

    // Pattern 9: Need more info in account/billing context
    val needMoreInfo = Regex("\\b(need|require).{0,30}(more|additional).{0,30}(information|details)\\b", RegexOption.IGNORE_CASE)
        .containsMatchIn(msg)
    val accountContext = Regex("\\b(account|billing|payment|profile|service|identity|verification)\\b", RegexOption.IGNORE_CASE)
        .containsMatchIn(msg)
    if (needMoreInfo && accountContext) score += 3

    // Decision threshold
    return score >= DECISION_THRESHOLD
}
