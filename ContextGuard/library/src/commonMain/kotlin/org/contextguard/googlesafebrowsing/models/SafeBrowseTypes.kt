package org.contextguard.googlesafebrowsing.models

import kotlinx.serialization.Serializable

/**
 * Enum representing the types of threats.
 * The names correspond directly to the string values used by the Google Safe Browse API.
 * https://developers.google.com/safe-browsing/v4/reference/rest/v4/ThreatType
 */
@Serializable
enum class ThreatType {
    THREAT_TYPE_UNSPECIFIED,
    MALWARE,
    SOCIAL_ENGINEERING,
    UNWANTED_SOFTWARE,
    POTENTIALLY_HARMFUL_APPLICATION
}

/**
 * Enum representing the platforms to check for threats.
 */
@Serializable
enum class PlatformType {
    WINDOWS, // Threat posed to Windows
    LINUX, // Threat posed to Linux
    ANDROID, // Threat posed to Android
    OSX, // Threat posed to OS X
    IOS, // Threat posed to iOS
    ANY_PLATFORM, // Threat posed to at least one of the defined platforms
    ALL_PLATFORMS, // Threat posed to all defined platforms
    CHROME, // Threat posed to Chrome
    PLATFORM_TYPE_UNSPECIFIED, // Unknown platform
}

/**
 * Enum representing the types of entries that pose threats.
 */
@Serializable
enum class ThreatEntryType {
    THREAT_ENTRY_TYPE_UNSPECIFIED, // Unspecified
    URL,
    EXECUTABLE, // An executable program
}

