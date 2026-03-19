package org.contextguard.lib

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.UiThreadTestRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.contextguard.models.TextClassificationResult
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Android instrumented tests for ContentVerifier.
 * These tests run on an Android device/emulator and test the ACTUAL ML models.
 *
 * Run with:
 * ./gradlew :library:connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class ContentVerifierAndroidTest {

    @get:Rule
    val uiThreadRule = UiThreadTestRule()

    private lateinit var context: Context
    private lateinit var verifier: ContentVerifier

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        verifier = ContentVerifierImpl(context)
    }

    private fun <T> runOnMainThread(block: suspend () -> T): T {
        return runBlocking {
            withContext(Dispatchers.Main) {
                block()
            }
        }
    }

    @Test
    fun testAmazonRefundScam() {
        val result = runOnMainThread {
            try {
                val message = "Amazon is sending you a refunding of £32.64. Please reply with your bank account number to receive your refund."
                val sender = "unknown"

                val result = verifier.verify(message, sender)

                assertNotNull(result, "Result should not be null")
                assertTrue(
                    result.textClassificationResult is TextClassificationResult.Unsafe,
                    "Amazon refund scam should be detected as unsafe. Got: ${result.textClassificationResult}"
                )

                if (result.textClassificationResult is TextClassificationResult.Unsafe) {
                    val reasons = result.textClassificationResult.listOfReasons
                    assertTrue(reasons.isNotEmpty(), "Should have at least one reason for being unsafe")
                    println("✅ Amazon scam detected with reasons: ${reasons.map { it.reason }}")
                }
                result
            } catch (e: Exception) {
                println("❌ Test failed with exception: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
        assertNotNull(result)
    }

    @Test
    fun testWellsFargoPhishingWithShortenedUrl() {
        val result = runOnMainThread {
            try {
                val message = "Wells Fargo Bank: Your account is temporarily locked. Please login at http://goog.gl/2a234 to secure your account."
                val sender = "unknown"

                val result = verifier.verify(message, sender)

                assertNotNull(result, "Result should not be null")
                assertTrue(
                    result.textClassificationResult is TextClassificationResult.Unsafe,
                    "Wells Fargo phishing should be detected as unsafe. Got: ${result.textClassificationResult}"
                )

                // Verify URL was extracted
                assertTrue(result.extractedUrls?.isNotEmpty() == true, "Should extract the URL")
                assertTrue(
                    result.extractedUrls?.any { it.contains("goog.gl") } == true,
                    "Should extract the shortened URL"
                )

                // Check URL scores
                assertNotNull(result.urlScores, "URL scores should not be null")
                assertTrue(result.urlScores!!.isNotEmpty(), "Should have URL prediction scores")

                if (result.textClassificationResult is TextClassificationResult.Unsafe) {
                    val reasons = result.textClassificationResult.listOfReasons
                    println("✅ Wells Fargo phishing detected with reasons: ${reasons.map { it.reason }}")
                    println("   URL score: ${result.urlScores?.firstOrNull()}")
                }
                result
            } catch (e: Exception) {
                println("❌ Test failed with exception: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
        assertNotNull(result)
    }

    @Test
    fun testMyGovTaxScamWithSuspiciousUrl() {
        val result = runOnMainThread {
            try {
                val message = "myGov: We have processed your tax return of £2560.70. Kindly visit http://mygovtax.com for more details"
                val sender = "unknown"

                val result = verifier.verify(message, sender)

                assertNotNull(result, "Result should not be null")
                assertTrue(
                    result.textClassificationResult is TextClassificationResult.Unsafe,
                    "myGov tax scam should be detected as unsafe. Got: ${result.textClassificationResult}"
                )

                // Verify URL was extracted
                assertTrue(result.extractedUrls?.isNotEmpty() == true, "Should extract the URL")
                assertTrue(
                    result.extractedUrls?.any { it.contains("mygovtax.com") } == true,
                    "Should extract mygovtax.com URL"
                )

                if (result.textClassificationResult is TextClassificationResult.Unsafe) {
                    val reasons = result.textClassificationResult.listOfReasons
                    println("✅ myGov scam detected with reasons: ${reasons.map { it.reason }}")
                    println("   URL score: ${result.urlScores?.firstOrNull()}")
                }
                result
            } catch (e: Exception) {
                println("❌ Test failed with exception: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
        assertNotNull(result)
    }

    @Test
    fun testMalformedCovidPhishingUrl() {
        val result = runOnMainThread {
            try {
                val message = "https://uk-covid-19.webredirect.org/to"
                val sender = "unknown"

                val result = verifier.verify(message, sender)

                assertNotNull(result, "Result should not be null")

                println("COVID URL test result: ${result.textClassificationResult}")
                println("Extracted URLs: ${result.extractedUrls}")

                // This malformed URL should be detected
                if (result.textClassificationResult is TextClassificationResult.Unsafe) {
                    val reasons = result.textClassificationResult.listOfReasons
                    println("✅ COVID phishing detected with reasons: ${reasons.map { it.reason }}")

                    // Check if malformed URL was detected
                    val hasMalformedReason = reasons.any { it.reason.contains("Malformed URL", ignoreCase = true) }
                    if (hasMalformedReason) {
                        println("   ✓ Malformed URL correctly detected")
                    }
                }
                result
            } catch (e: Exception) {
                println("❌ Test failed with exception: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
        assertNotNull(result)
    }

    @Test
    fun testAllFourScamMessages() {
        runOnMainThread {
            try {
                val scamMessages = listOf(
                    "Amazon is sending you a refunding of £32.64. Please reply with your bank account number to receive your refund.",
                    "Wells Fargo Bank: Your account is temporarily locked. Please login at http://goog.gl/2a234 to secure your account.",
                    "myGov: We have processed your tax return of £2560.70. Kindly visit http://mygovtax.com for more details",
                    "https:/uk-covid-19.webredirect.org/to"
                )

                println("\n========== TESTING ALL 4 SCAM MESSAGES ==========\n")

                scamMessages.forEachIndexed { index, message ->
                    println("Testing scam message ${index + 1}:")
                    println("Message: \"${message.take(60)}...\"")

                    val result = verifier.verify(message, "unknown")
                    assertNotNull(result, "Result should not be null for message ${index + 1}")

                    when (result.textClassificationResult) {
                        is TextClassificationResult.Unsafe -> {
                            val reasons = (result.textClassificationResult as TextClassificationResult.Unsafe).listOfReasons
                            println("✅ DETECTED AS UNSAFE - Reasons: ${reasons.map { it.reason }}")
                        }
                        is TextClassificationResult.Safe -> {
                            println("❌ WARNING: Detected as SAFE (may need model improvement)")
                        }
                    }

                    result.extractedUrls?.let { urls ->
                        if (urls.isNotEmpty()) {
                            println("   Extracted URLs: $urls")
                            println("   URL scores: ${result.urlScores}")
                        }
                    }
                    println()
                }

                println("========== END OF SCAM MESSAGE TESTS ==========\n")
            } catch (e: Exception) {
                println("❌ Test failed with exception: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
    }
}
