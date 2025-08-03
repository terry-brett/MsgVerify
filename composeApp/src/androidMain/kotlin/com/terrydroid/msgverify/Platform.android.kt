package com.terrydroid.msgverify

import android.app.Application
import android.content.Context
import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual class PlatformContext(private val androidContext: Context) {
    actual fun getNativeContext(): Any = androidContext
}