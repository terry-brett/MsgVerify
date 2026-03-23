package com.terrydroid.msgverify

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable data object Home : Route
    @Serializable data object DemoOverview : Route
    @Serializable data object DemoSmsOverview : Route
    @Serializable data class DemoSmsDetails(val id: Int) : Route
    @Serializable data object DemoEmailOverview : Route
    @Serializable data class DemoEmailDetails(val id: Int) : Route
    @Serializable data object SocialMediaDemo : Route
}
