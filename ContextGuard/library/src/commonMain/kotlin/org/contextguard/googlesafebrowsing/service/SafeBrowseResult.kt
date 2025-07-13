package org.contextguard.googlesafebrowsing.service

import org.contextguard.googlesafebrowsing.models.ThreatMatch

sealed class SafeBrowseResult {
    object Safe : SafeBrowseResult()
    data class Unsafe(val threatTypes: List<ThreatMatch>) : SafeBrowseResult()
    data class Error(val message: String) : SafeBrowseResult()
}