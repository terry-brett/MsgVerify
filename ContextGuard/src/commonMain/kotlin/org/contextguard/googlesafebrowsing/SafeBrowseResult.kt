package org.contextguard.googlesafebrowsing

sealed class SafeBrowseResult {
    object Safe : SafeBrowseResult()
    data class Unsafe(val threatTypes: List<ThreatType>) : SafeBrowseResult()
    data class Error(val message: String) : SafeBrowseResult()
}

