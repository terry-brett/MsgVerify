package org.contextguard.googlesafebrowsing

/**
 * This represents the types of threats returned by identifying the URL
 * https://developers.google.com/safe-browsing/reference/rest/v4/ThreatType
 */

enum class ThreatType{
    THREAT_TYPE_UNSPECIFIED,
    MALWARE,
    SOCIAL_ENGINEERING,
    UNWANTED_SOFTWARE,
    POTENTIALLY_HARMFUL_APPLICATION,
    // Any other types if the API introduces them in the future
    UNKNOWN
}
