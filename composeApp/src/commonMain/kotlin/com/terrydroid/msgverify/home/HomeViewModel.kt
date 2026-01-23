package com.terrydroid.msgverify.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrydroid.msgverify.data.ContentVerificationResponse
import com.terrydroid.msgverify.data.LinkVerificationResponse
import com.terrydroid.msgverify.data.MsgVerifyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.contextguard.lib.MLKit.urlClassification.UrlVerifierHelper
import org.contextguard.models.Reason

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

  fun onVerifyClicked(inputLink: String) {
    val urlVerifier = UrlVerifierHelper(inputLink)
    val isValidUrl = !urlVerifier.isMalformed

    _linkVerificationState.value =
        LinkVerificationState.LoadingVerification(
            verifiedLinkHistory = _linkVerificationState.value.verifiedLinkHistory,
            bottomSheetInformation = _linkVerificationState.value.bottomSheetInformation,
        )

    if (!isValidUrl) {
      viewModelScope.launch {
        msgVerifyRepository.verifyLink(inputLink).collect { result ->
          result.fold(
              onSuccess = { onLinkSuccessResult(it, inputLink) },
              onFailure = {
                _linkVerificationState.value =
                    LinkVerificationState.Error(
                        errorMessage = "Malformed URL",
                        verifiedLinkHistory = _linkVerificationState.value.verifiedLinkHistory,
                        bottomSheetInformation =
                            _linkVerificationState.value.bottomSheetInformation,
                    )
              },
          )
        }
      }
    } else {
      viewModelScope.launch {
        msgVerifyRepository.verifyContent(inputLink, "").collect { result ->
          result.fold(
              onSuccess = { onContentSuccessResult(it, inputLink) },
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
  }

  private fun onContentSuccessResult(response: ContentVerificationResponse, inputContent: String) {
    when (response) {
      is ContentVerificationResponse.Safe -> {
        _linkVerificationState.value =
            LinkVerificationState.Success(
                linkResult = LinkResult(
                    linkMaliciousPercentage = 0f,
                    classificationColor = ClassificationColor.Green,
                    url = inputContent,
                    description = "Content is safe",
                ),
                verifiedLinkHistory = _linkVerificationState.value.verifiedLinkHistory,
                bottomSheetInformation = null,
            )
      }
      is ContentVerificationResponse.Unsafe -> {
        val maxUrlScore = response.urlScores?.maxOrNull() ?: 0f
        val classificationColor = getClassificationColor(maxUrlScore)
        val reasonsDescription = response.reasons.joinToString(", ") { it.reason }

        val linkResult = LinkResult(
            linkMaliciousPercentage = maxUrlScore,
            classificationColor = classificationColor,
            url = inputContent,
            description = reasonsDescription,
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

  private fun onLinkSuccessResult(response: LinkVerificationResponse, inputLink: String) {
    val maliciousScorePercent = response.maliciousScore
    val classificationColor = getClassificationColor(maliciousScorePercent)

    val linkResult =
        LinkResult(
            linkMaliciousPercentage = maliciousScorePercent,
            classificationColor = classificationColor,
            url = inputLink,
            description = response.description,
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
    // Score is 0-1, convert to 0-100 for comparison
    val scorePercent = maliciousScore * 100
    return when {
      scorePercent > 70 -> ClassificationColor.Red
      scorePercent >= 40 && scorePercent < 70 -> ClassificationColor.Yellow
      else -> ClassificationColor.Green
    }
  }
}
