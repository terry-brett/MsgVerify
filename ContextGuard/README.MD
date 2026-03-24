# ContextGuard

A Kotlin Multiplatform library for offline phishing and scam detection. ContextGuard analyses text and URLs using embedded machine learning models and heuristic rules — no internet connection required.

**Platforms**: Android (API 30+), iOS (x64, arm64, simulator)

## What it detects

- Phishing and social engineering patterns in message text
- Malicious URLs (scored 0–100 for maliciousness)
- Suspicious link characteristics (typosquatting, malformed URLs)
- Grammatical errors and poor formatting associated with scam content
- Urgency tactics, credential requests, and other known scam signals

## Adding the dependency

```kotlin
// build.gradle.kts
implementation("org.contextguard.lib:contextguard:1.0.2")
```

### iOS (CocoaPods)

Add to your `Podfile`:

```ruby
platform :ios, '13.0'
use_frameworks!

target 'iosApp' do
  pod 'TensorFlowLiteObjC', '2.17.0'
  pod 'TensorFlowLiteObjC/Metal', '2.17.0'
end
```

Then run `pod install`.

## Usage

```kotlin
// Create the verifier (pass Android Context or iOS UIApplication)
val verifier: ContentVerifier = ContentVerifierImpl(platformContext)

// Verify a message (text + any embedded URLs)
val result = verifier.verify(
    content = "Your account has been suspended. Click here: http://paypa1.com/verify",
    sender = "support@paypa1.com"
)

// Inspect the result
when (result?.textClassificationResult) {
    is TextClassificationResult.Safe -> {
        // Content looks clean
    }
    is TextClassificationResult.Unsafe -> {
        val reasons = result.textClassificationResult.listOfReasons
        reasons.forEach { println(it.reason) }
    }
    null -> {
        // Verification failed or input was empty
    }
}

// URL scores (one per extracted URL, 0–100 maliciousness)
result?.urlScores?.forEachIndexed { i, score ->
    println("${result.extractedUrls?.get(i)}: $score% malicious")
}
```

### Verify a URL directly

```kotlin
val result = verifier.verifyUrl("http://suspicious-domain.xyz/login")
val score = result?.urlScores?.firstOrNull() // 0–100
```

## Result model

| Field | Type | Description |
|---|---|---|
| `textClassificationResult` | `TextClassificationResult` | `Safe` or `Unsafe` with a list of reasons |
| `urlScores` | `List<Float>?` | Maliciousness score (0–100) per extracted URL |
| `extractedUrls` | `List<String>?` | URLs found in the content, parallel to `urlScores` |

## Initialization (Android only)

On Android, initialise the spellchecker once at app startup with your `Application` context:

```kotlin
Spellcheck.init(applicationContext)
```

On iOS this is a no-op.

---

## Development

### Build

```bash
./gradlew build
```

### Run the sample app

- **Android**: Open the project in Android Studio and run the sample app module
- **iOS**: Open `sample/iosApp/iosApp.xcodeproj` in Xcode and run the sample app

### Publish to MavenLocal

```bash
./gradlew :ContextGuard:publishToMavenLocal
```

Output: `~/.m2/repository/org/contextguard/lib/`

### Publish to MavenCentral

1. Create an account and namespace on Sonatype:
   https://central.sonatype.org/register/central-portal/#create-an-account
2. Add developer details to `./ContextGuard/build.gradle.kts`
3. Generate a GPG key:
   https://getstream.io/blog/publishing-libraries-to-mavencentral-2021/#generating-a-gpg-key-pair
   ```
   gpg --full-gen-key
   gpg --keyserver keyserver.ubuntu.com --send-keys XXXXXXXX
   gpg --export-secret-key XXXXXXXX > XXXXXXXX.gpg
   ```
4. Add to `gradle.properties`:
   ```
   signing.keyId=XXXXXXXX
   signing.password=[key password]
   signing.secretKeyRingFile=../XXXXXXXX.gpg
   mavenCentralUsername=[generated username]
   mavenCentralPassword=[generated password]
   ```
5. Publish:
   ```bash
   ./gradlew :ContextGuard:publishAndReleaseToMavenCentral --no-configuration-cache
   ```
