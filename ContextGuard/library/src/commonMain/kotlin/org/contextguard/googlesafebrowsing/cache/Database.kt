package org.contextguard.googlesafebrowsing.cache

import org.contextguard.googlesafebrowsing.models.PlatformType
import org.contextguard.googlesafebrowsing.models.ThreatEntry
import org.contextguard.googlesafebrowsing.models.ThreatEntryType
import org.contextguard.googlesafebrowsing.models.ThreatMatch
import org.contextguard.googlesafebrowsing.models.ThreatType
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class SafeBrowseCache(databaseDriverFactory: DatabaseDriverFactory){
    private val database = SafeBrowseDb(databaseDriverFactory.createDriver())
    private val dbQuery = database.safeBrowseDbQueries

    fun getCachedThreatMatch(url: String): ThreatMatch? {
        return dbQuery.selectByUrl(url)
            .executeAsOneOrNull()
            ?.let { cachedUrl ->
                // Check if cache entry is expired
                if (cachedUrl.cacheExpiryEpochSeconds > currentTimeSeconds()) {
                    ThreatMatch(
                        threatType = ThreatType.valueOf(cachedUrl.threatType),
                        platformType = PlatformType.valueOf(cachedUrl.platformType),
                        threatEntryType = ThreatEntryType.valueOf(cachedUrl.threatEntryType),
                        threat = ThreatEntry(url = cachedUrl.url),
                        cacheDuration = (cachedUrl.cacheExpiryEpochSeconds - currentTimeSeconds()).toString() + ".000s" // Recalculate remaining duration
                    )
                } else {
                    null // Cache expired
                }
            }
    }

    fun cacheThreatMatch(threatMatch: ThreatMatch) {
        val cacheDurationSeconds = threatMatch.cacheDuration.removeSuffix("s").toDoubleOrNull()?.toLong() ?: 0L
        val expiryTime = currentTimeSeconds() + cacheDurationSeconds

        dbQuery.insertOrReplace(
            url = threatMatch.threat.url,
            threatType = threatMatch.threatType.name,
            platformType = threatMatch.platformType.name,
            threatEntryType = threatMatch.threatEntryType.name,
            cacheExpiryEpochSeconds = expiryTime
        )
    }

    fun cleanExpiredCache() {
        dbQuery.deleteExpired(currentTimeSeconds())
    }

    @OptIn(ExperimentalTime::class)
    private fun currentTimeSeconds(): Long {
        return (Clock.System.now().epochSeconds / 1000)
    }
}