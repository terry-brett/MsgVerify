package com.terrydroid.msgverify.demo.emailoverview

import com.terrydroid.msgverify.demo.readMock
import com.terrydroid.msgverify.demo.smsoverview.TrafficLight
import kotlinx.serialization.Serializable

@Serializable
private data class MockEmailItem(
    val sender: String,
    val subject: String,
    val message: String
)

internal suspend fun getEmailMockdata(): Messages {
    val maliciousEmailsRaw: List<MockEmailItem> = readMock("files/mocks/malicious/emails.json")
    val safeEmailsRaw: List<MockEmailItem> = readMock("files/mocks/safe/emails.json")

    val maliciousEmails = maliciousEmailsRaw.filter { it.sender.isNotBlank() && it.message.isNotBlank() }.mapIndexed { index, item ->
        EmailMessage(
            id = index,
            fromName = item.sender.substringBefore("@"),
            fromEmail = item.sender,
            subject = item.subject,
            preview = item.message.take(100) + if (item.message.length > 100) "..." else "",
            body = item.message,
            unread = index % 2 == 0,
            trafficLight = null,
            reasons = emptyList(),
            urlScores = emptyList()
        )
    }

    val safeEmails = safeEmailsRaw.filter { it.sender.isNotBlank() && it.message.isNotBlank() }.mapIndexed { index, item ->
        EmailMessage(
            id = maliciousEmails.size + index,
            fromName = item.sender.substringBefore("@"),
            fromEmail = item.sender,
            subject = item.subject,
            preview = item.message.take(100) + if (item.message.length > 100) "..." else "",
            body = item.message,
            unread = (maliciousEmails.size + index) % 2 == 0,
            trafficLight = null,
            reasons = emptyList(),
            urlScores = emptyList()
        )
    }

    return Messages(maliciousEmails + safeEmails)
}

internal suspend fun getEmail(id: Int): EmailMessage? {
    return getEmailMockdata().messages.find { it.id == id }
}
