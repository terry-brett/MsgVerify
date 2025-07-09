package org.contextguard.lib.usecase

import org.contextguard.models.Result

interface ICalculateResult {
    fun calculateResult(
        message: String,
        sender: String?,
        url: String?
    ) : Result
}
