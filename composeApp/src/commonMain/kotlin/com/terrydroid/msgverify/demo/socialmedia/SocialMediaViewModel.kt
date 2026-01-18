package com.terrydroid.msgverify.demo.socialmedia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrydroid.msgverify.data.ContentVerificationResponse
import com.terrydroid.msgverify.data.MsgVerifyRepository
import com.terrydroid.msgverify.demo.smsoverview.TrafficLight
import com.terrydroid.msgverify.demo.smsoverview.UrlScore
import com.terrydroid.msgverify.demo.smsoverview.getClassification
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SocialMediaViewModel(
    private val msgVerifyRepository: MsgVerifyRepository,
    dispatcher: CoroutineDispatcher
): ViewModel() {
    private val _state: MutableStateFlow<Messages> = MutableStateFlow(getMessagesMockData())

    val state: StateFlow<Messages>
        get() = _state.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            state.value.messages.forEach { message ->
                msgVerifyRepository.verifyContent(message.message, message.title).collect { result ->
                    result.fold(
                        onFailure = {},
                        onSuccess = { response ->
                            val maliciousScorePercent: Float
                            val reasons: List<String>
                            val urlScores: List<UrlScore>

                            when (response) {
                                is ContentVerificationResponse.Safe -> {
                                    maliciousScorePercent = 0f
                                    reasons = emptyList()
                                    urlScores = emptyList()
                                }
                                is ContentVerificationResponse.Unsafe -> {
                                    maliciousScorePercent = response.urlScores?.maxOrNull() ?: 0f
                                    reasons = response.reasons.map { it.reason }
                                    urlScores = (response.extractedUrls ?: emptyList())
                                        .zip(response.urlScores ?: emptyList())
                                        .map { (url, score) -> UrlScore(url, score) }
                                }
                            }

                            val classification = getClassification(maliciousScorePercent)
                            _state.update {
                                Messages(
                                    it.messages.replace(
                                        Message(
                                            id = message.id,
                                            title = message.title,
                                            message = message.message,
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
    val reasons: List<String> = emptyList(),
    val urlScores: List<UrlScore> = emptyList()
)

private fun getMessagesMockData(): Messages {
    return Messages(
        messages = listOf(
            Message(
                id = 1,
                title = "OsloWeatherBot",
                message = "FYI: Roads are slick this morning. Drive safe and leave extra time 🧊🚗",
            ),
            Message(
                id = 2,
                title = "CryptoBroDaily",
                message = "I turned 1,000 into 50,000 in a week. DM me for the 'secret method' 🔥",
            ),
            Message(
                id = 3,
                title = "BankHelpSupport",
                message = "URGENT: Your account is locked. Verify now at bank-login-secure.example to avoid closure.",
            ),
            Message(
                id = 4,
                title = "NewsHotTake",
                message = "Everyone who disagrees with me is paid off. They should be 'taught a lesson' soon.",
            ),
            Message(
                id = 5,
                title = "GiveawayCentral",
                message = "CONGRATS 🎉 You won! Send your phone number + address to claim your prize today!",
            ),
        )
    )
}
