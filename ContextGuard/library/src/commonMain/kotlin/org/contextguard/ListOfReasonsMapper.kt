package org.contextguard

import org.contextguard.models.Reason

internal fun List<String>.toListOfReasons(): List<Reason> = this.map {
    Reason(
        reason = it
    )
}
