data class Brand(
    val name: String,
    val abbr: String
)

data class Brands(
    val phishingTargets: List<Brand>
)

val BRANDS = Brands(
    phishingTargets = listOf(
        Brand("DHL", "DHL"),
        Brand("Allied Bank Limited", "ABL"),
        Brand("Santander UK", "SAN"),
        Brand("Coinbase", "COIN"),
        Brand("East Japan Railway Company", "JR East"),
        Brand("Steam", "STM"),
        Brand("Bank Millennium", "MIL"),
        Brand("Virustotal", "VT"),
        Brand("DocuSign", "DOCU"),
        Brand("Apple", "AAPL"),
        Brand("Nationwide", "NBS"),
        Brand("Banco Bilbao Vizcaya Argentaria", "BBVA"),
        Brand("WeTransfer", "WT"),
        Brand("Adobe", "ADBE"),
        Brand("Das kann Bank", "DKB"),
        Brand("Orange", "ORA"),
        Brand("Regions Bank", "RF"),
        Brand("Allegro", "ALE"),
        Brand("Royal Bank of Canada", "RY"),
        Brand("AEON Card", "AEON"),
        Brand("Microsoft", "MSFT"),
        Brand("The Brazilian Development Bank", "BNDES"),
        Brand("Caixa", "CEF"),
        Brand("Dropbox", "DBX"),
        Brand("Comcast", "CMCSA"),
        Brand("Wachovia", "WB"),
        Brand("Mercari", "MERC"),
        Brand("Other", "MISC"),
        Brand("HSBC Group", "HSBC"),
        Brand("Wells Fargo", "WFC"),
        Brand("Her Majesty's Revenue and Customs", "HMRC"),
        Brand("US Bank", "USB"),
        Brand("PayPay Bank", "PPB"),
        Brand("Aetna Health Plans & Dental Coverage", "AET"),
        Brand("Telefónica UK", "O2"),
        Brand("Visa", "V"),
        Brand("Banco De Brasil", "BBAS"),
        Brand("UniCredit", "UCG"),
        Brand("PKO Polish Bank", "PKO"),
        Brand("Bradesco", "BBD"),
        Brand("AT&T", "T"),
        Brand("Barclays Bank PLC", "BARC"),
        Brand("Co-operative Bank", "COOP"),
        Brand("Huntington National Bank", "HBAN"),
        Brand("ABN AMRO Bank", "ABN"),
        Brand("Internal Revenue Service", "IRS"),
        Brand("RuneScape", "RS"),
        Brand("Sumitomo Mitsui Banking Corporation", "SMBC"),
        Brand("Banco Santander, S.A.", "SAN"),
        Brand("Navy Federal Credit Union", "NFCU"),
        Brand("Netflix", "NFLX"),
        Brand("JPMorgan Chase and Co.", "JPM"),
        Brand("Bank of America Corporation", "BAC"),
        Brand("Raiffeisen Bank", "RBI"),
        Brand("Yahoo", "YHOO"),
        Brand("Accurint", "ACC"),
        Brand("Rakuten", "RKUNY"),
        Brand("British Telecom", "BT"),
        Brand("Hotmail", "MSFT"),
        Brand("AOL", "AOL"),
        Brand("Google", "GOOGL"),
        Brand("Intesa Sanpaolo", "ISP"),
        Brand("Volksbanken Raiffeisenbanken", "VR"),
        Brand("TSB", "TSB"),
        Brand("Nets", "NETS"),
        Brand("Binance", "BNB"),
        Brand("Itau", "ITUB"),
        Brand("Mastercard", "MA"),
        Brand("American Express", "AMEX"),
        Brand("ABSA Bank", "ABSA"),
        Brand("Westpac", "WBC"),
        Brand("PayPal", "PYPL"),
        Brand("Facebook", "META"),
        Brand("Swiss Post", "SWP"),
        Brand("Abonné Free Mobile", "FREE"),
        Brand("Nubank", "NU"),
        Brand("eBay, Inc.", "EBAY"),
        Brand("Nordea Bank", "NDA"),
        Brand("Development Bank of Singapore", "DBS"),
        Brand("Rackspace", "RAX"),
        Brand("Scotiabank", "BNS"),
        Brand("Interactive Brokers", "IBKR"),
        Brand("ING Direct", "ING"),
        Brand("Amazon.com", "AMZN"),
        Brand("Optus", "OPT"),
        Brand("Sulake Corporation", "SUL"),
        Brand("Instagram", "IG"),
        Brand("Capital One", "COF"),
        Brand("Capitec Bank", "CPI")
    )
)


fun normalise(message: String): String {
    return message
        .lowercase()
        .replace(Regex("\\s+"), " ")
        .trim()
}

fun extractEmailAddress(sender: String): String? {
    val regex = Regex(
        "([A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,})",
        RegexOption.IGNORE_CASE
    )

    val match = regex.find(sender)
    return match?.groupValues?.get(1)?.lowercase()
}

