# MsgVerify

A cross-platform message and content verification application that detects phishing, scams, and malicious content in text using embedded machine learning models. Works completely offline with on-device inference.

## Overview

MsgVerify is a Kotlin Multiplatform (KMP) application with a shared codebase deployed on Android and iOS. It serves as a sample implementation and demonstration of the **ContextGuard** library, showcasing how to integrate embedded machine learning models with heuristic-based detection to provide users with real-time content safety analysis without requiring an internet connection.

### Relationship with ContextGuard

MsgVerify is built on top of the **ContextGuard** library, which provides the core content verification engine. ContextGuard is a Kotlin Multiplatform library that encapsulates:
- Machine learning models (PyTorch Lite) for text classification
- URL extraction and analysis algorithms
- Heuristic-based detection rules
- Risk scoring and reasoning generation

MsgVerify demonstrates how to integrate ContextGuard into a real-world application by providing:
- A user-friendly UI for content verification
- Demo scenarios with mock phishing/spam data
- iOS Shortcuts integration for system-level access
- History tracking and result visualization

**Key Features:**
- **Offline Content Verification**: Analyzes text for phishing/scam indicators using embedded ML models
- **URL Detection & Analysis**: Extracts and scores URLs within content
- **Multi-Channel Support**: SMS, Email, Social Media, Generic Text
- **Demo Scenarios**: Interactive demonstrations with mock data
- **Verification History**: Maintains local history of checked items
- **iOS Shortcuts Integration**: Siri/Shortcuts support for voice commands

## Architecture

### Technology Stack

- **Frontend**: Kotlin Multiplatform with Compose Multiplatform
- **Architecture Pattern**: MVVM + Repository Pattern
- **Dependency Injection**: Koin
- **ML Backend**: ContextGuard library (embedded models)
- **Platforms**: Android (API 30-35), iOS (x64, arm64, simulator)

### Project Structure

```
MsgVerify/
├── composeApp/              # Main Kotlin Multiplatform application
│   ├── src/commonMain/      # Shared code (Android + iOS)
│   ├── src/androidMain/     # Android-specific code
│   ├── src/iosMain/         # iOS-specific code
│   └── src/commonTest/      # Shared tests
├── iosApp/                  # iOS-specific implementation
└── mocks/                   # Test data for demos
```

## Content Verification Pipeline

```
User Input
    ↓
HomeViewModel.onVerifyClicked()
    ↓
MsgVerifyRepository.verifyContent()
    ↓
ContentVerifierImpl (from ContextGuard library)
    ├─ ML Model Classification
    │  └─ TextClassificationResult.Safe/Unsafe
    ├─ URL Detection & Scoring
    │  └─ List<Float> urlScores
    └─ Reason Analysis
       └─ List<Reason> reasons
    ↓
LinkVerificationState Updates
    ├─ UI Re-composition
    └─ History Management
```

### Risk Classification

- **Green**: <40% malicious confidence (Safe)
- **Yellow**: 40-70% malicious confidence (Medium Risk)
- **Red**: >70% malicious confidence (High Risk)

## Getting Started

### Prerequisites

- **JDK**: Java 11 or higher
- **Android Studio**: Latest stable version with Kotlin plugin
- **Xcode**: For iOS development (macOS only)

### Building the Project

#### Android

```bash
./gradlew :composeApp:assembleDebug
```

#### iOS

1. Open `iosApp/iosApp.xcodeproj` in Xcode
2. Select your target device/simulator
3. Build and run (⌘+R)

### Installing Dependencies

All dependencies are managed via Gradle:

```bash
./gradlew build
```

### iOS Dependencies 

Navigate to ios app with `cd iosApp` and install the dependency

```bash
pod install
```

#### Manual installation
Create file named `Podfile` in the iOS app directory. Copy and paste the following:

```bash
platform :ios, '13.0'
use_frameworks!

target 'iosApp' do
  pod 'TensorFlowLiteObjC', '2.17.0'
  pod 'TensorFlowLiteObjC/Metal', '2.17.0'
end
```

Install with `pod install`

## Key Dependencies

### Compose & UI
- Compose Multiplatform: 1.8.2
- Material3: 1.8.2
- Lifecycle ViewModel: 2.9.1

### State Management & DI
- Koin: 4.0.3

### Content Verification
- ContextGuard: 1.0.0 (Custom ML-based verification library)

### ML & Data Processing
- PyTorch Lite Multiplatform: 0.7.0

## Demo Data

The project includes mock data for testing and demonstrations:

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

## iOS Shortcuts Integration

MsgVerify supports iOS Shortcuts via `VerifyMessageIntent.swift`:

1. **Add Shortcut**: Use the Shortcuts app to create a custom action
2. **Voice Command**: Say "Hey Siri, verify this message"
3. **System Integration**: Seamlessly integrates with iOS share sheets

## Configuration

### Local ContextGuard Testing

To test with a local build of the ContextGuard library:

1. Edit `settings.gradle.kts`
2. Set `testLocally = true`
3. Rebuild the project

## Screens

### Home Screen
- **Purpose**: Main content verification interface
- **Input**: Text area for messages/URLs
- **Output**: Risk assessment with color-coded results
- **History**: Recent verification history

### Demo Screens
- **SMS Demo**: Phishing SMS detection examples
- **Email Demo**: Phishing email detection examples
- **Social Media Demo**: Social media content analysis examples

