package org.contextguard.lib.mlkit.messageClassification.heuristics.constants

// Bank/card account restriction cues (SMS)
val bankActionRegex by lazy { Regex("\\b(blocked|lock(?:ed)?|suspend(?:ed)?|disabled|frozen|restricted|jam)\\b", RegexOption.IGNORE_CASE) }
val bankCardRegex by lazy { Regex("\\b(bank|debit|credit|atm)\\b.*\\bcard\\b|\\bcard\\b.*\\b(bank|debit|credit|atm)\\b", RegexOption.IGNORE_CASE) }

// Apple ID / iCloud (SMS)
val appleClaimRegex by lazy { Regex("\\b(apple\\s*(?:id|1d)|icloud|apple\\s+support)\\b", RegexOption.IGNORE_CASE) }
val appleActionRegex by lazy { Regex("\\b(unauthori[sz]ed|sign[-\\s]?in|password|login|verify|confirm)\\b", RegexOption.IGNORE_CASE) }
val appleExpireRegex by lazy { Regex("\\b(expir\\w*)\\b", RegexOption.IGNORE_CASE) }
val appleDueExpireRegex by lazy { Regex("due\\s+to.{0,25}expir", RegexOption.IGNORE_CASE) }

// Bank of America suspended restore (SMS)
val boaRegex by lazy { Regex("\\b(bank\\s*of\\s*america|bankofamerica)\\b", RegexOption.IGNORE_CASE) }
val boaSuspRegex by lazy { Regex("\\b(suspend(?:ed)?|restricted|limited)\\b", RegexOption.IGNORE_CASE) }
val boaLoginRegex by lazy { Regex("\\b(log\\s*in|login|sign\\s*in)\\b", RegexOption.IGNORE_CASE) }
val boaRestoreRegex by lazy { Regex("\\b(restore|reconstruct|regenerate|reactivat\\w*)\\b", RegexOption.IGNORE_CASE) }

// Tax agency refund (SMS)
val taxAgencyRegex by lazy { Regex("\\b(hmrc|govuk|gov\\.uk|irs)\\b", RegexOption.IGNORE_CASE) }
val refundRegex by lazy { Regex("\\b(refund|rebate)\\b", RegexOption.IGNORE_CASE) }

// Paytm FASTag (SMS)
val paytmRegex by lazy { Regex("\\bpaytm\\b", RegexOption.IGNORE_CASE) }
val paytmCtxRegex by lazy { Regex("\\b(received a request|request from you|fastag|payments bank)\\b", RegexOption.IGNORE_CASE) }

// Order cancel/delete (SMS)
val orderCancelRegex by lazy { Regex("\\b(cancel|delete)\\b.{0,25}\\border\\b", RegexOption.IGNORE_CASE) }
val orderCtaRegex by lazy { Regex("\\b(visit|call|chat|contact)\\b", RegexOption.IGNORE_CASE) }

// Email (TREC_06) — org claim + account action + sender mismatch
val emailActionRegex by lazy {
    Regex(
        "\\b(" +
                "(verify|confirm|update|validate|authenticate)\\s+(your\\s+)?(account|profile|billing|payment|information|details)" +
                "|((log\\s*in|login|sign\\s*in)\\s+(to\\s+)?(your\\s+)?(account|profile))" +
                "|((reset|change|update)\\s+(your\\s+)?password)" +
                "|((account|profile)\\s+(locked|suspended|disabled|restricted))" +
                "|(limited\\s+access|security\\s+alert|unusual\\s+activity|unauthori[sz]ed)" +
                ")\\b",
        RegexOption.IGNORE_CASE
    )
}

// Free email domains for sender mismatch
val _FREE_EMAIL_RE = setOf(
    "gmail.com", "googlemail.com", "yahoo.com", "ymail.com", "outlook.com", "hotmail.com", "live.com", "msn.com",
    "aol.com", "icloud.com", "me.com", "mac.com", "proton.me", "protonmail.com", "gmx.com", "mail.com"
)


val urlRegex by lazy {
    Regex(
        "(https?://\\S+|www\\.\\S+|\\b(?:bit\\.ly|t\\.co|tinyurl\\.com|goo\\.gl|ow\\.ly|is\\.gd|buff\\.ly|rebrand\\.ly)/\\S+|" +
                "\\b[a-z0-9-]{2,}\\.(?:com|net|org|info|biz|io|co|me|app|xyz|uk|in|ru|de|fr|no|se|dk|fi|nl|au|br|jp|kr|sg|za)\\b[^\\s]*)",
        RegexOption.IGNORE_CASE
    )
}


