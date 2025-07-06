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
        LinkVerificationState.Idle
    )

    val linkVerificationState: StateFlow<LinkVerificationState>
        get() = _linkVerificationState.asStateFlow()

    fun onVerifyClicked(inputLink: String) {
        if (!isValidUrl(inputLink)) {
            _linkVerificationState.value = LinkVerificationState.Error("Is not a valid url")
        } else {
            viewModelScope.launch {
                msgVerifyRepository.verifyLink(inputLink).collect { result ->
                    result.fold(
                        onSuccess = {
                            val maliciousScorePercent = it.maliciousScore * 100
                            _linkVerificationState.value =
                                LinkVerificationState.Success("$maliciousScorePercent")
                        },
                        onFailure = {
                            _linkVerificationState.value =
                                LinkVerificationState.Error("Could not get score")
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

