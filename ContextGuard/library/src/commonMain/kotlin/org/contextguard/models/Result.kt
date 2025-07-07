package org.contextguard.models

/**
 * The result represents a score given to analysed message
 * @param score: Score out of 10 which specifies confidence of message being safe
 * @param reasons: List of reasons for why the score is given
 */
data class Result(
    val score: Int,
    val reasons: List<Reason>?
)
