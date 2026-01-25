package com.terrydroid.msgverify.demo.emailoverview

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrydroid.msgverify.data.ContentVerificationResponse
import com.terrydroid.msgverify.data.MsgVerifyRepository
import com.terrydroid.msgverify.demo.smsoverview.TrafficLight
import com.terrydroid.msgverify.demo.smsoverview.UrlScore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class DemoEmailOverviewViewModel(
    private val msgVerifyRepository: MsgVerifyRepository,
    dispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _state: MutableStateFlow<Messages> = MutableStateFlow(Messages(emptyList()))

    val state: StateFlow<Messages>
        get() = _state.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            val mockData = getEmailMockdata()
            _state.value = mockData
        }

        viewModelScope.launch {
            state.collect { messages ->
                messages.messages.forEach { message ->
                    msgVerifyRepository.verifyContent(message.preview, message.fromName).collect { result ->
                        result.fold(
                            onFailure = {},
                            onSuccess = { response ->
                                val classification: TrafficLight
                                val reasons: List<String>
                                val urlScores: List<UrlScore>

                                when (response) {
                                    is ContentVerificationResponse.Safe -> {
                                        classification = TrafficLight.Green
                                        reasons = emptyList()
                                        urlScores = emptyList()
                                    }
                                    is ContentVerificationResponse.Unsafe -> {
                                        classification = TrafficLight.Red
                                        reasons = response.reasons.map { it.reason }
                                        urlScores = (response.extractedUrls ?: emptyList())
                                            .zip(response.urlScores ?: emptyList())
                                            .map { (url, score) -> UrlScore(url, score) }
                                    }
                                }
                                _state.update {
                                    Messages(
                                        it.messages.replace(
                                            EmailMessage(
                                                id = message.id,
                                                fromName = message.fromName,
                                                fromEmail = message.fromEmail,
                                                subject = message.subject,
                                                preview = message.preview,
                                                unread = message.unread,
                                                trafficLight = classification,
                                                reasons = reasons,
                                                urlScores = urlScores
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
}

private fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
    return map {
        if (block(it)) newValue else it
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
    val unread: Boolean,
    val trafficLight: TrafficLight? = null,
    val reasons: List<String> = emptyList(),
    val urlScores: List<UrlScore> = emptyList()
)
