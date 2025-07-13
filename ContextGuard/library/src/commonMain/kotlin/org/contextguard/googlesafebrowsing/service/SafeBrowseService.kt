package org.contextguard.googlesafebrowsing.service

import org.contextguard.googlesafebrowsing.service.SafeBrowseResult

interface SafeBrowseService {
    suspend fun checkUrls(listOfUrls: List<String>): SafeBrowseResult
}
