package com.terrydroid.msgverify.demo

import kotlinx.serialization.json.Json

val json = Json { ignoreUnknownKeys = true }

expect suspend fun readMockJsonText(path: String): String

suspend inline fun <reified T> readMock(path: String): T {
  val text = readMockJsonText(path)
  return json.decodeFromString(text)
}
