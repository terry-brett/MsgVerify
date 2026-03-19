package com.terrydroid.msgverify.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrydroid.msgverify.data.ContentVerificationResponse
import com.terrydroid.msgverify.data.MsgVerifyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val msgVerifyRepository: MsgVerifyRepository) : ViewModel() {

  private val _linkVerificationState: MutableStateFlow<LinkVerificationState> =
      MutableStateFlow(
          LinkVerificationState.Idle(
              verifiedLinkHistory = emptyList(),
              bottomSheetInformation = null,
          )
      )

  val linkVerificationState: StateFlow<LinkVerificationState>
    get() = _linkVerificationState.asStateFlow()
  fun onShowBottomSheetDescription(linkResult: LinkResult) {
    viewModelScope.launch {
      _linkVerificationState.value =
          _linkVerificationState.value.copyWithBottomSheetInformation(linkResult)
    }
  }

  fun onHideBottomSheet() {
    viewModelScope.launch {
      _linkVerificationState.value =
          _linkVerificationState.value.copyWithBottomSheetInformation(null)
    }
  }

  fun onVerifyClicked(inputContent: String) {
    _linkVerificationState.value =
        LinkVerificationState.LoadingVerification(
            verifiedLinkHistory = _linkVerificationState.value.verifiedLinkHistory,
            bottomSheetInformation = _linkVerificationState.value.bottomSheetInformation,
        )

    viewModelScope.launch {
      msgVerifyRepository.verifyContent(inputContent, "").collect { result ->
        result.fold(
            onSuccess = { onContentSuccessResult(it, inputContent) },
            onFailure = {
              _linkVerificationState.value =
                  LinkVerificationState.Error(
                      errorMessage = "Could not verify this content",
                      verifiedLinkHistory = _linkVerificationState.value.verifiedLinkHistory,
                      bottomSheetInformation =
                          _linkVerificationState.value.bottomSheetInformation,
                  )
            },
        )
      }
    }
  }

  private fun onContentSuccessResult(response: ContentVerificationResponse, inputContent: String) {
      println("response is $response")
    when (response) {
      is ContentVerificationResponse.Safe -> {
        val hasUrls = !response.extractedUrls.isNullOrEmpty()
        val maxUrlScore = response.urlScores?.maxOrNull() ?: 0f
        val classificationColor = getClassificationColor(maxUrlScore)

        val linkResult = LinkResult(
            linkMaliciousPercentage = maxUrlScore,
            classificationColor = classificationColor,
            url = inputContent,
            description = "Content is safe",
            hasUrls = hasUrls
        )
        val newVerifierLinkHistory =
            listOf(linkResult) + _linkVerificationState.value.verifiedLinkHistory

        _linkVerificationState.value =
            LinkVerificationState.Success(
                linkResult = linkResult,
                verifiedLinkHistory = newVerifierLinkHistory,
                bottomSheetInformation = linkResult,
            )
      }
      is ContentVerificationResponse.Unsafe -> {
        val hasUrls = !response.extractedUrls.isNullOrEmpty()
        val maxUrlScore = response.urlScores?.maxOrNull() ?: 0f
        val classificationColor = getClassificationColor(maxUrlScore)

        // Check if input is URL-only (no extra text)
        val isUrlOnly = hasUrls && response.extractedUrls?.any {
            inputContent.trim() == it.trim()
        } == true

        val reasonsDescription = if (isUrlOnly) {
            // For URL-only input, don't show text spam reasons
            "URL analysis result"
        } else {
            // For text with URLs, show all reasons
            response.reasons.joinToString(", ") { it.reason }
        }

        val linkResult = LinkResult(
            linkMaliciousPercentage = maxUrlScore,
            classificationColor = classificationColor,
            url = inputContent,
            description = reasonsDescription,
            hasUrls = hasUrls
        )
        val newVerifierLinkHistory =
            listOf(linkResult) + _linkVerificationState.value.verifiedLinkHistory

        _linkVerificationState.value =
            LinkVerificationState.Success(
                linkResult = linkResult,
                verifiedLinkHistory = newVerifierLinkHistory,
                bottomSheetInformation = linkResult,
            )
      }
    }
  }

  private fun getClassificationColor(maliciousScore: Float): ClassificationColor {
    // Score is 0-1, convert to 0-100 for comparison
    val scorePercent = maliciousScore * 100
    return when {
      scorePercent > 70 -> ClassificationColor.Red
      scorePercent >= 40 && scorePercent < 70 -> ClassificationColor.Yellow
      else -> ClassificationColor.Green
    }
  }
}
