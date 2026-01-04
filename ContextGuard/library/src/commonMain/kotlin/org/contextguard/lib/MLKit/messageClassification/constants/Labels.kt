package org.contextguard.lib.MLKit.messageClassification.constants

object Labels {
    val reasonLabels = listOf(
        "Grammatical Errors/Poor Formatting",
        "Impersonation",
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
