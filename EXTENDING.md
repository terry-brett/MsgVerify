# Extending MsgVerify

This guide is for researchers and developers who want to customise MsgVerify for empirical studies, custom threat detection, or alternative security indicator designs.

MsgVerify is designed as an extensible research platform. While it ships with default classifiers, heuristics, and UI components, each layer can be modified or replaced to suit specific research requirements.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    MsgVerify Application                     │
├─────────────────────────────────────────────────────────────┤
│  Presentation Layer (Compose Multiplatform)                  │
│  ├── Theme & Styling (MsgVerifyTheme.kt)                    │
│  ├── Risk Indicators (configurable thresholds)              │
│  └── Demo Screens (swappable datasets)                      │
├─────────────────────────────────────────────────────────────┤
│  Business Logic Layer                                        │
│  ├── MsgVerifyRepository (orchestration)                    │
│  ├── Configuration (MsgVerifyConfig.kt)                     │
│  └── ViewModels (state management)                          │
├─────────────────────────────────────────────────────────────┤
│  ContextGuard Library (Detection Engine)                     │
│  ├── URL Classifier (Neural Network)                        │
│  ├── Text Classifier (DistilBERT)                           │
│  └── Heuristic Labels (Pattern Matching)                    │
└─────────────────────────────────────────────────────────────┘
```

## Extension Points

### 1. Risk Thresholds

Risk classification thresholds determine how confidence scores map to visual indicators (green/yellow/red). These are configurable in:

**File:** `composeApp/src/commonMain/kotlin/com/terrydroid/msgverify/config/MsgVerifyConfig.kt`

```kotlin
object MsgVerifyConfig {
    // Risk thresholds (0-100 scale)
    var highRiskThreshold: Int = 70    // >= this = Red (High Risk)
    var mediumRiskThreshold: Int = 40  // >= this = Yellow (Medium Risk)
                                        // < this = Green (Safe)
}
```

**Use cases:**
- Adjust sensitivity for different user populations
- A/B test different threshold configurations
- Calibrate for specific threat landscapes

### 2. Demo Datasets

Demo scenarios use JSON files that can be replaced with custom research datasets.

**Location:** `mocks/` (root directory - copied to composeResources during build)

```
mocks/
├── safe/
│   ├── sms.json
│   ├── emails.json
│   └── social_media_messages.json
└── malicious/
    ├── sms.json
    ├── emails.json
    └── social_media_messages.json
```

**JSON format for SMS:**
```json
[
  {
    "sender": "+1234567890",
    "content": "Your package is waiting. Track here: http://example.com/track",
    "timestamp": "2024-01-15T10:30:00Z"
  }
]
```

**JSON format for emails:**
```json
[
  {
    "sender": "security@example.com",
    "subject": "Urgent: Verify your account",
    "content": "Click here to verify your account immediately...",
    "timestamp": "2024-01-15T10:30:00Z"
  }
]
```

**JSON format for social media:**
```json
[
  {
    "message": "URGENT: Your account is locked. Verify now at secure-login.example"
  }
]
```

**Use cases:**
- Controlled experiments with specific threat scenarios
- Localised phishing campaigns (language, regional threats)
- Longitudinal studies with evolving threat datasets

### 3. Visual Theme Customisation

The UI layer uses Compose Multiplatform with Material3 theming, allowing customisation of colours, typography, and visual indicators.

**File:** `composeApp/src/commonMain/kotlin/com/terrydroid/msgverify/theme/MsgVerifyTheme.kt`

```kotlin
@Composable
fun MsgVerifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Add custom parameters for A/B testing
    warningColorScheme: WarningColorScheme = WarningColorScheme.Default,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

**Risk indicator colours are defined in:**
`composeApp/src/commonMain/kotlin/com/terrydroid/msgverify/home/HomeViewModel.kt`

```kotlin
fun getClassificationColor(score: Int): Color {
    return when {
        score >= MsgVerifyConfig.highRiskThreshold -> Color.Red
        score >= MsgVerifyConfig.mediumRiskThreshold -> Color.Yellow
        else -> Color.Green
    }
}
```

**Use cases:**
- Test alternative warning colour schemes
- Compare traffic-light vs. gradient indicators
- Accessibility studies with different contrast ratios

### 4. Heuristic Labels

The heuristic label system generates human-readable explanations for detected threats. Labels are defined in the ContextGuard library.

**File:** `ContextGuard/library/src/commonMain/kotlin/org/contextguard/lib/MLKit/messageClassification/heuristics/MessageReasoningLabels.kt`

**Default labels:**
- `Impersonation` — Sender claims to be a known entity
- `Urgency/Intimidation` — Time pressure or threatening language
- `Link Click Pressure` — Aggressive prompts to click links
- `Financial or Personal Information` — Requests for sensitive data
- `Too Good to Be True` — Unrealistic offers or prizes
- `Credential Verification Request` — Fake login/verification requests

#### Using HeuristicProvider and HeuristicRegistry

