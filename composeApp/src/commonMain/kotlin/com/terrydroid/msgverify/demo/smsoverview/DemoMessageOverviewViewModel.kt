package com.terrydroid.msgverify.demo.smsoverview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrydroid.msgverify.data.ContentVerificationResponse
import com.terrydroid.msgverify.data.MsgVerifyRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DemoMessageOverviewViewModel(
    private val msgVerifyRepository: MsgVerifyRepository,
    dispatcher: CoroutineDispatcher,
) : ViewModel() {
  private val _state: MutableStateFlow<Messages> = MutableStateFlow(Messages(emptyList()))

  val state: StateFlow<Messages>
    get() = _state.asStateFlow()

  init {
    viewModelScope.launch {
      val mockData = getMessagesMockdata()
      _state.value = mockData
    }

    viewModelScope.launch {
      state.collect { messages ->
        messages.messages.forEach { message ->
          msgVerifyRepository.verifyContent(message.message, message.title).collect { result ->
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
                            Message(
                                id = message.id,
                                title = message.title,
                                message = message.message,
                                trafficLight = classification,
                                reasons = reasons,
                                urlScores = urlScores,
                            )
                        ) { value ->
                          value == message
                        }
                    )
                  }
                },
            )
          }
        }
      }
    }
  }
}

private fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
  return map { if (block(it)) newValue else it }
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

