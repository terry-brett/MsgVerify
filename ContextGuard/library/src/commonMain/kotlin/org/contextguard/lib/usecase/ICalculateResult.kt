package org.contextguard.lib.usecase

import org.contextguard.models.Result

interface ICalculateResult {
    suspend fun calculateResult(
        message: String,
        sender: String?,
        url: String?,
        platformContext: Any
    ) : Result
}
