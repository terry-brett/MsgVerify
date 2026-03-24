package com.terrydroid.msgverify.demo.smsoverview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrydroid.msgverify.data.ContentVerificationResponse
import com.terrydroid.msgverify.data.MsgVerifyRepository
import com.terrydroid.msgverify.demo.mapper.toUiModels
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class DemoMessageOverviewViewModel(
    private val msgVerifyRepository: MsgVerifyRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val verificationSemaphore = Semaphore(5)

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val state: StateFlow<Messages> = _messages
        .map { Messages(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Messages(emptyList()))

    init {
        loadAndVerifyMessages()
    }

    private fun loadAndVerifyMessages() {
        viewModelScope.launch(ioDispatcher) {
            val mockData = getMessagesMockdata().messages
            _messages.value = mockData

            // Launch verification for each message
            mockData.forEach { message ->
                launch {
                    verificationSemaphore.withPermit {
                        msgVerifyRepository.verifyContent(message.message, message.title)
                            .collect { result ->
                                result.onSuccess { response ->
                                    updateSingleMessage(message.id, response)
                                }
                            }
                    }
                }
            }
        }
    }

    private fun updateSingleMessage(id: Int, response: ContentVerificationResponse) {
        _messages.update { currentList ->
            currentList.map { msg ->
                if (msg.id == id) {
                    val (light, reasons, scores) = response.toUiModels()
                    msg.copy(trafficLight = light, reasons = reasons, urlScores = scores)
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
    val urlScores: List<UrlScore> = emptyList(),
)

data class UrlScore(val url: String, val score: Float)

enum class TrafficLight {
  Red,
  Yellow,
  Green,
}

