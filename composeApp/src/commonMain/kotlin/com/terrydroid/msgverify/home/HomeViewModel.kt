package com.terrydroid.msgverify.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrydroid.msgverify.data.MsgVerifyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class HomeViewModel(
    private val msgVerifyRepository: MsgVerifyRepository
) : ViewModel() {

    private val _linkVerificationState: MutableStateFlow<LinkVerificationState> = MutableStateFlow(
        LinkVerificationState.Idle(emptyList())
    )

    val linkVerificationState: StateFlow<LinkVerificationState>
        get() = _linkVerificationState.asStateFlow()


    fun onVerifyClicked(inputLink: String) {
        if (!isValidUrl(inputLink)) {
            _linkVerificationState.value = LinkVerificationState.Error(
                errorMessage = "Is not a valid url",
                verifiedLinkHistory = _linkVerificationState.value.verifiedLinkHistory
            )
        } else {
            viewModelScope.launch {
                msgVerifyRepository.verifyLink(inputLink).collect { result ->
                    result.fold(
                        onSuccess = {
                            val maliciousScorePercent = it.maliciousScore * 100

                            //TODO: Change these when we agree on threshold
                            val classificationColor = if (maliciousScorePercent < 40) {
                                ClassificationColor.Green
                            } else if (maliciousScorePercent >= 40 && maliciousScorePercent < 70) {
                                ClassificationColor.Yellow
                            } else {
                                ClassificationColor.Red
                            }
                            val linkResult = LinkResult(
                                linkMaliciousPercentage = maliciousScorePercent,
                                classificationColor = classificationColor,
                                url = inputLink
                            )
                            val newVerifierLinkHistory = listOf(linkResult) + _linkVerificationState
                                .value
                                .verifiedLinkHistory

                            _linkVerificationState.value =
                                LinkVerificationState.Success(
                                    linkResult = linkResult,
                                    verifiedLinkHistory = newVerifierLinkHistory
                                )
                        },
                        onFailure = {
                            _linkVerificationState.value = LinkVerificationState.Error(
                                errorMessage = "Could not get the score for this link",
                                verifiedLinkHistory = _linkVerificationState.value.verifiedLinkHistory
                            )
                        }
                    )
                }
            }
        }

    }

}

//TODO: Change to some common url checking that works on CMP
private fun isValidUrl(url: String): Boolean {
    return Regex(
        "^https?://[\\w\\-]+(\\.[\\w\\-]+)+[/#?]?.*\$",
        RegexOption.IGNORE_CASE
    ).matches(
        url
    )
}

