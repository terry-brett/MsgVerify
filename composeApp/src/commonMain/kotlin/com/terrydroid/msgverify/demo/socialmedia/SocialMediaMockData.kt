package com.terrydroid.msgverify.demo.socialmedia

import com.terrydroid.msgverify.demo.readMock
import kotlinx.serialization.Serializable

@Serializable
private data class MockSocialMediaItem(
    val message: String
)

internal suspend fun getSocialMediaMockdata(): Messages {
    val maliciousSocialMediaRaw: List<MockSocialMediaItem> = readMock("files/mocks/malicious/social_media_messages.json")
    val safeSocialMediaRaw: List<MockSocialMediaItem> = readMock("files/mocks/safe/social_media_messages.json")

    // If the mock data is empty, use default malicious social media messages
    val maliciousSocialMedia = if (maliciousSocialMediaRaw.isEmpty() || maliciousSocialMediaRaw.all { it.message.isEmpty() }) {
        listOf(
            Message(
                id = 0,
                title = "Post #1",
                message = "I turned 1,000 into 50,000 in a week. DM me for the 'secret method' 🔥",
            ),
            Message(
                id = 1,
                title = "Post #2",
                message = "URGENT: Your account is locked. Verify now at bank-login-secure.example to avoid closure.",
            ),
            Message(
                id = 2,
                title = "Post #3",
                message = "CONGRATS 🎉 You won! Send your phone number + address to claim your prize today!",
            ),
        )
    } else {
        maliciousSocialMediaRaw.mapIndexed { index, item ->
            Message(
                id = index,
                title = "Post #${index + 1}",
                message = item.message
            )
        }
    }

    val safeSocialMedia = if (safeSocialMediaRaw.isEmpty() || safeSocialMediaRaw.all { it.message.isEmpty() }) {
        listOf(
            Message(
                id = maliciousSocialMedia.size,
                title = "Post #${maliciousSocialMedia.size + 1}",
                message = "FYI: Roads are slick this morning. Drive safe and leave extra time 🧊🚗",
            ),
            Message(
                id = maliciousSocialMedia.size + 1,
                title = "Post #${maliciousSocialMedia.size + 2}",
                message = "Great article on climate solutions. Link in bio.",
            ),
        )
    } else {
        safeSocialMediaRaw.mapIndexed { index, item ->
            Message(
                id = maliciousSocialMedia.size + index,
                title = "Post #${maliciousSocialMedia.size + index + 1}",
                message = item.message
            )
        }
    }

    return Messages(maliciousSocialMedia + safeSocialMedia)
}
