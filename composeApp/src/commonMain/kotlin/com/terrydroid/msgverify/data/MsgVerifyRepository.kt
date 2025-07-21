package com.terrydroid.msgverify.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.component.KoinComponent


class MsgVerifyRepository : KoinComponent {

    fun verifyLink(url: String): Flow<Result<LinkVerificationResponse>> {

        //TODO: Integrate with model. Now it will just return a mock score
        return flowOf(
            Result.success(
                LinkVerificationResponse(
                    maliciousScore = 0.45f,
                    description = "This url might be malicious since it contains http and " +
                            "not a https scheme"
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
