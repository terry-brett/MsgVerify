package com.terrydroid.msgverify.demo.socialmedia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrydroid.msgverify.data.ContentVerificationResponse
import com.terrydroid.msgverify.data.MsgVerifyRepository
import com.terrydroid.msgverify.demo.mapper.toUiModels
import com.terrydroid.msgverify.demo.smsoverview.TrafficLight
import com.terrydroid.msgverify.demo.smsoverview.UrlScore
import com.terrydroid.msgverify.demo.socialmedia.model.SocialMediaUiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SocialMediaViewModel(
    private val msgVerifyRepository: MsgVerifyRepository,
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    // Internal source of truth: A simple list of messages
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    private val _isLoading = MutableStateFlow(true)

    val uiState: StateFlow<SocialMediaUiState> = _messages
        .combine(_isLoading) { list, loading ->
            when {
                loading && list.isEmpty() -> SocialMediaUiState.Loading
                else -> SocialMediaUiState.Success(list)
            }
        }
        .flowOn(dispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SocialMediaUiState.Loading
        )

    init {
        loadAndVerify()
    }

    private fun loadAndVerify() {
        viewModelScope.launch(dispatcher) {
            val initialData = getSocialMediaMockdata().messages
            _messages.value = initialData
            _isLoading.value = false // Initial data is loaded, stop showing full-screen loader

            // Start background verification for each item
            initialData.forEach { message ->
                launch {
                    msgVerifyRepository.verifyContent(message.message, message.title)
                        .collect { result ->
                            result.onSuccess { response ->
                                updateMessageInList(message.id, response)
                            }
                        }
                }
            }
        }
    }

    private fun updateMessageInList(id: Int, response: ContentVerificationResponse) {
        val (light, reasons, scores) = response.toUiModels()

        _messages.update { currentList ->
            currentList.map { msg ->
                if (msg.id == id) {
                    msg.copy(
                        trafficLight = light,
                        reasons = reasons,
                        urlScores = scores
                    )
                } else msg
            }
        }
    }
}

data class Messages(
    val messages: List<Message>,
)

data class Message(
    val id: Int,
    val title: String,
    val message: String,
    val trafficLight: TrafficLight? = null,
    val reasons: List<String> = emptyList(),
    val urlScores: List<UrlScore> = emptyList()
)
