package com.terrydroid.msgverify.home

import androidx.lifecycle.ViewModel
import com.terrydroid.msgverify.data.MsgVerifyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class HomeViewModel(
    msgVerifyRepository: MsgVerifyRepository
) : ViewModel() {

    private val _linkVerificationState: MutableStateFlow<LinkVerificationState> = MutableStateFlow(
        LinkVerificationState.Idle
    )

    val linkVerificationState: StateFlow<LinkVerificationState>
        get() = _linkVerificationState.asStateFlow()

    fun onVerifyClicked(inputLink: String) {

        //TODO: Do verification that it is actually a link

    }

}

sealed class LinkVerificationState {
    data object Idle : LinkVerificationState()
    data object LoadingVerification : LinkVerificationState()
    data class Error(val errorMessage: String) : LinkVerificationState()
    data class Success(val linkMaliciousPercentage: String) : LinkVerificationState()
}

