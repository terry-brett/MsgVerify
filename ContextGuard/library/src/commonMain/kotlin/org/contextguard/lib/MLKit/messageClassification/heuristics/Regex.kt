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

// Telecom impersonation (narrow, Twitter-labeled patterns)
val _TELECOM_BRAND_RE = Regex("\\b(vodafone|orange|t-?mobile|tmobile|o2)\\b", RegexOption.IGNORE_CASE)
val _UPGRDCENTRE_RE = Regex("\\bupgrdcentre\\b", RegexOption.IGNORE_CASE)
val _TELECOM_ACTION_RE = Regex("\\b(call|dial|text)\\b", RegexOption.IGNORE_CASE)

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
val _FREE_EMAIL = setOf(
    "gmail.com", "googlemail.com", "yahoo.com", "ymail.com", "outlook.com", "hotmail.com", "live.com", "msn.com",
    "aol.com", "icloud.com", "me.com", "mac.com", "proton.me", "protonmail.com", "gmx.com", "mail.com"
)


val URL_REGEX = Regex(
    "(https?://\\S+|www\\.\\S+|\\b(?:bit\\.ly|t\\.co|tinyurl\\.com|goo\\.gl|ow\\.ly|is\\.gd|buff\\.ly|rebrand\\.ly)/\\S+|" +
            "\\b[a-z0-9-]{2,}\\.(?:com|net|org|info|biz|io|co|me|app|xyz|uk|in|ru|de|fr|no|se|dk|fi|nl|au|br|jp|kr|sg|za)\\b[^\\s]*)",
    RegexOption.IGNORE_CASE
)


val PHONE_LIKE = Regex(
    "\\b(?:\\+?\\d[\\d\\s\\-()]{7,}\\d)\\b"
)

val SHORTCODE = Regex(
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

val LOOSE_URL_REGEX = Regex(
    "(" +
            "\\bhttps?://\\S+" +
            "|\\bhttps?/\\S+" +                     // e.g., http/example.com
            "|\\bwww\\.\\S+" +
            "|\\b[a-z0-9-]{2,}\\s*\\.\\s*[a-z]{2,}(?:\\s*\\.\\s*[a-z]{2,})*\\b" +  // e.g., daal.au or a.b.co (with spaces)
            ")",
    RegexOption.IGNORE_CASE
)

val EMAIL_REGEX = Regex(
    "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}",
    RegexOption.IGNORE_CASE
)

val URGENCY_PATTERN by lazy {
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