val phoneLikeRegex by lazy {
    Regex(
        "\\b(?:\\+?\\d[\\d\\s\\-()]{7,}\\d)\\b"
    )
}

val shortcodeRegex by lazy {
    Regex(
        "\\b\\d{5,6}\\b"
    )
}

val quoteCutoffRegex by lazy {
    Regex(
        "(^\\s*-----\\s*original message\\s*-----\\s*$|" +
                "^\\s*begin forwarded message\\s*$|" +
                "^\\s*on .{0,60} wrote:\\s*$|" +
                "^\\s*from:\\s*.+$)",
        setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
    )
}

val techBulletinRegex by lazy {
    Regex(
        "\\b(cve-\\d{4}-\\d+|us-cert|cert\\b|rfc\\s*\\d+|" +
                "vulnerability|exploit|patch|advisory|mailing list|bugtraq)\\b",
        RegexOption.IGNORE_CASE
    )
}

val looseUrlRegex by lazy {
    Regex(
        "(" +
                "\\bhttps?://\\S+" +
                "|\\bhttps?/\\S+" +                     // e.g., http/example.com
                "|\\bwww\\.\\S+" +
                "|\\b[a-z0-9-]{2,}\\s*\\.\\s*[a-z]{2,}(?:\\s*\\.\\s*[a-z]{2,})*\\b" +  // e.g., daal.au or a.b.co (with spaces)
                ")",
        RegexOption.IGNORE_CASE
    )
}

val emailRegex by lazy {
    Regex(
        "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}",
        RegexOption.IGNORE_CASE
    )
}

