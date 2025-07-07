package org.contextguard.googlesafebrowsing.service

import org.contextguard.googlesafebrowsing.SafeBrowseResult

interface SafeBrowseService {
    suspend fun checkUrls(listOfUrls: List<String>): SafeBrowseResult
}
