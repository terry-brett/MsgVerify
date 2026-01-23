package org.contextguard.lib.MLKit.urlClassification

import io.ktor.http.Url
import io.ktor.http.hostWithPortIfSpecified
import org.contextguard.lib.MLKit.urlClassification.models.UrlComponents

class UrlVerifierHelper(val url: String) {

    val components: UrlComponents? = getUrlComponents(url)
    val isMalformed: Boolean = components == null

    private fun getUrlComponents(url: String): UrlComponents? {
        return try {
            // Validate URL format
            val regex = Regex("^https?://[^\\s/$.?#]+.*")
            if (!regex.matches(url)) {
                return null
            }

            // Parse URL components
            val parsed = Url(url)
            val hostParts = parsed.host.split(".")
            val subdomain = if (hostParts.size > 2) hostParts.dropLast(2).joinToString(".") else ""
            val domain = if (hostParts.size >= 2) hostParts[hostParts.size - 2] else parsed.host
            val tld = if (hostParts.size >= 1) hostParts.last() else ""

            UrlComponents(
                subdomain = subdomain,
                domain = domain,
                tld = tld,
                path = parsed.encodedPath,
                query = parsed.encodedQuery,
                fragment = parsed.fragment,
                netloc = parsed.hostWithPortIfSpecified,
                scheme = parsed.protocol.name
            )
        } catch (e: Exception) {
            // URL parsing failed - return null so caller can add "malformed URL" as reason
            null
        }
    }

    fun extractUrlFeatures(): Map<String, Int> {
        // Return empty map if URL is malformed - caller should check components for malformed flag
        if (components == null) {
            return emptyMap()
        }

        val fullUrl = url
        val featureMap = mutableMapOf<String, Int>()

        fun countMatches(input: String, regex: Regex): Int = regex.findAll(input).count()

        featureMap["url_length"] = fullUrl.length
        featureMap["url_punctuations_count"] = countMatches(fullUrl, Regex("[^\\w\\s]"))
        featureMap["url_digits_count"] = countMatches(fullUrl, Regex("\\d"))

        val parts = listOf(
            "subdomain" to components.subdomain,
            "domain" to components.domain,
            "tld" to components.tld,
            "path" to components.path,
            "query" to components.query,
            "fragment" to components.fragment,
            "netloc" to components.netloc
        )

        for ((key, value) in parts) {
            featureMap["${key}_length"] = value.length
            featureMap["${key}_punctuations_count"] = countMatches(value, Regex("[^\\w\\s]"))
            featureMap["${key}_digits_count"] = countMatches(value, Regex("\\d"))
        }

        featureMap["secured_scheme"] = if (components.scheme == "https") 1 else 0
        return featureMap
    }
}