val urgencyPatternRegex by lazy {
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

val optOutRegex by lazy {
    Regex(
        "\\b(unsubscribe|opt\\s*out|reply\\s*stop|text\\s*stop|send\\s*stop|stop\\s+to)\\b",
        RegexOption.IGNORE_CASE
    )
}

val otpRegex by lazy {
    Regex(
        "\\b(your\\s+otp|your\\s+verification\\s+code|one[-\\s]?time\\s+pass)\\b",
        RegexOption.IGNORE_CASE
    )
}

val promoRegex by lazy {
    Regex(
        "\\b(discount|offer|offers|deal|deals|sale|limited|promo|promotion|promotions|voucher|vouchers|coupon|coupons|cashback|half price|free)\\b",
        RegexOption.IGNORE_CASE
    )
}

val telecomRegex by lazy {
    Regex(
        "\\b(line rental|free texts?|text messages|minutes|min|handset|tariff|contract)\\b",
        RegexOption.IGNORE_CASE
    )
}

val contestRegex by lazy {
    Regex(
        "\\b(free entry|entry|competition|comp\\b|win)\\b",
        RegexOption.IGNORE_CASE
    )
}

val ctaRegex by lazy {
    Regex(
        "\\b(call|dial|text|reply|click|visit|join|buy|order|subscribe|claim|redeem|get)\\b",
        RegexOption.IGNORE_CASE
    )
}

val prizeValueRegex by lazy {
    Regex(
        "(£|\\$|€|gbp|usd|eur)\\s*\\d+|prize|award|cash|voucher|gift\\s+card|winning",
        RegexOption.IGNORE_CASE
    )
}

val prizeScamRegex by lazy {
    Regex(
        "\\b(you\\s+have\\s+won|you've\\s+won|you\\s+are\\s+a\\s+winner|won\\s+a|" +
                "selected\\s+to\\s+receive|you\\s+have\\s+been\\s+selected|you\\s+are\\s+awarded|" +
                "congratulations.*won|awarded\\s+a)\\b",
        RegexOption.IGNORE_CASE
    )
}

val orgClaimRegex by lazy {
    Regex(
        "\\b(bank|treasury|ministry|government|customs|immigration|revenue|tax|" +
                "paypal|amazon|apple|microsoft|google|bank\\s*of\\s*america|bankofamerica|" +
                "hmrc|govuk|gov\\.uk|irs|dhl|ups|fedex|usps|royal\\s*mail)\\b",
        RegexOption.IGNORE_CASE
    )
}

val credRequestRegex by lazy {
    Regex(
        "\\b(login|log\\s*in|sign\\s*in|username|user\\s*id|password|passcode|pin\\s*code|pin\\b|" +
                "otp|verification\\s+code|security\\s+code|authenticate|apple\\s+id)\\b.{0,60}" +
                "\\b(verify|confirm|send|provide|enter|update)\\b",
        RegexOption.IGNORE_CASE
    )
}

val credRequestV2Regex by lazy {
    Regex(
        "\\b(verify|confirm|send|provide|enter|update).{0,60}" +
                "\\b(login|log\\s*in|sign\\s*in|username|user\\s*id|password|passcode|pin\\s*code|pin\\b|" +
                "otp|verification\\s+code|security\\s+code|authenticate|apple\\s+id)\\b",
        RegexOption.IGNORE_CASE
    )
}

val actionRegex by lazy {
    Regex(
        "\\b(verify|confirm|update|contact|reset|authenticate|login|log in|sign in)\\b",
        RegexOption.IGNORE_CASE
    )
}
val nounRegex by lazy {
    Regex(
        "\\b(password|passcode|pin|expired|credentials|account|login|username|security code|verification code|otp|2fa)\\b",
        RegexOption.IGNORE_CASE
    )
}

val prizeScamKeywordsRegex by lazy { Regex("\\b(won|winner|winning|prize|award|claim|collect|receive|redeem|gain(?:ed)?)\\b", RegexOption.IGNORE_CASE) }

val urgentInfoRequestRegex by lazy {
    Regex(
        "\\b(verify|confirm|update|validate|provide|send).{0,50}" +
                "\\b(account\\s+number|identity|personal\\s+information|contact\\s+details|billing\\s+information)\\b",
        RegexOption.IGNORE_CASE
    )
}

// Regex constants extracted from Helper.kt

val whitespaceRegex by lazy { Regex("\\s+") }

val emailExtractRegex by lazy { Regex("([A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,})", RegexOption.IGNORE_CASE) }

private val adultTerms = listOf(
    "xxx", "p[o0]rn", "nude", "nudes", "c[u0]m", "s[e3]x",
    "sexcam", "camgirl", "escort", "filthy", "hardcore", "cum",
    "sexy", "laid"
)
private val slangTerms = listOf(
    "shag", "hookup", "flirt", "horny", "hot\\s+pics",
    "rude\\s+chat", "private\\s+line", "gettin\\s+it", "dirty"
)
val adultContentRegex by lazy { Regex("\\b(" + (adultTerms + slangTerms).joinToString("|") + ")\\b", RegexOption.IGNORE_CASE) }

val urgentWordRegex by lazy { Regex("\\burgent\\b", RegexOption.IGNORE_CASE) }
val prizeWinWordRegex by lazy { Regex("\\b(prize|award|won|winner)\\b", RegexOption.IGNORE_CASE) }
val currencySymbolRegex by lazy { Regex("[£$€]") }
val selectedWordRegex by lazy { Regex("\\bselected\\b", RegexOption.IGNORE_CASE) }
val receiveAwardRegex by lazy { Regex("\\b(receive|award|prize)\\b", RegexOption.IGNORE_CASE) }
val moneyAmountRegex by lazy { Regex("\\b\\d{3,}\\s*(pounds|gbp|usd|eur|inr|nok|sek|dkk)\\b", RegexOption.IGNORE_CASE) }

val telecomBrandRegex by lazy { Regex("\\b(orange|vodafone|tmobile|t-mobile|o2)\\s+(customer|subscriber|user)", RegexOption.IGNORE_CASE) }
val advanceFeeFormalRegex by lazy {
    Regex(
        "\\b(from\\s+the\\s+desk\\s+of|office\\s+of|attn\\b|attention\\b|" +
        "director|chairman|vice\\s+president|ambassador|attorney|barrister)\\b",
        RegexOption.IGNORE_CASE
    )
}
val bankNoticedDebitRegex by lazy { Regex("\\bhas noticed\\b.*\\bdebit card\\b.*\\bused\\b", RegexOption.IGNORE_CASE) }

val sensitiveFinInfoRegex by lazy {
    Regex(
        "\\b(account\\s+number|credit\\s+card|debit\\s+card|cvv|cvc|ssn|social\\s+security|" +
        "sort\\s+code|routing\\s+number|iban|bank\\s+details|credit\\s+card\\s+number)\\b",
        RegexOption.IGNORE_CASE
    )
}
val provideVerbRegex by lazy { Regex("\\b(provide|send|enter|verify|confirm|update|share|give|submit)\\b", RegexOption.IGNORE_CASE) }
val nameFieldRegex by lazy { Regex("\\b(your\\s+)?name\\b", RegexOption.IGNORE_CASE) }
val addressFieldRegex by lazy { Regex("\\b(your\\s+)?(address|house\\s+no)\\b", RegexOption.IGNORE_CASE) }
val phoneFieldRegex by lazy { Regex("\\b(your\\s+)?(phone|mobile|cell|number)\\b", RegexOption.IGNORE_CASE) }
val ageDobFieldRegex by lazy { Regex("\\b(your\\s+)?(age|dob|date\\s+of\\s+birth|d\\.o\\.b\\.?)\\b", RegexOption.IGNORE_CASE) }
val postcodeFieldRegex by lazy { Regex("\\b(your\\s+)?(postcode|zip\\s*code|postal\\s*code)\\b", RegexOption.IGNORE_CASE) }
val requestVerbRegex by lazy { Regex("\\b(send|reply|provide|text|forward|email|give|share)\\b", RegexOption.IGNORE_CASE) }
val explicitFieldsRegex by lazy {
    Regex(
        "\\b(send|provide|reply|forward).{0,80}(name.{0,40}(address|phone|number|age|postcode)|" +
        "address.{0,40}(name|phone|number|age)|phone.{0,40}(name|address))\\b",
        RegexOption.IGNORE_CASE
    )
}
val urgencyWordRegex by lazy { Regex("\\b(urgent|immediately|now|today|asap)\\b", RegexOption.IGNORE_CASE) }
val restrictedAccountRegex by lazy {
    Regex(
        "\\b(suspended|locked|restricted|limited|blocked|disabled|frozen).{0,80}(account|access|card)\\b",
        RegexOption.IGNORE_CASE
    )
}
val specificInfoNeededRegex by lazy {
    Regex(
        "\\b(provide|verify|confirm|update).{0,50}" +
        "\\b(account\\s+number|personal\\s+information|identity|contact\\s+details|billing\\s+information|name|address|phone)\\b",
        RegexOption.IGNORE_CASE
    )
}
val directNeedRegex by lazy {
    Regex(
        "\\bi\\s+(need|require|want|ask).{0,50}(your|you).{0,50}" +
        "(address|dob|phone|mobile|postcode|details|information)\\b",
        RegexOption.IGNORE_CASE
    )
}
val startsWithRequestRegex by lazy { Regex("^(reply|send|text|provide)\\s+(with|us)\\b", RegexOption.IGNORE_CASE) }
val advanceFeeRegex by lazy {
    Regex(
        "\\b(seek\\s+(your\\s+)?cooperation|can\\s+you\\s+assist|need\\s+(your\\s+)?assistance|" +
        "require\\s+(your\\s+)?cooperation|business\\s+proposal)\\b",
        RegexOption.IGNORE_CASE
    )
}
val financialTransferRegex by lazy {
    Regex(
        "\\b(transfer.{0,30}(fund|money|amount)|fund.{0,30}transfer|" +
        "bank\\s+account.{0,50}(transfer|details)|sum\\s+of.{0,30}(million|thousand|usd|gbp|eur))\\b",
        RegexOption.IGNORE_CASE
    )
}
val contactFinancialRegex by lazy {
    Regex(
        "\\b(contact|reply|respond|email\\s+me).{0,50}(fund|transfer|claim|bank|account|payment)\\b",
        RegexOption.IGNORE_CASE
    )
}
val financialContactRegex by lazy {
    Regex(
        "\\b(fund|transfer|claim|bank|account|payment).{0,50}(contact|reply|respond|email\\s+me)\\b",
        RegexOption.IGNORE_CASE
    )
}
val needMoreInfoRegex by lazy { Regex("\\b(need|require).{0,30}(more|additional).{0,30}(information|details)\\b", RegexOption.IGNORE_CASE) }
val accountContextRegex by lazy { Regex("\\b(account|billing|payment|profile|service|identity|verification)\\b", RegexOption.IGNORE_CASE) }
val vagueSocialRegex by lazy { Regex("\\bupdate\\s+(your\\s+)?security\\s+details\\b", RegexOption.IGNORE_CASE) }
val sensitiveFieldsRegex by lazy {
    Regex(
        "\\b(name|address|phone|mobile|postcode|account\\s+number|credit\\s+card|ssn)\\b",
        RegexOption.IGNORE_CASE
    )
}
val telecomPromoRegex by lazy { Regex("\\b(o2|orange|vodafone|t-?mobile|three|ee|verizon|at&t|sprint)\\b", RegexOption.IGNORE_CASE) }
val promoRewardRegex by lazy { Regex("\\b(free\\s+call\\s+credit|free\\s+credit|reward|points|cashback|loyalty)\\b", RegexOption.IGNORE_CASE) }
val casualDatingRegex by lazy {
    Regex(
        "\\b(meet\\s+someone|find\\s+a\\s+date|flirt).{0,60}\\b(eg|e\\.g\\.|example)\\s+\\w+\\s+\\d+",
        RegexOption.IGNORE_CASE
    )
}