For adding custom heuristics without modifying ContextGuard source code, use the `HeuristicProvider` interface and `HeuristicRegistry`:

**File:** `ContextGuard/library/src/commonMain/kotlin/org/contextguard/lib/MLKit/messageClassification/heuristics/HeuristicProvider.kt`

```kotlin
// Define a custom heuristic
val cryptoScamHeuristic = object : HeuristicProvider {
    override val label: String = "Cryptocurrency Scam"

    override fun detect(content: String, sender: String?): Boolean {
        val patterns = listOf("bitcoin", "btc", "ethereum", "crypto wallet", "seed phrase")
        return patterns.any { content.lowercase().contains(it) }
    }
}

// Register at app startup
HeuristicRegistry.register(cryptoScamHeuristic)

// Or register multiple heuristics
HeuristicRegistry.register(jobScamHeuristic)
HeuristicRegistry.register(romanceScamHeuristic)

// Clear all custom heuristics
HeuristicRegistry.clear()
```

Custom heuristics are automatically evaluated alongside built-in detection rules and their labels appear in the results.

#### Modifying Built-in Labels

To add custom labels directly to ContextGuard:

1. Add the label constant:
```kotlin
private object Labels {
    // ... existing labels
    const val CUSTOM_LABEL = "Your Custom Label"
}
```

2. Add detection logic in `Helper.kt`:
```kotlin
fun detectCustomPattern(content: String): Boolean {
    // Your detection logic
    return content.contains("specific pattern", ignoreCase = true)
}
```

3. Register in the label generation flow in `MessageReasoningLabels.kt`

**Use cases:**
- Detect emerging threat types
- Localise for language-specific patterns
- Research-specific categorisation schemes

### 5. Live Data Source Integration

MsgVerify can receive and analyse content from external sources in real-time, enabling longitudinal field studies and ecological assessments. The framework provides platform-native hooks for receiving live message streams.

#### Android: Intent Handling

MsgVerify registers as a share target, allowing users to forward suspicious content from any app.

**File:** `composeApp/src/androidMain/AndroidManifest.xml`

```xml
<intent-filter>
    <action android:name="android.intent.action.SEND" />
    <category android:name="android.intent.category.DEFAULT" />
    <data android:mimeType="text/plain" />
</intent-filter>
```

**File:** `composeApp/src/androidMain/kotlin/com/terrydroid/msgverify/MainActivity.kt`

```kotlin
private fun handleIntent(intent: Intent?) {
    if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        receivedText.value = sharedText
    }
}
```

**Extending for custom data sources:**

```kotlin
// Add support for additional MIME types
<data android:mimeType="text/*" />
<data android:mimeType="application/json" />

// Or register a BroadcastReceiver for specific apps
class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Extract SMS content and forward to MsgVerify
    }
}
```

#### iOS: App Intents and Shortcuts

MsgVerify integrates with iOS Shortcuts and Siri, enabling automated analysis workflows.

**File:** `iosApp/iosApp/VerifyMessageIntent.swift`

```swift
struct VerifyMessageIntent: AppIntent {
    static var title: LocalizedStringResource = "Verify Message"
    
    @Parameter(title: "Message Text")
    var text: String
    
    func perform() async throws -> some IntentResult {
        MainViewControllerKt.setSharedText(text: text)
        return .result()
    }
}
```

**File:** `iosApp/iosApp/MsgVerifyShortcuts.swift`

```swift
struct MsgVerifyShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: VerifyMessageIntent(),
            phrases: [
                "Verify a message with \(.applicationName)",
                "Check a message with \(.applicationName)",
                "Scan a message with \(.applicationName)"
            ]
        )
    }
}
```

**iOS Automation workflows:**
Users can configure iOS Automations to trigger MsgVerify analysis automatically:
1. When a message arrives from an unknown sender
2. At scheduled intervals for batch processing
3. When specific keywords are detected

#### URL Scheme Integration

MsgVerify supports deep linking via custom URL schemes:

**iOS:** `msgverify://share?text=<encoded_content>`

```swift
// In iOSApp.swift
.onOpenURL { url in
    if url.scheme == "msgverify", 
       let components = URLComponents(url: url, resolvingAgainstBaseURL: true),
       let text = components.queryItems?.first(where: { $0.name == "text" })?.value {
        MainViewControllerKt.setSharedText(text: text)
    }
}
```

**Use cases:**
- Longitudinal field studies capturing real threat encounters
- Automated analysis pipelines for security operations
- Integration with notification monitoring tools
- Browser extensions forwarding suspicious links

#### Implementing Custom Data Sources

MsgVerify provides a `LiveDataProvider` interface and `LiveDataRegistry` for integrating custom data sources.

**File:** `composeApp/src/commonMain/kotlin/com/terrydroid/msgverify/data/LiveDataProvider.kt`

```kotlin
interface LiveDataProvider {
    val id: String
    val displayName: String
    fun start()
    fun stop()
    fun isActive(): Boolean
}
```

