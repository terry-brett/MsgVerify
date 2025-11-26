package org.contextguard.lib.MLKit.messageClassification.tokenizer

class WordPieceTokenizer(
    vocab: Map<String, Int>,
    private val unkToken: String = "[UNK]",
    private val clsToken: String = "[CLS]",
    private val sepToken: String = "[SEP]",
    private val padToken: String = "[PAD]",
    private val maskToken: String = "[MASK]"
) {
    private val vocabMap = vocab
    private val unkId = vocabMap[unkToken] ?: error("[UNK] not in vocab")
    private val clsId = vocabMap[clsToken] ?: error("[CLS] not in vocab")
    private val sepId = vocabMap[sepToken] ?: error("[SEP] not in vocab")
    private val padId = vocabMap[padToken] ?: error("[PAD] not in vocab")
    private val maskId = vocabMap[maskToken] ?: error("[MASK] not in vocab")

    private fun tokenizeWord(word: String): List<String> {
        if (word.isEmpty()) return emptyList()

        val subTokens = mutableListOf<String>()
        var start = 0

        while (start < word.length) {
            var end = word.length
            var found: String? = null

            while (start < end) {
                val piece = if (start == 0) {
                    word.substring(start, end)
                } else {
                    "##" + word.substring(start, end)
                }

                if (vocabMap.containsKey(piece)) {
                    found = piece
                    break
                }
                end -= 1
            }

            if (found == null) return listOf(unkToken)

            subTokens.add(found)
            start = end
        }

        return subTokens
    }

    private fun tokenizeSentence(text: String): List<String> {
        val cleaned = text.lowercase().trim()

        val tokensWithPunctuation = mutableListOf<String>()

        val puncPattern = "([.,!?;:\\\"()])".toRegex()

        val words = cleaned.split("\\s+".toRegex()).filter { it.isNotEmpty() }

        for (word in words) {
            val pieces = word.replace(puncPattern, " $1 ")

            tokensWithPunctuation.addAll(pieces.split(' ').filter { it.isNotEmpty() })
        }

        return tokensWithPunctuation.flatMap { tokenizeWord(it) }
    }

    /**
     * Encode a pair (premise + hypothesis) to input_ids + attention_mask
     */
    fun encode(premise: String, hypothesis: String, maxLen: Int): Pair<IntArray, IntArray> {

        val tokens1 = tokenizeSentence(premise)
        val tokens2 = tokenizeSentence(hypothesis)

        val tokens = mutableListOf<String>()

        // [CLS] premise [SEP] hypothesis [SEP]
        tokens.add(clsToken)
        tokens.addAll(tokens1)
        tokens.add(sepToken)
        tokens.addAll(tokens2)
        tokens.add(sepToken)

        // Convert tokens to initial IDs
        val initialInputIds = tokens.map { token ->
            vocabMap[token] ?: unkId
        }

        val truncatedInputIds = if (initialInputIds.size > maxLen) {
            initialInputIds.take(maxLen)
        } else {
            initialInputIds
        }

        val paddingLength = maxLen - truncatedInputIds.size
        val paddingIds = List(paddingLength) { padId }

        val finalInputIds = (truncatedInputIds + paddingIds).toIntArray()

        val originalMask = List(truncatedInputIds.size) { 1 }
        val paddingMask = List(paddingLength) { 0 }
        val finalAttentionMask = (originalMask + paddingMask).toIntArray()

        return finalInputIds to finalAttentionMask
    }
}
