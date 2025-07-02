package com.terrydroid.msgverify.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrydroid.msgverify.data.LinkVerificationResponse
import com.terrydroid.msgverify.data.MsgVerifyRepository
import io.ktor.http.URLParserException
import io.ktor.http.Url
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

        //TODO: Do verification that it is actually a link
        viewModelScope.launch {
            try {
                val url = Url(inputLink)
                msgVerifyRepository.verifyLink(url).collect { result ->
                    if (result.isSuccess) {
                        _linkVerificationState.value = LinkVerificationState.Success()
                    }
                }
            } catch (e: URLParserException) {
                _linkVerificationState.value = LinkVerificationState.Error("Not a valid url")
            }
        }

    }

}



