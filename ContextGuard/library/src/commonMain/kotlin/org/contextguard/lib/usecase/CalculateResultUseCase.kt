package org.contextguard.lib.usecase

import org.contextguard.addLinkWarningReason
import org.contextguard.lib.MLKit.messageClassification.MessagePrediction
import org.contextguard.lib.MLKit.urlClassification.UrlPrediction
import org.contextguard.models.Result
import org.contextguard.models.TextClassificationResult
import org.contextguard.toListOfReasons


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
    private val url: String? = null,
    private val platformContext: Any
) {
    suspend operator fun invoke(): Result {
        return calculateResult(
            message = message,
            sender = sender,
            url = url,
            platformContext = platformContext
        )
    }

    private suspend fun calculateResult(
        message: String,
        sender: String?,
        url: String?,
        platformContext: Any
    ): Result {

        val urlPredictionScore = url?.let {
            UrlPrediction(platformContext).makePrediction(it)
        }

        val messageSpamProbability = MessagePrediction(platformContext).getSpamPrediction(
            message
        )

        if (messageSpamProbability > 0.9){

        }

        return Result(
            urlScore = urlPredictionScore,
            textClassificationResult = TextClassificationResult.Unsafe(
                emptyList()
            )
        )
    }
}
