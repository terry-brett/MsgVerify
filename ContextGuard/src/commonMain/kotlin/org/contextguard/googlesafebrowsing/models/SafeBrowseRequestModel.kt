package org.contextguard.googlesafebrowsing.models

import kotlinx.serialization.Serializable

/**
 * Represents the request body for the Google Safe Browse API.
 * The use of enums for threat and platform types provides type safety.
 */
@Serializable
data class SafeBrowseRequest(
    val client: ClientInfo,
    val threatInfo: ThreatInfo
)

/**
 * Information about the client making the request.
 */
@Serializable
data class ClientInfo(
    val clientId: String, // name of the app
    val clientVersion: String // app version number
)

/**
 * Contains threat and platform information for the lookup.
 */
@Serializable
data class ThreatInfo(
    val threatTypes: List<ThreatType>,
    val platformTypes: List<PlatformType>,
    val threatEntryTypes: List<ThreatEntryType>,
    val threatEntries: List<ThreatEntry>
)

/**
 * Represents a threat entry, such as a URL, that is being checked.
 */
@Serializable
data class ThreatEntry(
    val url: String
)

