# ContextGuard

Kotlin Multiplatform Library

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

### Run Sample App

 - Android: `open project in Android Studio and run the sample app`
 - iOS: `open 'sample/iosApp/iosApp.xcodeproj' in Xcode and run the sample app`

### Publish to MavenLocal

1) Run `./gradlew :ContextGuard:publishToMavenLocal`
2) Open `~/.m2/repository/org/contextguard/lib/`

### Publish to MavenCentral

1) Create an account and a namespace on Sonatype:  
   https://central.sonatype.org/register/central-portal/#create-an-account
2) Add developer id, name, email and the project url to  
   `./ContextGuard/build.gradle.kts`
3) Generate a GPG key:  
   https://getstream.io/blog/publishing-libraries-to-mavencentral-2021/#generating-a-gpg-key-pair
   ```
   gpg --full-gen-key
   gpg --keyserver keyserver.ubuntu.com --send-keys XXXXXXXX
   gpg --export-secret-key XXXXXXXX > XXXXXXXX.gpg
   ```
4) Add these lines to `gradle.properties`:
   ```
   signing.keyId=XXXXXXXX
   signing.password=[key password]
   signing.secretKeyRingFile=../XXXXXXXX.gpg
   mavenCentralUsername=[generated username]
   mavenCentralPassword=[generated password]
   ```
5) Run `./gradlew :ContextGuard:publishAndReleaseToMavenCentral --no-configuration-cache`
