package org.contextguard.lib.MLKit.urlClassification

import io.ktor.http.Url
import io.ktor.http.hostWithPortIfSpecified
import org.contextguard.lib.MLKit.urlClassification.models.UrlComponents

private val PUNCTUATION_REGEX = Regex("[^\\w\\s]")
private val DIGITS_REGEX = Regex("\\d")

class UrlVerifierHelper(val url: String) {

    val components: UrlComponents? = getUrlComponents(url)
    val isMalformed: Boolean = components == null

    private fun getUrlComponents(url: String): UrlComponents? {
        return try {
            val regex = Regex("^https?://[^\\s/$.?#]+.*")
            if (!regex.matches(url)) return null

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
            null
        }
    }

    fun extractUrlFeatures(): Map<String, Int> {
        if (components == null) return emptyMap()

        val featureMap = mutableMapOf<String, Int>()

        fun countMatches(input: String, regex: Regex): Int = regex.findAll(input).count()

        featureMap["url_length"] = url.length
        featureMap["url_punctuations_count"] = countMatches(url, PUNCTUATION_REGEX)
        featureMap["url_digits_count"] = countMatches(url, DIGITS_REGEX)

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
            featureMap["${key}_punctuations_count"] = countMatches(value, PUNCTUATION_REGEX)
            featureMap["${key}_digits_count"] = countMatches(value, DIGITS_REGEX)
        }

        featureMap["secured_scheme"] = if (components.scheme == "https") 1 else 0
        return featureMap
    }
}
