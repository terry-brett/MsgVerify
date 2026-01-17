package org.contextguard

import org.contextguard.models.Reason

internal fun MutableList<Reason>.addLinkWarningReason(
    urlPredictionScore: Float?
) : List<Reason> {
    return if (urlPredictionScore != null && urlPredictionScore > 50){
        this.add(
            Reason(
                reason = "Suspicious Link"
            )
        )
        this
    } else {
        this
    }
}
