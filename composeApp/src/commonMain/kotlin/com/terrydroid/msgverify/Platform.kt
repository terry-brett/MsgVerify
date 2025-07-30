package com.terrydroid.msgverify

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect object AppContext