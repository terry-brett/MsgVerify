package org.contextguard.lib.mlkit.messageClassification.heuristics

import org.contextguard.lib.mlkit.messageClassification.heuristics.constants.*

private const val DECISION_THRESHOLD = 3

fun String.normalise(): String {
    return this
        .lowercase()
        .replace(whitespaceRegex, " ")
        .trim()
}

fun String.extractEmailAddress(): String? {
    val match = emailExtractRegex.find(this)
    return match?.groupValues?.get(1)?.lowercase()
}

fun String?.hasAdultContentPatterns(): Boolean {
    if (this.isNullOrBlank()) return false
    return adultContentRegex.containsMatchIn(this)
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
    val ctaLocalRegex = Regex("\\b(click|visit|reply|enter|submit)\\b", RegexOption.IGNORE_CASE)

    val request =
        politeRequestRegex.containsMatchIn(msg) ||
                ctaLocalRegex.containsMatchIn(msg) ||
                msg.containsUrl()

    return actionRegex.containsMatchIn(msg) &&
            nounRegex.containsMatchIn(msg) &&
            request
}

fun String?.hasUrgencyOrIntimidationPatterns(): Boolean {
    if (this.isNullOrBlank()) return false

    val msg = this.lowercase()

    val hasKeywordMatch = urgencyPatternRegex.containsMatchIn(msg)

    val hasImpliedThreat = msg.contains("blocked") &&
            (msg.contains("not updated") || msg.contains("contact"))

    return hasKeywordMatch || hasImpliedThreat
}

fun String.hasTooGoodToBeTruePatterns(): Boolean {
    val msg = this.normalise()

    val contact =
        phoneLikeRegex.containsMatchIn(this) ||
                shortcodeRegex.containsMatchIn(this) ||
                this.containsUrl()

    if (!contact) return false

    // Pattern A: urgent + prize/win + explicit currency symbol
    if (
        urgentWordRegex.containsMatchIn(msg) &&
        prizeWinWordRegex.containsMatchIn(msg) &&
        currencySymbolRegex.containsMatchIn(this)
    ) {
        return true
    }

    // Pattern B: selected + prize/award + money (symbol or "5000 pounds", etc.)
    val money =
        currencySymbolRegex.containsMatchIn(this) ||
                moneyAmountRegex.containsMatchIn(msg)

    if (
        selectedWordRegex.containsMatchIn(msg) &&
        money &&
        receiveAwardRegex.containsMatchIn(msg)
    ) {
        return true
    }

    return false
}

fun String.hasMarketingPatterns(): Boolean {
    val msg = this.normalise()

    // Strong opt-out / compliance (common in marketing SMS)
    if (optOutRegex.containsMatchIn(msg)) {
        return true
    }

    // Suppress pure OTP / verification messages (transactional)
    if (otpRegex.containsMatchIn(msg)) {
        return false
    }

    val promo = promoRegex.containsMatchIn(msg)
    val telecom = telecomRegex.containsMatchIn(msg)
    val contest = contestRegex.containsMatchIn(msg)

    val cta = ctaRegex.containsMatchIn(msg)
    val contact = phoneLikeRegex.containsMatchIn(this) ||
            shortcodeRegex.containsMatchIn(this) ||
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

    if (techBulletinRegex.containsMatchIn(text)) return false

    val msg = text.normalise()
    val hasLink = text.containsUrlLoose()
    val hasPhone = phoneLikeRegex.containsMatchIn(text) || shortcodeRegex.containsMatchIn(text)
    val hasEmailInBody = emailRegex.containsMatchIn(text)
    val hasContactWord = Regex("\\b(call|dial|contact|reply|text|email|chat)\\b")
        .containsMatchIn(msg)
    val contact = hasLink || hasPhone || hasEmailInBody || hasContactWord

    if (bankActionRegex.containsMatchIn(text) &&
        bankCardRegex.containsMatchIn(text) &&
        hasPhone && contact
    ) {
        if (!bankNoticedDebitRegex.containsMatchIn(text)) {
            return true
        }
    }

    if (appleClaimRegex.containsMatchIn(text)) {
        val appleAction = appleActionRegex.containsMatchIn(text) ||
                appleExpireRegex.containsMatchIn(text) ||
                appleDueExpireRegex.containsMatchIn(text)
        if (appleAction && (contact || hasLink)) {
            if (text.containsUrl() && sender == null) {
                if (!urlRegex.containsMatchIn(text) && hasLink) return true
            } else {
                return true
            }
        }
    }

    if (boaRegex.containsMatchIn(text) &&
        boaSuspRegex.containsMatchIn(text) &&
        boaLoginRegex.containsMatchIn(text) &&
        boaRestoreRegex.containsMatchIn(text) &&
        hasLink
    ) return true

    if (taxAgencyRegex.containsMatchIn(text) &&
        refundRegex.containsMatchIn(text) &&
        hasLink
    ) return true

    if (paytmRegex.containsMatchIn(text) &&
        paytmCtxRegex.containsMatchIn(text) &&
        hasLink
    ) return true

    if (orderCancelRegex.containsMatchIn(text) &&
        hasLink &&
        orderCtaRegex.containsMatchIn(text)
    ) return true

    if (telecomBrandRegex.containsMatchIn(msg) && contact) return true

    val isPrizeScamMatch = prizeScamRegex.containsMatchIn(msg)
    val containsPrizeValue = prizeValueRegex.containsMatchIn(msg)

    if (isPrizeScamMatch && containsPrizeValue && contact) return true

    if (sender != null) {
        val sdom = (sender.extractEmailDomain() ?: "").lowercase()
        if (sdom.endsWith(".gov") || sdom.endsWith(".gov.uk") ||
            sdom.endsWith(".mil") || sdom.endsWith(".edu")
        ) {
            if (!emailActionRegex.containsMatchIn(raw)) return false
        }

        if (orgClaimRegex.containsMatchIn(raw)) {
            if (contact && emailActionRegex.containsMatchIn(raw)) return true
        }

        if (advanceFeeFormalRegex.containsMatchIn(raw)) {
            if (contact && (sdom in _FREE_EMAIL_RE || sdom.isEmpty())) return true
        }
    }

    return false
}

