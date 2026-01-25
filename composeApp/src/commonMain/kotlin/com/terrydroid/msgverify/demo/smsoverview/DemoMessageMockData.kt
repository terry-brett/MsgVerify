package com.terrydroid.msgverify.demo.smsoverview

import com.terrydroid.msgverify.demo.readMock
import kotlinx.serialization.Serializable

@Serializable private data class MockSmsItem(val message: String)

internal suspend fun getMessagesMockdata(): Messages {
  val maliciousSmsRaw: List<MockSmsItem> = readMock("files/mocks/malicious/sms.json")
  val safeSmsRaw: List<MockSmsItem> = readMock("files/mocks/safe/sms.json")

  val maliciousSms =
      maliciousSmsRaw.mapIndexed { index, item ->
        Message(id = index * 2, title = "Message #${index * 2 + 1}", message = item.message)
      }

  val safeSms =
      safeSmsRaw.mapIndexed { index, item ->
        Message(
            id = index * 2 + 1,
            title = "Message #${index * 2 + 2}",
            message = item.message,
        )
      }

  // Interleave malicious and safe messages
  val allMessages = mutableListOf<Message>()
  for (i in 0 until maxOf(maliciousSms.size, safeSms.size)) {
    if (i < maliciousSms.size) allMessages.add(maliciousSms[i])
    if (i < safeSms.size) allMessages.add(safeSms[i])
  }

  // Reassign IDs to be sequential after interleaving
  val reorderedMessages = allMessages.mapIndexed { index, message ->
    message.copy(id = index, title = "Message #${index + 1}")
  }

  return Messages(reorderedMessages)
}

internal suspend fun getMessage(id: Int): Message? {
  return getMessagesMockdata().messages.find { it.id == id }
}
