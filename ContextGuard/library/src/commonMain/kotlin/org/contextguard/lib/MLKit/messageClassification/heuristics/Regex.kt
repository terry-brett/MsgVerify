package org.contextguard.lib.MLKit.messageClassification.heuristics

// Bank/card account restriction cues (SMS)
val _BANK_ACTION_RE = Regex("\\b(blocked|lock(?:ed)?|suspend(?:ed)?|disabled|frozen|restricted|jam)\\b", RegexOption.IGNORE_CASE)
val _BANK_CARD_RE = Regex("\\b(bank|debit|credit|atm)\\b.*\\bcard\\b|\\bcard\\b.*\\b(bank|debit|credit|atm)\\b", RegexOption.IGNORE_CASE)

// Apple ID / iCloud (SMS)
val _APPLE_CLAIM_RE = Regex("\\b(apple\\s*(?:id|1d)|icloud|apple\\s+support)\\b", RegexOption.IGNORE_CASE)
val _APPLE_ACTION_RE = Regex("\\b(unauthori[sz]ed|sign[-\\s]?in|password|login|verify|confirm)\\b", RegexOption.IGNORE_CASE)
val _APPLE_EXPIRE_RE = Regex("\\b(expir\\w*)\\b", RegexOption.IGNORE_CASE)
val _APPLE_DUE_EXPIRE_RE = Regex("due\\s+to.{0,25}expir", RegexOption.IGNORE_CASE)

// Bank of America suspended restore (SMS)
val _BOA_RE = Regex("\\b(bank\\s*of\\s*america|bankofamerica)\\b", RegexOption.IGNORE_CASE)
val _BOA_SUSP_RE = Regex("\\b(suspend(?:ed)?|restricted|limited)\\b", RegexOption.IGNORE_CASE)
val _BOA_LOGIN_RE = Regex("\\b(log\\s*in|login|sign\\s*in)\\b", RegexOption.IGNORE_CASE)
val _BOA_RESTORE_RE = Regex("\\b(restore|reconstruct|regenerate|reactivat\\w*)\\b", RegexOption.IGNORE_CASE)

// Tax agency refund (SMS)
val _TAX_AGENCY_RE = Regex("\\b(hmrc|govuk|gov\\.uk|irs)\\b", RegexOption.IGNORE_CASE)
val _REFUND_RE = Regex("\\b(refund|rebate)\\b", RegexOption.IGNORE_CASE)

// Paytm FASTag (SMS)
val _PAYTM_RE = Regex("\\bpaytm\\b", RegexOption.IGNORE_CASE)
val _PAYTM_CTX_RE = Regex("\\b(received a request|request from you|fastag|payments bank)\\b", RegexOption.IGNORE_CASE)

// Order cancel/delete (SMS)
val _ORDER_CANCEL_RE = Regex("\\b(cancel|delete)\\b.{0,25}\\border\\b", RegexOption.IGNORE_CASE)
val _ORDER_CTA_RE = Regex("\\b(visit|call|chat|contact)\\b", RegexOption.IGNORE_CASE)

// Email (TREC_06) — org claim + account action + sender mismatch
val _EMAIL_ACTION_RE = Regex(
    "\\b(" +
            "(verify|confirm|update|validate|authenticate)\\s+(your\\s+)?(account|profile|billing|payment|information|details)" +
            "|((log\\s*in|login|sign\\s*in)\\s+(to\\s+)?(your\\s+)?(account|profile))" +
            "|((reset|change|update)\\s+(your\\s+)?password)" +
            "|((account|profile)\\s+(locked|suspended|disabled|restricted))" +
            "|(limited\\s+access|security\\s+alert|unusual\\s+activity|unauthori[sz]ed)" +
            ")\\b",
    RegexOption.IGNORE_CASE
)

// Free email domains for sender mismatch
val _FREE_EMAIL_RE = setOf(
    "gmail.com", "googlemail.com", "yahoo.com", "ymail.com", "outlook.com", "hotmail.com", "live.com", "msn.com",
    "aol.com", "icloud.com", "me.com", "mac.com", "proton.me", "protonmail.com", "gmx.com", "mail.com"
)


val _URL_RE = Regex(
    "(https?://\\S+|www\\.\\S+|\\b(?:bit\\.ly|t\\.co|tinyurl\\.com|goo\\.gl|ow\\.ly|is\\.gd|buff\\.ly|rebrand\\.ly)/\\S+|" +
            "\\b[a-z0-9-]{2,}\\.(?:com|net|org|info|biz|io|co|me|app|xyz|uk|in|ru|de|fr|no|se|dk|fi|nl|au|br|jp|kr|sg|za)\\b[^\\s]*)",
    RegexOption.IGNORE_CASE
)


val _PHONE_LIKE_RE = Regex(
    "\\b(?:\\+?\\d[\\d\\s\\-()]{7,}\\d)\\b"
)

val _SHORTCODE_RE = Regex(
    "\\b\\d{5,6}\\b"
)

val _QUOTE_CUTOFF_RE = Regex(
    "(^\\s*-----\\s*original message\\s*-----\\s*$|" +
            "^\\s*begin forwarded message\\s*$|" +
            "^\\s*on .{0,60} wrote:\\s*$|" +
            "^\\s*from:\\s*.+$)",
    setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
)

val _TECH_BULLETIN_RE = Regex(
    "\\b(cve-\\d{4}-\\d+|us-cert|cert\\b|rfc\\s*\\d+|" +
            "vulnerability|exploit|patch|advisory|mailing list|bugtraq)\\b",
    RegexOption.IGNORE_CASE
)

