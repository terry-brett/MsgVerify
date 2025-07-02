package com.terrydroid.msgverify.home

sealed class LinkVerificationState {
    data object Idle : LinkVerificationState()
    data object LoadingVerification : LinkVerificationState()
    data class Error(val errorMessage: String) : LinkVerificationState()
    data class Success(val linkMaliciousPercentage: String) : LinkVerificationState()
}
