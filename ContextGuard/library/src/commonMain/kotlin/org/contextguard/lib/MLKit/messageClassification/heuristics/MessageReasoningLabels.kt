package org.contextguard.lib.mlkit.messageClassification.heuristics

import org.contextguard.models.Reason

class MessageReasoningLabels(
    message: String,
    private val sender: String
) {
    private val cleanMessage: String = message.normalise()

    private object Labels {
        const val IMPERSONATION = "Impersonation"
        const val MARKETING = "Marketing"
        const val ADULT_CONTENT = "Adult Content"
        const val URGENCY = "Urgency/Intimidation"
        const val LINK_PRESSURE = "Link Click Pressure"
        const val FINANCIAL_PERSONAL = "Financial or Personal Information"
        const val TOO_GOOD = "Too Good to Be True"
        const val CREDENTIAL_VERIFICATION = "Credential Verification Request"
        const val FALLBACK = "Uncategorized Spam"
    }

    fun addLabels(): MutableList<Reason> {
        val checks: List<Pair<String, (String) -> Boolean>> = listOf(
            Labels.IMPERSONATION to { msg -> msg.checkImpersonation(sender) },
            Labels.MARKETING to { msg -> msg.hasMarketingPatterns() },
            Labels.ADULT_CONTENT to { msg -> msg.hasAdultContentPatterns() },
            Labels.URGENCY to { msg -> msg.hasUrgencyOrIntimidationPatterns() },
            Labels.LINK_PRESSURE to { msg -> msg.containsUrl() },
            Labels.FINANCIAL_PERSONAL to { msg -> msg.asksForFinancialOrPersonalInfo() },
            Labels.TOO_GOOD to { msg -> msg.hasTooGoodToBeTruePatterns() },
            Labels.CREDENTIAL_VERIFICATION to { msg -> msg.hasCredentialVerificationPatterns() },
        )

        val reasons = checks
            .asSequence()
            .filter { (_, predicate) -> predicate(cleanMessage) }
            .map { (label, _) -> Reason(label) }
            .toMutableList()

        // Evaluate custom heuristics
        val customReasons = HeuristicRegistry.evaluate(cleanMessage, sender)
        reasons.addAll(customReasons)

        return reasons.ifEmpty { mutableListOf(Reason(Labels.FALLBACK)) }
    }
}