val _LOOSE_URL_RE = Regex(
    "(" +
            "\\bhttps?://\\S+" +
            "|\\bhttps?/\\S+" +                     // e.g., http/example.com
            "|\\bwww\\.\\S+" +
            "|\\b[a-z0-9-]{2,}\\s*\\.\\s*[a-z]{2,}(?:\\s*\\.\\s*[a-z]{2,})*\\b" +  // e.g., daal.au or a.b.co (with spaces)
            ")",
    RegexOption.IGNORE_CASE
)

val _EMAIL_RE = Regex(
    "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}",
    RegexOption.IGNORE_CASE
)

val _URGENCY_PATTERN_RE by lazy {
    val urgencyTerms = listOf(
        "urgent", "immediately", "final notice", "action required",
        "attention", "mandatory", "asap", "verify now", "within \\d+ (hour|min|day)",
        "24 hour", "48 hour", "today", "now"
    )

    val threatTerms = listOf(
        "block(ed)?", "expir(ed|y)", "suspend(ed)?", "restrict(ed)?",
        "deactivate(d)?", "closed", "terminated", "security alert", "unauthorized"
    )

    Regex("\\b(${urgencyTerms.joinToString("|")}|${threatTerms.joinToString("|")})\\b", RegexOption.IGNORE_CASE)
}

val _OPT_OUT_RE = Regex(
    "\\b(unsubscribe|opt\\s*out|reply\\s*stop|text\\s*stop|send\\s*stop|stop\\s+to)\\b",
    RegexOption.IGNORE_CASE
)

val _OTP_RE = Regex(
    "\\b(your\\s+otp|your\\s+verification\\s+code|one[-\\s]?time\\s+pass)\\b",
    RegexOption.IGNORE_CASE
)

val _PROMO_RE = Regex(
    "\\b(discount|offer|offers|deal|deals|sale|limited|promo|promotion|promotions|voucher|vouchers|coupon|coupons|cashback|half price|free)\\b",
    RegexOption.IGNORE_CASE
)

val _TELECOM_RE = Regex(
    "\\b(line rental|free texts?|text messages|minutes|min|handset|tariff|contract)\\b",
    RegexOption.IGNORE_CASE
)

val _CONTEST_RE = Regex(
    "\\b(free entry|entry|competition|comp\\b|win)\\b",
    RegexOption.IGNORE_CASE
)

val _CTA_RE = Regex(
    "\\b(call|dial|text|reply|click|visit|join|buy|order|subscribe|claim|redeem|get)\\b",
    RegexOption.IGNORE_CASE
)

val _PRIZE_VALUE_RE = Regex(
    "(£|\\$|€|gbp|usd|eur)\\s*\\d+|prize|award|cash|voucher|gift\\s+card|winning",
    RegexOption.IGNORE_CASE
)

val _PRIZE_SCAM_RE = Regex(
    "\\b(you\\s+have\\s+won|you've\\s+won|you\\s+are\\s+a\\s+winner|won\\s+a|" +
            "selected\\s+to\\s+receive|you\\s+have\\s+been\\s+selected|you\\s+are\\s+awarded|" +
            "congratulations.*won|awarded\\s+a)\\b",
    RegexOption.IGNORE_CASE
)

val _ORG_CLAIM_RE = Regex(
    "\\b(bank|treasury|ministry|government|customs|immigration|revenue|tax|" +
            "paypal|amazon|apple|microsoft|google|bank\\s*of\\s*america|bankofamerica|" +
            "hmrc|govuk|gov\\.uk|irs|dhl|ups|fedex|usps|royal\\s*mail)\\b",
    RegexOption.IGNORE_CASE
)

val _CRED_REQUEST_RE = Regex(
    "\\b(login|log\\s*in|sign\\s*in|username|user\\s*id|password|passcode|pin\\s*code|pin\\b|" +
            "otp|verification\\s+code|security\\s+code|authenticate|apple\\s+id)\\b.{0,60}" +
            "\\b(verify|confirm|send|provide|enter|update)\\b",
    RegexOption.IGNORE_CASE
)

val _CRED_REQUEST_V2_RE = Regex(
    "\\b(verify|confirm|send|provide|enter|update).{0,60}" +
            "\\b(login|log\\s*in|sign\\s*in|username|user\\s*id|password|passcode|pin\\s*code|pin\\b|" +
            "otp|verification\\s+code|security\\s+code|authenticate|apple\\s+id)\\b",
    RegexOption.IGNORE_CASE
)

val _ACTION_RE = Regex(
    "\\b(verify|confirm|update|contact|reset|authenticate|login|log in|sign in)\\b",
    RegexOption.IGNORE_CASE
)
val _NOUN_RE = Regex(
    "\\b(password|passcode|pin|expired|credentials|account|login|username|security code|verification code|otp|2fa)\\b",
    RegexOption.IGNORE_CASE
)

val _PRIZE_SCAM_KEYWORDS_RE = Regex("\\b(won|winner|winning|prize|award|claim|collect|receive|redeem|gain(?:ed)?)\\b", RegexOption.IGNORE_CASE)

val _URGENT_INFO_REQUEST_RE = Regex(
    "\\b(verify|confirm|update|validate|provide|send).{0,50}" +
            "\\b(account\\s+number|identity|personal\\s+information|contact\\s+details|billing\\s+information)\\b",
    RegexOption.IGNORE_CASE
)