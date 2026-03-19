package com.terrydroid.msgverify.home

sealed interface LinkVerificationState {
    val verifiedLinkHistory: List<LinkResult>

    val bottomSheetInformation: LinkResult?

    fun copyWithBottomSheetInformation(linkInfo: LinkResult?): LinkVerificationState

    data class Idle(
        override val bottomSheetInformation: LinkResult?,
        override val verifiedLinkHistory: List<LinkResult>
    ) : LinkVerificationState {
        override fun copyWithBottomSheetInformation(linkInfo: LinkResult?): LinkVerificationState {
           return copy(bottomSheetInformation = linkInfo)
        }
    }

    data class LoadingVerification(
        override val verifiedLinkHistory: List<LinkResult>,
        override val bottomSheetInformation: LinkResult?
    ) : LinkVerificationState {
        override fun copyWithBottomSheetInformation(linkInfo: LinkResult?): LinkVerificationState {
           return copy(bottomSheetInformation = linkInfo)
        }
    }

    data class Error(
        val errorMessage: String,
        override val verifiedLinkHistory: List<LinkResult>,
        override val bottomSheetInformation: LinkResult?
    ) : LinkVerificationState {
        override fun copyWithBottomSheetInformation(linkInfo: LinkResult?): LinkVerificationState {
           return copy(bottomSheetInformation = linkInfo)
        }
    }

    data class Success(
        val linkResult: LinkResult,
        override val verifiedLinkHistory: List<LinkResult>,
        override val bottomSheetInformation: LinkResult?
    ) : LinkVerificationState {
        override fun copyWithBottomSheetInformation(linkInfo: LinkResult?): LinkVerificationState {
           return copy(bottomSheetInformation = linkInfo)
        }
    }
}

data class LinkResult(
    val linkMaliciousPercentage: Float,
    val classificationColor: ClassificationColor,
    val url: String,
    val description: String,
    val hasUrls: Boolean = true
)

enum class ClassificationColor {
    Red, Yellow, Green
}

