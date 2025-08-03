package com.terrydroid.msgverify

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect class PlatformContext {
    fun getNativeContext(): Any
}
