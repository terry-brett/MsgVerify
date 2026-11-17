package com.terrydroid.msgverify.benchmark

import com.terrydroid.msgverify.demo.readMock
import kotlinx.serialization.Serializable

/** One benchmark input, taken from the bundled demo corpus in `mocks/`. */
data class BenchmarkMessage(
    /** Stable identifier, identical on every platform, used to pair measurements. */
    val id: String,
    val channel: String,
    val label: String,
    val text: String,
    val sender: String,
)

@Serializable private data class CorpusItem(
    val message: String,
    val sender: String = "",
    val subject: String = "",
)

private val CHANNELS = listOf(
    "sms" to "sms.json",
    "email" to "emails.json",
    "social" to "social_media_messages.json",
)

/**
 * Loads the channel-balanced corpus (30 messages per channel) in a fixed order:
 * SMS, email, social media; malicious before safe within each channel; file order within each file.
 * The order is the same on Android and iOS so that runs can be compared message by message.
 */
suspend fun loadBenchmarkCorpus(): List<BenchmarkMessage> = buildList {
  for ((channel, file) in CHANNELS) {
    for (label in listOf("malicious", "safe")) {
      val items: List<CorpusItem> = readMock("files/mocks/$label/$file")
      items.forEachIndexed { index, item ->
        add(
            BenchmarkMessage(
                id = "$channel-$label-${(index + 1).toString().padStart(2, '0')}",
                channel = channel,
                label = label,
                // Same inputs the demo screens pass to the verifier: the message body, and the
                // sender for emails.
                text = item.message,
                sender = item.sender,
            )
        )
      }
    }
  }
}
