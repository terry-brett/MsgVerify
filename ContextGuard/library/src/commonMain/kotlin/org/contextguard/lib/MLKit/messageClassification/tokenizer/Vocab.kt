package org.contextguard.lib.MLKit.messageClassification.tokenizer

private var cachedVocab: Map<String, Int>? = null

suspend fun loadVocab(): Map<String, Int> {
    cachedVocab?.let { return it }

    val content = loadVocabFile()

    val vocab = mutableMapOf<String, Int>()
    content.lines().forEachIndexed { index, token ->
        if (token.isNotBlank()) vocab[token] = index
    }
    cachedVocab = vocab
    return vocab
}
