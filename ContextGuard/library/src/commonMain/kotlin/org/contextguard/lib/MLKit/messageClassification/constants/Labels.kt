package org.contextguard.lib.MLKit.messageClassification.constants

object Labels {
    val reasonLabels = listOf(
        "Grammatical Errors/Poor Formatting",
        "Impersonation", // company data
        "Marketing",
        "Adult content",
        "Urgency/Intimidation",
        "Link Click Pressure",
        "Financial/Personal Information Request", // bank account, home address, sort-code, card details, name, social security
        "Too Good To Be True",
        "Generic Greeting",
        "Credential Verification Request",
    )
}

/* rules from playing around in python

 "Impersonation": r"(amazon|mygov|wells fargo|paypal|hmrc|netflix|apple|microsoft|delivery|o2|vodafone|bank)",
            "Urgency": r"(urgent|immediately|action required|expires|final notice|within \d+ hours|hurry|now1!)",
            "Intimidation": r"(locked|suspended|illegal|police|unauthorized|blocked|frozen|security risk)",
            "Financial information request": r"(bank account|sort code|iban|credit card|routing number|account number)",
            "Sensitive Information Request": r"(valid name|house no|postcode|dob|date of birth|ssn)",
            "Too Good To Be True": r"(refund|won|lottery|prize|compensation|gift card|cash|free|selected)",
            "Fake Technical Language": r"(ssl error|database breach|protocol|security certificate|server bypass|error code)",
            "Generic Greeting": r"(dear customer|dear user|valued member|dear sir/madam|hey there darling)",
            "Unusual Account Activity Claim": r"(suspicious activity|unusual login|new device detected|trying 2 contact u)",
            "Credential Verification Request": r"(verify your password|update credentials|confirm login)",
            "Payment Request": r"(pay now|wire transfer|payment required|overdue|outstanding balance)",
            "Bypassing Official Channels": r"(reply to this text|text back|call now|reply with)",
            "Emotional Manipulation": r"(help me|emergency|stuck|stranded|worried|family)",
            "Adult Content / Solicitation": r"(shag|sextext|suzy|dating|hot girls|meet me)",

 */