**Example: SMS Monitor Provider**

```kotlin
class SmsMonitorProvider(private val context: Context) : LiveDataProvider {
    override val id = "sms-monitor"
    override val displayName = "SMS Monitor"
    private var isRunning = false

    override fun start() {
        isRunning = true
        // Register BroadcastReceiver for incoming SMS
        context.registerReceiver(smsReceiver, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
    }

    override fun stop() {
        isRunning = false
        context.unregisterReceiver(smsReceiver)
    }

    override fun isActive() = isRunning

    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            messages.forEach { sms ->
                // Emit content for analysis
                runBlocking {
                    LiveDataRegistry.emitContent(
                        content = sms.messageBody,
                        sender = sms.originatingAddress,
                        sourceId = id
                    )
                }
            }
        }
    }
}
```

**Registering and Using Providers**

```kotlin
// At app startup (e.g., in Application.onCreate or Koin module)
val smsProvider = SmsMonitorProvider(context)
LiveDataRegistry.register(smsProvider)

// Start monitoring
LiveDataRegistry.startAll()

// Collect incoming content for analysis
scope.launch {
    LiveDataRegistry.contentFlow.collect { liveContent ->
        val result = msgVerifyRepository.verifyContent(
            input = liveContent.content,
            sender = liveContent.sender ?: ""
        )
        // Handle verification result
    }
}

// Stop when done
LiveDataRegistry.stopAll()
```

**LiveDataRegistry API:**
- `register(provider)` — Add a data provider
- `unregister(id)` — Remove a provider by ID
- `startAll()` / `stopAll()` — Control all providers
- `contentFlow` — SharedFlow of incoming `LiveContent` for analysis
- `emitContent(...)` — Send content from within a provider

### 6. ContextGuard Integration

For deeper customisation, you can modify or replace the ContextGuard library components.

**ContentVerifier interface:**
`ContextGuard/library/src/commonMain/kotlin/org/contextguard/lib/ContentVerifier.kt`

```kotlin
interface ContentVerifier {
    suspend fun verify(content: String, sender: String? = null): VerificationResult
    suspend fun verifyUrl(url: String): Float
}
```

**To implement a custom verifier:**

```kotlin
class CustomContentVerifier : ContentVerifier {
    override suspend fun verify(content: String, sender: String?): VerificationResult {
        // Your custom verification logic
    }
    
    override suspend fun verifyUrl(url: String): Float {
        // Your custom URL scoring logic
    }
}
```

**Register via Koin in:**
`composeApp/src/commonMain/kotlin/com/terrydroid/msgverify/di/AppModule.kt`

## Research Workflows

### A/B Testing Warning Designs

1. Create multiple theme variants in `MsgVerifyTheme.kt`
2. Add a configuration flag to switch between variants
3. Log user interactions for analysis

### Controlled Threat Studies

1. Replace demo JSON files with your experimental dataset
2. Configure appropriate risk thresholds
3. Deploy to participant devices

### Cross-Platform Consistency Studies

MsgVerify uses Kotlin Multiplatform to ensure identical detection logic on Android and iOS. The shared codebase guarantees:
- Same ML model inference
- Same heuristic evaluation
- Same risk scoring

Platform-specific code is isolated to:
- `composeApp/src/androidMain/` — Android intents, file I/O
- `composeApp/src/iosMain/` — iOS Shortcuts, file I/O

### In-the-Wild Ecological Assessments

Leverage the live data source integration (Section 5) to conduct longitudinal field studies:

1. **Configure platform hooks:**
   - Android: Enable share intent handling in AndroidManifest.xml
   - iOS: Set up Shortcuts and Automations for participants

2. **Enable research logging:**
   ```kotlin
   MsgVerifyConfig.enableLogging = true
   ```

3. **Deploy to participant devices** with appropriate consent flows

4. **Collect data** as participants forward real suspicious messages they encounter

This approach captures genuine threat-assessment behaviours in participants' natural environments, providing ecological validity that controlled lab studies cannot achieve.

## Building Custom Variants

### Local Development

```bash
# Build Android debug variant
./gradlew :composeApp:assembleDebug

# Build iOS (requires macOS + Xcode)
cd iosApp && pod install
# Then build via Xcode
```

### Testing with Local ContextGuard Changes

1. Edit `settings.gradle.kts`
2. Set `testLocally = true`
3. Make changes to `ContextGuard/` directory
4. Rebuild the project

## Data Collection Considerations

When deploying MsgVerify for research:

- **Privacy:** All ML inference runs on-device; no data is transmitted externally
- **Logging:** Add custom logging in `MsgVerifyRepository.kt` for research data collection
- **Consent:** Implement appropriate consent flows for participant studies
- **Ethics:** Ensure IRB/ethics approval for human subjects research

## Support

For questions about extending MsgVerify for research purposes:
- Open an issue on GitHub
- Contact: odin.asbjornsen@dnb.no
