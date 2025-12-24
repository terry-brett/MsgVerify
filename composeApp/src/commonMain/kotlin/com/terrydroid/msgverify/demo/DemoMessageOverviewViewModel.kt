package com.terrydroid.msgverify.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrydroid.msgverify.data.MsgVerifyRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DemoMessageOverviewViewModel(
    private val msgVerifyRepository: MsgVerifyRepository,
    dispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _state: MutableStateFlow<Messages> = MutableStateFlow(getMessagesMockdata())

    val state: StateFlow<Messages>
        get() = _state.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            state.value.messages.forEach { message ->
                msgVerifyRepository.verifyContent(message.message).collect { result ->
                    result.fold(
                        onFailure = {},
                        onSuccess = { value ->
                            val maliciousScorePercent = value.maliciousScore

                            val classification = getClassification(maliciousScorePercent)
                            _state.update {
                                Messages(
                                    it.messages.replace(
                                        Message(
                                            id = message.id,
                                            title = message.title,
                                            message = message.message,
                                            trafficLight = classification
                                        )
                                    ) { value ->
                                        value == message
                                    }
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
    return map {
        if (block(it)) newValue else it
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
    val reason: String? = null
)

enum class TrafficLight {
    Red, Yellow, Green
}