package org.contextguard.googlesafebrowsing.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query
import org.contextguard.googlesafebrowsing.SearchHashesRequest
import org.contextguard.googlesafebrowsing.SearchHashesResponse


const val SAFE_BROWSE_V5_BASE_URL = "https://safeBrowse.googleapis.com/v5/"

interface SafeBrowseApi {

    @POST("hashes:search")
    @Headers("Content-Type: application/json")
    suspend fun searchHashes(
        @Body request: SearchHashesRequest,
        @Query("key") apiKey: String
    ) : SearchHashesResponse

}