fun hasAdultContentPatterns(message: String?): Boolean {
    // High-precision adult-content detector (tuned to Dataset_10191 annotations).
    val msg = normalise(message ?: "")
    val adultRegex = Regex(
        "\\b(xxx|porn|hardcore|nude|nudes|sexcam|camgirl|escort|filthy)\\b",
        RegexOption.IGNORE_CASE
    )
    return adultRegex.containsMatchIn(msg)
}


fun extractEmailDomain(email: String): String? {
    if (email.isBlank() or !email.contains("@")) return null
    return email.split("@")[1].lowercase()
}

fun checkImpersonation(message: String?, sender: String? = null): Boolean {
    val raw = message ?: ""
    println(raw)
    val isEmailLike = sender != null || raw.length > 800
    val text = if (isEmailLike) extractPrimaryEmailText(raw) else raw

    if (_TECH_BULLETIN_RE.containsMatchIn(text)) return false

    val msg = normalise(text)
    val hasLink = containsUrlLoose(text)
    val hasPhone = PHONE_LIKE.containsMatchIn(text) || SHORTCODE.containsMatchIn(text)
    val hasEmailInBody = EMAIL_REGEX.containsMatchIn(text)
    val hasContactWord = Regex("\\b(call|dial|contact|reply|text|email|chat)\\b")
        .containsMatchIn(msg)
    val contact = hasLink || hasPhone || hasEmailInBody || hasContactWord

    if (_BANK_ACTION_RE.containsMatchIn(text) &&
        _BANK_CARD_RE.containsMatchIn(text) &&
        hasPhone && contact
    ) {
        if (!Regex("\\bhas noticed\\b.*\\bdebit card\\b.*\\bused\\b", RegexOption.IGNORE_CASE)
                .containsMatchIn(text)) {
            return true
        }
    }

    if (_APPLE_CLAIM_RE.containsMatchIn(text)) {
        val appleAction = _APPLE_ACTION_RE.containsMatchIn(text) ||
                _APPLE_EXPIRE_RE.containsMatchIn(text) ||
                _APPLE_DUE_EXPIRE_RE.containsMatchIn(text)
        if (appleAction && (contact || hasLink)) {
            if (containsUrl(text) && sender == null) {
                if (!URL_REGEX.containsMatchIn(text) && hasLink) return true
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
            .containsMatchIn(msg) && contact) return true

    val prizeScam = Regex(
        "\\b(you\\s+have\\s+won|you've\\s+won|you\\s+are\\s+a\\s+winner|won\\s+a|" +
                "selected\\s+to\\s+receive|you\\s+have\\s+been\\s+selected|you\\s+are\\s+awarded|" +
                "congratulations.*won|awarded\\s+a)\\b",
        RegexOption.IGNORE_CASE
    ).containsMatchIn(msg)

    val prizeValue = Regex(
        "(£|\\$|€|gbp|usd|eur)\\s*\\d+|prize|award|cash|voucher|gift\\s+card|winning",
        RegexOption.IGNORE_CASE
    ).containsMatchIn(msg)

    if (prizeScam && prizeValue && contact) return true

    if (sender != null) {
        val sdom = (extractEmailDomain(sender) ?: "").lowercase()
        if (sdom.endsWith(".gov") || sdom.endsWith(".gov.uk") ||
            sdom.endsWith(".mil") || sdom.endsWith(".edu")
        ) {
            if (!_EMAIL_ACTION_RE.containsMatchIn(raw)) return false
        }

        val orgClaimRe = Regex(
            "\\b(bank|treasury|ministry|government|customs|immigration|revenue|tax|" +
                    "paypal|amazon|apple|microsoft|google|bank\\s*of\\s*america|bankofamerica|" +
                    "hmrc|govuk|gov\\.uk|irs|dhl|ups|fedex|usps|royal\\s*mail)\\b",
            RegexOption.IGNORE_CASE
        )

        if (orgClaimRe.containsMatchIn(raw)) {
            if (contact && _EMAIL_ACTION_RE.containsMatchIn(raw)) return true
        }

        if (Regex(
                "\\b(from\\s+the\\s+desk\\s+of|office\\s+of|attn\\b|attention\\b|" +
                        "director|chairman|vice\\s+president|ambassador|attorney|barrister)\\b",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(raw)
        ) {
            if (contact && (sdom in _FREE_EMAIL || sdom.isEmpty())) return true
        }
    }

    return false
}

fun extractPrimaryEmailText(text: String?): String {
    var t = text ?: ""

    // Stop at common quote markers
    val m = _QUOTE_CUTOFF_RE.find(t)
    if (m != null) {
        t = t.substring(0, m.range.first)
    }

    // Drop quoted lines starting with ">"
    val lines = t.lines().filter { !it.trimStart().startsWith(">") }

    return lines.joinToString("\n").trim()
}


fun containsUrlLoose(message: String?): Boolean{
    val text = message ?: ""
    return (URL_REGEX.matches(text) or LOOSE_URL_REGEX.matches(text))
}

fun containsUrl(message: String) : Boolean {
    for (token in message.split(" ")) {
        if (URL_REGEX.matches(token)) {
            return true
        }
    }
    return false
}

