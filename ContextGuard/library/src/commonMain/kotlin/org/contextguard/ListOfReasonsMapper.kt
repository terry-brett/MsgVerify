package org.contextguard

import org.contextguard.lib.MLKit.urlClassification.constants.Constants
import org.contextguard.lib.spellcheck.Spellcheck
import org.contextguard.models.Reason

internal suspend fun MutableList<Reason>.checkUrlAndSpelling(
    content: String,
    platformContext: Any,
    urlPredictionScores: List<Float>
): List<Reason> {
    val hasSuspiciousLink = urlPredictionScores.any { it > Constants.HIGH_RISK_URL_SCORE }

    Spellcheck.init(platformContext)
    val hasSpellingIssues = Spellcheck.isMisspelled(content)

    if (hasSuspiciousLink) {
        add(Reason(reason = "Suspicious Link"))
    }

    if (hasSpellingIssues) {
        add(Reason(reason = "Grammatical Errors/Poor Formatting"))
    }

    return this
}
