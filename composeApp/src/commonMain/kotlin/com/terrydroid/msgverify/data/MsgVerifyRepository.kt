package com.terrydroid.msgverify.data

import com.terrydroid.msgverify.PlatformContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.contextguard.lib.MLKit.urlClassification.UrlPrediction
import org.koin.core.component.KoinComponent


class MsgVerifyRepository(val platformContext: PlatformContext) : KoinComponent {

    suspend fun verifyLink(url: String): Flow<Result<LinkVerificationResponse>> {
        val result = UrlPrediction(platformContext.getNativeContext()).makePrediction(url)
        return flowOf(
            Result.success(
                LinkVerificationResponse(
                    maliciousScore = result,
                    description = ""
                )
            )
        )
    }

    suspend fun verifyContent(input: String): Flow<Result<LinkVerificationResponse>> {
        // TODO: Connect this when it becomes available
        return flowOf(
            Result.success(
                LinkVerificationResponse(
                    maliciousScore = 0.5f,
                    description = "Could be malicious"
                )
            )
        )
    }
}

/**
 * After asking the machine learning model if the link is malicious,
 * it will return a score.
 * This is the @property maliciousScore. This is a percentage 0 - 1
 *
 * @property description - Description of why the model thinks it has this score
 */
data class LinkVerificationResponse(
    val maliciousScore: Float,
    val description: String
)
