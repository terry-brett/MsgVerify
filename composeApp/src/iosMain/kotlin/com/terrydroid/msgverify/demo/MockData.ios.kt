package com.terrydroid.msgverify.demo

import msgverify.composeapp.generated.resources.Res

actual suspend fun readMockJsonText(path: String): String {
  // Examples:
  // "mocks/safe/emails.json"
  // "mocks/malicious/sms.json"
  return Res.readBytes(path).decodeToString()
}
