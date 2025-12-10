package com.terrydroid.msgverify.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrydroid.msgverify.data.LinkVerificationResponse
import com.terrydroid.msgverify.data.MsgVerifyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.contextguard.lib.MLKit.urlClassification.UrlVerifierHelper


class HomeViewModel(
    private val msgVerifyRepository: MsgVerifyRepository
) : ViewModel() {

    private val _linkVerificationState: MutableStateFlow<LinkVerificationState> = MutableStateFlow(
        LinkVerificationState.Idle(
            verifiedLinkHistory = emptyList(),
            bottomSheetInformation = null
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
        val isValidUrl = try {
            UrlVerifierHelper(inputLink)
            true
        } catch (_: IllegalArgumentException) {
           false
        }
        if (!isValidUrl) {
            viewModelScope.launch {
                msgVerifyRepository.verifyContent(inputLink).collect { result ->
                    result.fold(
                        onSuccess = {
                            onSuccessResult(it, inputLink)
                        },
                        onFailure = {
                            _linkVerificationState.value = LinkVerificationState.Error(
                                errorMessage = "Is not a valid url",
                                verifiedLinkHistory = _linkVerificationState.value.verifiedLinkHistory,
                                bottomSheetInformation = _linkVerificationState.value.bottomSheetInformation
                            )
                        }
                    )
                }
            }
        } else {
            viewModelScope.launch {
                msgVerifyRepository.verifyLink(inputLink).collect { result ->
                    result.fold(
                        onSuccess = {
                            onSuccessResult(it, inputLink)
                        },
                        onFailure = {
                            _linkVerificationState.value = LinkVerificationState.Error(
                                errorMessage = "Could not get the score for this link",
                                verifiedLinkHistory = _linkVerificationState.value.verifiedLinkHistory,
                                bottomSheetInformation = _linkVerificationState.value.bottomSheetInformation
                            )
                        }
                    )
                }
            }
        }
    }

    private fun onSuccessResult(
        response: LinkVerificationResponse,
        inputLink: String
    ) {
        val maliciousScorePercent = response.maliciousScore

        //TODO: Change these when we agree on threshold
        val classificationColor = if (maliciousScorePercent > 70) {
            ClassificationColor.Green
        } else if (maliciousScorePercent >= 40 && maliciousScorePercent < 70) {
            ClassificationColor.Yellow
        } else {
            ClassificationColor.Red
        }
        val linkResult = LinkResult(
            linkMaliciousPercentage = maliciousScorePercent,
            classificationColor = classificationColor,
            url = inputLink,
            description = response.description
        )
        val newVerifierLinkHistory = listOf(linkResult) + _linkVerificationState
            .value
            .verifiedLinkHistory

        _linkVerificationState.value =
            LinkVerificationState.Success(
                linkResult = linkResult,
                verifiedLinkHistory = newVerifierLinkHistory,
                bottomSheetInformation = linkResult
            )
    }
}
