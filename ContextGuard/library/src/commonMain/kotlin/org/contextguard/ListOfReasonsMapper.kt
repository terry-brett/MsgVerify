package org.contextguard

import org.contextguard.models.Reason

internal fun List<String>.toListOfReasons(): List<Reason> = this.map {
    Reason(
        reason = it
    )
}

internal fun MutableList<Reason>.addLinkWarningReason(
    urlPredictionScore: Float
) : List<Reason> {
    return if (urlPredictionScore != -1f && urlPredictionScore > 50){
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
