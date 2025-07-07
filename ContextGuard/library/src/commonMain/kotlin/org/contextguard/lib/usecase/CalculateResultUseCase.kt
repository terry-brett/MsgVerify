package org.contextguard.lib.usecase

import org.contextguard.googlesafebrowsing.service.SafeBrowseServiceImpl
import org.contextguard.models.Result


/**
 * Use case used to compute the result given the message content and optional sender and URL
 * @param message: The content of the message to be analysed
 * @param sender: Optional sender, this can be a phone number
 * or email address where the message came from
 * @param url: Optional url
 */

class CalculateResultUseCase(
    private val message: String,
    private val sender: String? = null,
    private val url: String? = null
) : ICalculateResult {
    operator fun invoke() : Result {
        return calculateResult(
            message = message,
            sender = sender,
            url = url
        )
    }

    override fun calculateResult(
        message: String,
        sender: String?,
        url: String?
    ): Result {
        return Result(
            score = 0,
            reasons = null
        )
    }
}
