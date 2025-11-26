package org.contextguard.lib.MLKit.messageClassification.tokenizer

import org.contextguard.lib.library.generated.resources.Res

actual suspend fun loadVocabFile(): String {
    val bytes = Res.readBytes("files/vocab.txt")
    return bytes.decodeToString()
}
