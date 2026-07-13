package com.terrydroid.msgverify.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Interface for live data source providers.
 *
 * Implement this interface to integrate custom data sources that feed
 * content into MsgVerify for analysis. Examples include:
 * - SMS listeners
 * - Notification monitors
 * - Email polling services
 * - Custom API integrations
 *
 * Usage:
 * ```kotlin
 * val myProvider = object : LiveDataProvider {
 *     override val id = "sms-monitor"
 *     override val displayName = "SMS Monitor"
 *
 *     override fun start() {
 *         // Begin listening for SMS messages
 *         smsReceiver.register { message, sender ->
 *             emitContent(message, sender)
 *         }
 *     }
 *
 *     override fun stop() {
 *         smsReceiver.unregister()
 *     }
 * }
 *
 * LiveDataRegistry.register(myProvider)
 * LiveDataRegistry.startAll()
 * ```
 */
interface LiveDataProvider {
    /**
     * Unique identifier for this provider.
     */
    val id: String

    /**
     * Human-readable name for display purposes.
     */
    val displayName: String

    /**
     * Start listening for data from this source.
     * When content is received, call [emitContent] to send it for analysis.
     */
    fun start()

    /**
     * Stop listening and clean up resources.
     */
    fun stop()

    /**
     * Check if the provider is currently active.
     */
    fun isActive(): Boolean = false
}

/**
 * Data class representing content received from a live data source.
 */
data class LiveContent(
    val content: String,
    val sender: String? = null,
    val sourceId: String,
    val timestamp: Long = currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Registry for managing live data providers.
 *
 * Researchers can register custom providers to integrate various data sources.
 * The registry manages lifecycle and provides a unified stream of incoming content.
 */
object LiveDataRegistry {
    private val providers = mutableMapOf<String, LiveDataProvider>()
    private val _contentFlow = MutableSharedFlow<LiveContent>(replay = 0, extraBufferCapacity = 64)

    /**
     * Flow of content received from all active providers.
     * Subscribe to this flow to receive live content for analysis.
     */
    val contentFlow: SharedFlow<LiveContent> = _contentFlow.asSharedFlow()

    /**
     * Register a new data provider.
     * If a provider with the same ID exists, it will be replaced.
     */
    fun register(provider: LiveDataProvider) {
        providers[provider.id]?.stop()
        providers[provider.id] = provider
    }

    /**
     * Unregister a provider by ID.
     */
    fun unregister(id: String) {
        providers[id]?.stop()
        providers.remove(id)
    }

    /**
     * Unregister a provider instance.
     */
    fun unregister(provider: LiveDataProvider) {
        unregister(provider.id)
    }

    /**
     * Get all registered providers.
     */
    fun getProviders(): List<LiveDataProvider> = providers.values.toList()

    /**
     * Get a provider by ID.
     */
    fun getProvider(id: String): LiveDataProvider? = providers[id]

    /**
     * Start all registered providers.
     */
    fun startAll() {
        providers.values.forEach { it.start() }
    }

    /**
     * Stop all registered providers.
     */
    fun stopAll() {
        providers.values.forEach { it.stop() }
    }

    /**
     * Clear all providers (stops them first).
     */
    fun clear() {
        stopAll()
        providers.clear()
    }

    /**
     * Emit content from a provider into the shared flow.
     * Call this from within your provider's implementation when content is received.
     */
    suspend fun emitContent(content: LiveContent) {
        _contentFlow.emit(content)
    }

    /**
     * Convenience method to emit content with minimal parameters.
     */
    suspend fun emitContent(
        content: String,
        sender: String? = null,
        sourceId: String,
        metadata: Map<String, String> = emptyMap()
    ) {
        emitContent(LiveContent(content, sender, sourceId, currentTimeMillis(), metadata))
    }
}

/**
 * Platform-independent timestamp function.
 */
internal expect fun currentTimeMillis(): Long
