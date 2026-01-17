package org.contextguard.lib.usecase

import MessageReasoningLabels
import org.contextguard.addLinkWarningReason
import org.contextguard.lib.MLKit.messageClassification.MessagePrediction
import org.contextguard.lib.MLKit.urlClassification.UrlPrediction
import org.contextguard.models.Reason
import org.contextguard.models.Result
import org.contextguard.models.TextClassificationResult

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

        val isMessageSpam = MessagePrediction(platformContext).isSpam(
            message
        )
        var listOfReasons: List<Reason> = listOf()

        if (isMessageSpam){
            listOfReasons = MessageReasoningLabels(message, sender.orEmpty()).addLabels()
        }

        return Result(
            urlScore = urlPredictionScore,
            textClassificationResult = TextClassificationResult.Unsafe(
                listOfReasons.addLinkWarningReason(urlPredictionScore)
            )
        )
    }
}
