package org.contextguard.lib.spellcheck

object Spellcheck {
    /**
     * Android: call once with a Context (Application context recommended).
     * iOS: no-op.
     */
    fun init(platformContext: Any? = null) = SpellcheckPlatform.init(platformContext)

    /**
     * Returns true if the message contains at least one misspelled token.
     * Returns false if spellcheck is unavailable or message is blank.
     */
    suspend fun isMisspelled(message: String): Boolean = SpellcheckPlatform.isMisspelled(message)
}

internal expect object SpellcheckPlatform {
    fun init(platformContext: Any? = null)
    suspend fun isMisspelled(message: String): Boolean
}
