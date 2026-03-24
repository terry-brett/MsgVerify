package org.contextguard.lib.spellcheck

import android.content.Context
import com.darkrockstudios.libs.platformspellchecker.PlatformSpellChecker
import com.darkrockstudios.libs.platformspellchecker.PlatformSpellCheckerFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal actual object SpellcheckPlatform {
    private var appContext: Context? = null

    // Cached checker; only ever accessed from the main thread via withContext(Dispatchers.Main).
    private var cachedChecker: PlatformSpellChecker? = null

    actual fun init(platformContext: Any?) {
        appContext = (platformContext as? Context)?.applicationContext
    }

    actual suspend fun isMisspelled(message: String): Boolean {
        if (message.isBlank()) return false
        val ctx = appContext ?: return false

        // createSpellChecker() and checkMultiword() are both suspend funs that internally
        // create a Handler via TextServicesManager.newSpellCheckerSession(), which requires
        // a Looper. Dispatching to Main ensures they always run on a Looper thread.
        return try {
            withContext(Dispatchers.Main) {
                val checker = cachedChecker ?: run {
                    val factory = PlatformSpellCheckerFactory(ctx)
                    if (!factory.isAvailable()) return@withContext false
                    factory.createSpellChecker().also { cachedChecker = it }
                }
                checker.checkMultiword(message).isNotEmpty()
            }
        } catch (e: Exception) {
            false
        }
    }
}
