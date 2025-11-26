package org.contextguard.lib.MLKit.messageClassification.tokenizer

suspend fun loadVocab(): Map<String, Int> {
    val content = loadVocabFile()

    val vocab = mutableMapOf<String, Int>()
    content.lines().forEachIndexed { index, token ->
        if (token.isNotBlank()) vocab[token] = index
    }
    return vocab
}
