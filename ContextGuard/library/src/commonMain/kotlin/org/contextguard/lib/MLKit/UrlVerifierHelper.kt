package org.contextguard.lib.MLKit

import io.ktor.http.Url
import io.ktor.http.formUrlEncode
import org.contextguard.lib.MLKit.models.UrlComponents

class UrlVerifierHelper(val url: String) {

    val components: UrlComponents = getUrlComponents(url)


    init {
        validateUrl()
    }

    private fun validateUrl(){
        val regex = Regex("https?://[^\\s/\$.?#].[^\\s]*")
        if (!regex.matches(url)){
            throw IllegalArgumentException("Url is not set or valid")
        }
    }
    private fun getUrlComponents(url: String) : UrlComponents {
        val parsed = Url(url)
        val hostParts = parsed.host.split(".")
        val subdomain = if (hostParts.size > 2) hostParts.dropLast(2).joinToString(".") else ""
        val domain = if (hostParts.size >= 2) hostParts[hostParts.size - 2] else parsed.host
        val tld = if (hostParts.size >= 1) hostParts.last() else ""

        return UrlComponents(
            subdomain = subdomain,
            domain = domain,
            tld = tld,
            path = parsed.encodedPath,
            query = parsed.parameters.formUrlEncode(),
            fragment = parsed.fragment ?: "",
            netloc = parsed.host + if (parsed.port != -1) ":${parsed.port}" else "",
            scheme = parsed.protocol.name
        )
    }

    fun isSchemeSecured(): Boolean {
        return components.scheme == "https"
    }

    fun extractUrlFeatures(): Map<String, Int> {
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