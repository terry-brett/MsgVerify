package com.terrydroid.msgverify.demo.emailoverview.model

import com.terrydroid.msgverify.demo.emailoverview.EmailMessage

sealed interface DemoEmailUiState {
    data object Loading : DemoEmailUiState
    data class Success(val emails: List<EmailMessage>) : DemoEmailUiState
}
