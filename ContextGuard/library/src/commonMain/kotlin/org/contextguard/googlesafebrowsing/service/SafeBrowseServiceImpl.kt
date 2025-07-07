package org.contextguard.googlesafebrowsing.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import org.contextguard.googlesafebrowsing.SafeBrowseResult
import org.contextguard.googlesafebrowsing.models.ClientInfo
import org.contextguard.googlesafebrowsing.models.PlatformType
import org.contextguard.googlesafebrowsing.models.SafeBrowseRequest
import org.contextguard.googlesafebrowsing.models.SafeBrowseResponse
import org.contextguard.googlesafebrowsing.models.ThreatEntry
import org.contextguard.googlesafebrowsing.models.ThreatEntryType
import org.contextguard.googlesafebrowsing.models.ThreatInfo
import org.contextguard.googlesafebrowsing.models.ThreatType

class SafeBrowseServiceImpl(private val apiKey: String): SafeBrowseService {

    // a URL to use for testing https://testsafebrowsing.appspot.com/s/phishing.html

    companion object {
        const val SAFE_BROWSE_API_ENDPOINT = "https://safebrowsing.googleapis.com/v4/threatMatches:find"
    }

    private val httpClient = HttpClient{
        install(ContentNegotiation){
            json(Json{
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true // if new fields appear in response we ignore them
            })
        }
    }

    /**
     * Checks a list of URLs against the Google Safe Browse API to determine their safety.
     *
     * @param listOfUrls The HTTP POST request can include up to 500 URLs.
     * @return A [SafeBrowseResult] indicating the result of the check against the API.
     * - [SafeBrowseResult.Safe]: All provided URLs were determined to be safe.
     * - [SafeBrowseResult.Unsafe]: At least one URL was found to be unsafe,
     * containing a list of [org.contextguard.googlesafeBrowse.models.ThreatMatch]
     * objects detailing the threats.
     * - [SafeBrowseResult.Error]: An error occurred during the API call,
     * including network issues or invalid API responses. The [SafeBrowseResult.Error.message]
     * field provides details about the error.
     */
    override suspend fun checkUrls(listOfUrls: List<String>): SafeBrowseResult {
        val urlsToCheck = listOfUrls.toThreatEntryList()
       val requestBody = SafeBrowseRequest(
           client = ClientInfo(
               clientId = "contextguard",
               clientVersion = "1.0.0"
           ),
           threatInfo = ThreatInfo(
               threatTypes = ThreatType.entries,
               platformTypes = PlatformType.entries,
               threatEntryTypes = ThreatEntryType.entries,
               threatEntries = urlsToCheck
           )
       )

        try {
            val response = httpClient.post(
                SAFE_BROWSE_API_ENDPOINT
            ){
                contentType(ContentType.Application.Json)
                setBody(requestBody)
                url {
                    parameters.append("key", apiKey)
                }
            }

            return when(response.status){
                HttpStatusCode.OK -> {
                    val safeBrowseResponse: SafeBrowseResponse = response.body()
                    if (safeBrowseResponse.matches.isNullOrEmpty()){
                        SafeBrowseResult.Safe
                    } else {
                        SafeBrowseResult.Unsafe(
                            threatTypes = safeBrowseResponse.matches
                        )
                    }
                }
                else -> {
                    SafeBrowseResult.Error(
                        message = "Error checking URL: ${response.status.value}"
                    )
                }
            }

        } catch (e: Exception){
            return SafeBrowseResult.Error(message = e.message ?: "Something went wrong")
        }

    }
}

private fun List<String>.toThreatEntryList(): List<ThreatEntry> {
    return this.map { url ->
        ThreatEntry(url = url)
    }
}
