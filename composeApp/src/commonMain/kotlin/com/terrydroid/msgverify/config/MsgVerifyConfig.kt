package com.terrydroid.msgverify.config

/**
 * Central configuration for MsgVerify framework.
 *
 * Developers can modify these values to customise the application's behaviour
 * for empirical studies, A/B testing, or different threat landscapes.
 *
 * See EXTENDING.md for detailed documentation on framework customisation.
 */
object MsgVerifyConfig {

    // ============================================
    // Risk Classification Thresholds
    // ============================================

    /**
     * Threshold for high-risk classification (red indicator).
     * Scores >= this value are classified as high risk.
     * Default: 70 (70% confidence of malicious content)
     */
    var highRiskThreshold: Int = 70

    /**
     * Threshold for medium-risk classification (yellow indicator).
     * Scores >= this value (but < highRiskThreshold) are classified as medium risk.
     * Scores below this value are classified as safe (green indicator).
     * Default: 40 (40% confidence of malicious content)
     */
    var mediumRiskThreshold: Int = 40

    // ============================================
    // Feature Flags
    // ============================================

    /**
     * Enable detailed logging for data collection.
     * When true, verification events are logged with timestamps and scores.
     */
    var enableLogging: Boolean = false

    /**
     * Enable demo mode by default on app launch.
     * Useful for controlled study environments.
     */
    var startInDemoMode: Boolean = false
}
