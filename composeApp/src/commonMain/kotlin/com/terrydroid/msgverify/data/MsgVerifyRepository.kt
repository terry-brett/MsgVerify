package com.terrydroid.msgverify.data

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.component.KoinComponent


class MsgVerifyRepository : KoinComponent {

    suspend fun verifyLink(url: Url): Flow<Result<LinkVerificationResponse>> {

        //TODO: Integrate with model. Now it will just return a mock score
        return flowOf(Result.success(LinkVerificationResponse(maliciousScore = 0.64f)))
    }
}

/**
 * After asking the machine learning model if the link is malicious,
 * it will return a score. This is the @property maliciousScore. This is a pecentage
 * 0 - 1
 */
data class LinkVerificationResponse(
    val maliciousScore: Float
)
