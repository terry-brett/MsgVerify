package org.contextguard.lib.spellcheck

import android.content.Context
import com.darkrockstudios.libs.platformspellchecker.PlatformSpellCheckerFactory

internal actual object SpellcheckPlatform {
    private var appContext: Context? = null

    actual fun init(platformContext: Any?) {
        appContext = (platformContext as? Context)?.applicationContext
            ?: error("Spellcheck.init(context) must be called with an Android Context")
    }

    actual suspend fun isMisspelled(message: String): Boolean {
        val text = message.trim()
        if (text.isEmpty()) return false

        val ctx = appContext ?: error("Call Spellcheck.init(context) once before isMisspelled()")
        val factory = PlatformSpellCheckerFactory(ctx)

        // If the platform spellchecker is disabled/unavailable, treat as "not misspelled"
        if (!factory.isAvailable()) return false

        val checker = factory.createSpellChecker()
        val hits = checker.checkMultiword(text) // list of misspellings
        return hits.isNotEmpty()
    }
}
