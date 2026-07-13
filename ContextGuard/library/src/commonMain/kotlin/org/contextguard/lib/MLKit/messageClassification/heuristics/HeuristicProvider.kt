package org.contextguard.lib.mlkit.messageClassification.heuristics

import org.contextguard.models.Reason

/**
 * Interface for providing custom heuristic detection rules.
 *
 * Developers can implement this interface to add custom threat detection patterns
 * beyond the built-in heuristics. Custom providers are registered via [HeuristicRegistry].
 *
 * Example implementation:
 * ```kotlin
 * class CustomHeuristicProvider : HeuristicProvider {
 *     override val label: String = "Custom Threat Pattern"
 *
 *     override fun detect(content: String, sender: String?): Boolean {
 *         return content.contains("specific pattern", ignoreCase = true)
 *     }
 * }
 * ```
 *
 * See EXTENDING.md for detailed documentation on framework customisation.
 */
interface HeuristicProvider {
    /**
     * Human-readable label for this heuristic.
     * This label is displayed to users when the heuristic matches.
     */
    val label: String

    /**
     * Detect whether the content matches this heuristic's pattern.
     *
     * @param content The normalised message content to analyse
     * @param sender Optional sender information for context-aware detection
     * @return true if the heuristic pattern is detected, false otherwise
     */
    fun detect(content: String, sender: String?): Boolean
}

/**
 * Registry for custom heuristic providers.
 *
 * Developers can register custom heuristics that will be evaluated alongside
 * the built-in detection rules.
 *
 * Example usage:
 * ```kotlin
 * // Register a custom heuristic at app startup
 * HeuristicRegistry.register(CustomHeuristicProvider())
 *
 * // Clear all custom heuristics
 * HeuristicRegistry.clear()
 * ```
 */
object HeuristicRegistry {
    private val customProviders = mutableListOf<HeuristicProvider>()

    /**
     * Register a custom heuristic provider.
     */
    fun register(provider: HeuristicProvider) {
        customProviders.add(provider)
    }

    /**
     * Unregister a specific heuristic provider.
     */
    fun unregister(provider: HeuristicProvider) {
        customProviders.remove(provider)
    }

    /**
     * Clear all registered custom heuristics.
     */
    fun clear() {
        customProviders.clear()
    }

    /**
     * Get all registered custom heuristic providers.
     */
    fun getProviders(): List<HeuristicProvider> = customProviders.toList()

    /**
     * Evaluate all custom heuristics against the given content.
     *
     * @param content The normalised message content
     * @param sender Optional sender information
     * @return List of Reasons for matching custom heuristics
     */
    fun evaluate(content: String, sender: String?): List<Reason> {
        return customProviders
            .filter { it.detect(content, sender) }
            .map { Reason(it.label) }
    }
}
