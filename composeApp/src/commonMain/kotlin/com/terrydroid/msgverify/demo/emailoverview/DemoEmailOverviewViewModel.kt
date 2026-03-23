package com.terrydroid.msgverify.demo.emailoverview

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrydroid.msgverify.data.ContentVerificationResponse
import com.terrydroid.msgverify.data.MsgVerifyRepository
import com.terrydroid.msgverify.demo.emailoverview.model.DemoEmailUiState
import com.terrydroid.msgverify.demo.mapper.toUiModels
import com.terrydroid.msgverify.demo.smsoverview.TrafficLight
import com.terrydroid.msgverify.demo.smsoverview.UrlScore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class DemoEmailOverviewViewModel(
    private val msgVerifyRepository: MsgVerifyRepository,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _emails = MutableStateFlow<List<EmailMessage>>(emptyList())
    private val _isLoading = MutableStateFlow(true)

    val uiState: StateFlow<DemoEmailUiState> = _emails
        .combine(_isLoading) { list, loading ->
            if (loading && list.isEmpty()) DemoEmailUiState.Loading
            else DemoEmailUiState.Success(list)
        }
        .flowOn(dispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DemoEmailUiState.Loading
        )

    init {
        loadAndVerifyEmails()
    }

    private fun loadAndVerifyEmails() {
        viewModelScope.launch(dispatcher) {
            val mockData = getEmailMockdata().messages
            _emails.value = mockData
            _isLoading.value = false

            mockData.forEach { email ->
                // Launch verification for each email in parallel
                launch {
                    msgVerifyRepository.verifyContent(email.preview, email.fromName)
                        .collect { result ->
                            result.onSuccess { response ->
                                updateEmailStatus(email.id, response)
                            }
                        }
                }
            }
        }
    }

    private fun updateEmailStatus(id: Int, response: ContentVerificationResponse) {
        val (light, reasons, scores) = response.toUiModels()

        _emails.update { currentList ->
            currentList.map { email ->
                if (email.id == id) {
                    email.copy(
                        trafficLight = light,
                        reasons = reasons,
                        urlScores = scores
                    )
                } else email
            }
        }
    }
}


@Immutable
data class Messages(val messages: List<EmailMessage>)

@Immutable
data class EmailMessage(
    val id: Int,
    val fromName: String,
    val fromEmail: String,
    val subject: String,
    val preview: String,
    val body: String,
    val unread: Boolean,
    val trafficLight: TrafficLight? = null,
    val reasons: List<String> = emptyList(),
    val urlScores: List<UrlScore> = emptyList()
)
