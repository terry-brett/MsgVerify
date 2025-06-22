package org.contextguard.googlesafebrowsing.service

import org.contextguard.googlesafebrowsing.SafeBrowseResult

interface SafeBrowseService {
    suspend fun checkUrl(url: String): SafeBrowseResult
}
