package org.contextguard.lib.spellcheck

import com.darkrockstudios.libs.platformspellchecker.PlatformSpellCheckerFactory

internal actual object SpellcheckPlatform {
    actual fun init(platformContext: Any?) {
        // no-op on iOS
    }

    actual suspend fun isMisspelled(message: String): Boolean {
        val text = message.trim()
        if (text.isEmpty()) return false

        val factory = PlatformSpellCheckerFactory()
        if (!factory.isAvailable()) return false

        val checker = factory.createSpellChecker()
        val hits = checker.checkMultiword(text)
        return hits.isNotEmpty()
    }
}
