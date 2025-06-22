package org.contextguard.googlesafebrowsing.service

import io.ktor.client.HttpClient
import org.contextguard.googlesafebrowsing.SafeBrowseResult

class SafeBrowseServiceImpl(private val apiKey: String): SafeBrowseService {

    init {

    }

    override suspend fun checkUrl(url: String): SafeBrowseResult {
        TODO("Not yet implemented")
    }
}