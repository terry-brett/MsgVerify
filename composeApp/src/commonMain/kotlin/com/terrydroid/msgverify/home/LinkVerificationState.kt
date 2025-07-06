package com.terrydroid.msgverify.home

sealed interface LinkVerificationState {
    val verifiedLinkHistory: List<LinkResult>

    data class Idle(
        override val verifiedLinkHistory: List<LinkResult>
    ) : LinkVerificationState

    data class LoadingVerification(
        override val verifiedLinkHistory: List<LinkResult>
    ) : LinkVerificationState

    data class Error(
        val errorMessage: String,
        override val verifiedLinkHistory: List<LinkResult>
    ) : LinkVerificationState

    data class Success(
        val linkResult: LinkResult,
        override val verifiedLinkHistory: List<LinkResult>
    ) : LinkVerificationState
}

data class LinkResult(
    val linkMaliciousPercentage: Float,
    val classificationColor: ClassificationColor,
    val url: String
)

enum class ClassificationColor {
    Red, Yellow, Green
}

