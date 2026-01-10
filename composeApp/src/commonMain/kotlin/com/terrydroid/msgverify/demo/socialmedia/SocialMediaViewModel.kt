package com.terrydroid.msgverify.demo.socialmedia

import androidx.lifecycle.ViewModel
import com.terrydroid.msgverify.data.MsgVerifyRepository
import com.terrydroid.msgverify.demo.smsoverview.TrafficLight
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SocialMediaViewModel constructor(
    private val msgVerifyRepository: MsgVerifyRepository,
    dispatcher: CoroutineDispatcher
): ViewModel() {
    private val _state: MutableStateFlow<Messages> = MutableStateFlow(getMessagesMockData())

    val state: StateFlow<Messages>
        get() = _state.asStateFlow()

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
