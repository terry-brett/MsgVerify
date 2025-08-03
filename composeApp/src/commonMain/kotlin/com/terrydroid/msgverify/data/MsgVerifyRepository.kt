package com.terrydroid.msgverify.data

import com.terrydroid.msgverify.PlatformContext
import com.terrydroid.msgverify.getPlatform
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.component.KoinComponent
import org.contextguard.lib.MLKit.UrlPrediction

class MsgVerifyRepository(val platformContext: PlatformContext) : KoinComponent {

    suspend fun verifyLink(url: String): Flow<Result<LinkVerificationResponse>> {

        val result = UrlPrediction(platformContext.getNativeContext()).makePrediction(url)
        //TODO: Integrate with model. Now it will just return a mock score
        return flowOf(
            Result.success(
                LinkVerificationResponse(
                    maliciousScore = 0.45f,
                    description = result
                )
            )
        )
    }

    fun getSafeBrowserUrls(): Flow<Result<Unit>> {
        //TODO: Integrate this with a service to get the Urls. I guess it will come from context guard
        return flowOf(
            Result.success(Unit)
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
