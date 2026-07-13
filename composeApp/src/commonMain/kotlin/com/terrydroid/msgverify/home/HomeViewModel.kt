package com.terrydroid.msgverify.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrydroid.msgverify.config.MsgVerifyConfig
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
    _linkVerificationState.value =
        _linkVerificationState.value.copyWithBottomSheetInformation(linkResult)
  }

  fun onHideBottomSheet() {
    _linkVerificationState.value =
        _linkVerificationState.value.copyWithBottomSheetInformation(null)
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
    val hasUrls: Boolean
    val maxUrlScore: Float
    val description: String

    when (response) {
      is ContentVerificationResponse.Safe -> {
        hasUrls = !response.extractedUrls.isNullOrEmpty()
        maxUrlScore = response.urlScores?.maxOrNull() ?: 0f
        description = "Content is safe"
      }
      is ContentVerificationResponse.Unsafe -> {
        hasUrls = !response.extractedUrls.isNullOrEmpty()
        maxUrlScore = response.urlScores?.maxOrNull() ?: 0f
        val isUrlOnly = hasUrls && response.extractedUrls?.any {
          inputContent.trim() == it.trim()
        } == true
        description = if (isUrlOnly) "URL analysis result" else response.reasons.joinToString(", ") { it.reason }
      }
    }

    val classificationColor = getClassificationColor(maxUrlScore)
    val linkResult = LinkResult(
        linkMaliciousPercentage = maxUrlScore,
        classificationColor = classificationColor,
        url = inputContent,
        description = description,
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

  private fun getClassificationColor(maliciousScore: Float): ClassificationColor {
    val scorePercent = maliciousScore * 100
    return when {
      scorePercent > MsgVerifyConfig.highRiskThreshold -> ClassificationColor.Red
      scorePercent >= MsgVerifyConfig.mediumRiskThreshold -> ClassificationColor.Yellow
      else -> ClassificationColor.Green
    }
  }
}