fun String?.extractPrimaryEmailText(): String {
    var t = this ?: ""

    // Stop at common quote markers
    val m = quoteCutoffRegex.find(t)
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
        if (urlRegex.containsMatchIn(token) || looseUrlRegex.containsMatchIn(token)) {
            return true
        }
    }
    return false
}

fun String.containsUrl(): Boolean {
    for (token in this.split(" ")) {
        if (urlRegex.containsMatchIn(token)) {
            return true
        }
    }
    return false
}

fun String?.asksForFinancialOrPersonalInfo(): Boolean {
    if (this.isNullOrBlank()) return false

    val msg = this.normalise()

    // Exclusions: Credential verification
    val credentialRequest = credRequestRegex.containsMatchIn(msg) || credRequestV2Regex.containsMatchIn(msg)

    if (credentialRequest) return false

    // Exclusions: vague security update
    val vagueSecurity = vagueSocialRegex.containsMatchIn(msg) &&
            !sensitiveFieldsRegex.containsMatchIn(msg)

    if (vagueSecurity) return false

    // Exclusions: Telecom promotional offers
    val telecomPromo = telecomPromoRegex.containsMatchIn(msg) &&
            promoRewardRegex.containsMatchIn(msg)

    if (telecomPromo) return false

    // Exclusions: casual dating/flirting with example
    val casualDatingWithExample = casualDatingRegex.containsMatchIn(msg)

    if (casualDatingWithExample) return false

    // === Composite scoring ===
    var score = 0

    // Pattern 1: Sensitive financial info
    if (sensitiveFinInfoRegex.containsMatchIn(this) &&
        provideVerbRegex.containsMatchIn(msg)
    ) {
        score += 6
    }

    // Pattern 2: Multiple personal info fields
    val hasName = nameFieldRegex.containsMatchIn(msg)
    val hasAddress = addressFieldRegex.containsMatchIn(msg)
    val hasPhone = phoneFieldRegex.containsMatchIn(msg)
    val hasAgeDob = ageDobFieldRegex.containsMatchIn(msg)
    val hasPostcode = postcodeFieldRegex.containsMatchIn(msg)

    val fieldCount = listOf(hasName, hasAddress, hasPhone, hasAgeDob, hasPostcode).count { it }

    val hasRequestVerb = requestVerbRegex.containsMatchIn(msg)

    if (fieldCount >= 3 && hasRequestVerb) score += 6
    else if (fieldCount == 2 && hasRequestVerb &&
        ((hasName && hasAddress) || (hasAddress && hasAgeDob))
    ) score += 5

    // Pattern 3: Prize/lottery + multi-field info request
    val prizePattern = prizeScamKeywordsRegex.containsMatchIn(msg)
    val explicitFields = explicitFieldsRegex.containsMatchIn(msg)
    if (prizePattern && explicitFields) score += 6

    // Pattern 4: Urgent request for account info
    val urgentInfoRequest = urgentInfoRequestRegex.containsMatchIn(msg)
    val hasUrgency = urgencyWordRegex.containsMatchIn(msg)
    if (urgentInfoRequest) {
        score += 4
        if (hasUrgency) score += 1
    }

    // Pattern 5: Account access restricted
    val restricted = restrictedAccountRegex.containsMatchIn(msg)
    val specificInfoNeeded = specificInfoNeededRegex.containsMatchIn(msg)
    if (restricted && specificInfoNeeded) score += 4

    // Pattern 6: Direct request for info ASAP
    val directNeed = directNeedRegex.containsMatchIn(msg)
    if (directNeed && fieldCount >= 2) score += 5

    // Pattern 7: Start-of-message request
    val startsWithRequest = startsWithRequestRegex.containsMatchIn(msg)
    if (startsWithRequest && fieldCount >= 2) score += 3

    // Pattern 8: Advance-fee / 419 cues
    val advanceFeeCues = advanceFeeRegex.containsMatchIn(msg)
    val financialTransfer = financialTransferRegex.containsMatchIn(msg)
    val contactFinancial = contactFinancialRegex.containsMatchIn(msg) || financialContactRegex.containsMatchIn(msg)

    if (advanceFeeCues || (financialTransfer && contactFinancial)) score += 3

    // Pattern 9: Need more info in account/billing context
    val needMoreInfo = needMoreInfoRegex.containsMatchIn(msg)
    val accountContext = accountContextRegex.containsMatchIn(msg)
    if (needMoreInfo && accountContext) score += 3

    // Decision threshold
    return score >= DECISION_THRESHOLD
}
