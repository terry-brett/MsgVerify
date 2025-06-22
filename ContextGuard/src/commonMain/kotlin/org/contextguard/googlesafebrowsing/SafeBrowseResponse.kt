package org.contextguard.googlesafebrowsing

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchHashesRequest(
    @SerialName("hashPrefixes")
    val hashPrefixes: List<String> // Base64-encoded SHA256 hash prefixes
)

@Serializable
data class SearchHashesResponse(
    @SerialName("matches")
    val matches: List<FullHashDetail>? = null,
    @SerialName("cacheDuration")
    val cacheDuration: String? = null
)

@Serializable
data class FullHashDetail(
    @SerialName("fullHash")
    val fullHash: String, // The matching full SHA256 hash (Base64-encoded, 32 bytes)
    @SerialName("threatType")
    val threatType: String, // e.g., "MALWARE", "SOCIAL_ENGINEERING"
    @SerialName("platformType")
    val platformType: String? = null, // e.g., "WINDOWS", "ANDROID"
    @SerialName("threatEntryType")
    val threatEntryType: String? = null, // e.g., "URL"
    @SerialName("threatAttributes")
    val threatAttributes: List<String>? = null
)
