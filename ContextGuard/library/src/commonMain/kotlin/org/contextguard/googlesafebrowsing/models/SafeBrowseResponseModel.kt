package org.contextguard.googlesafebrowsing.models

import kotlinx.serialization.Serializable

@Serializable
data class SafeBrowseResponse(
    val matches: List<ThreatMatch>? = null
)

@Serializable
data class ThreatMatch(
    val threatType: ThreatType,
    val platformType: PlatformType,
    val threatEntryType: ThreatEntryType,
    val threat: ThreatEntry,
    val cacheDuration: String
)
