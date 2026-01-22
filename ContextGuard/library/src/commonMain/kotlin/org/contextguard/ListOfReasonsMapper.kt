package org.contextguard

import org.contextguard.lib.spellcheck.Spellcheck
import org.contextguard.models.Reason

internal suspend fun MutableList<Reason>.checkUrlAndSpelling(
    content: String,
    platformContext: Any,
    urlPredictionScore: Float?
): List<Reason> {
    val hasSuspiciousLink = (urlPredictionScore != null && urlPredictionScore > 50)

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
