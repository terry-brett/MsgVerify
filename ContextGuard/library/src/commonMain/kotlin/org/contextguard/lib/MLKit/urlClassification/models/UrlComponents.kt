package org.contextguard.lib.MLKit.urlClassification.models

data class UrlComponents(
    val subdomain: String,
    val domain: String,
    val tld: String,
    val path: String,
    val query: String,
    val fragment: String,
    val netloc: String,
    val scheme: String
)
