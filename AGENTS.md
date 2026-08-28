# AGENTS.md - Observer Android App

## Mentoring Role
- Treat this as a learning project: act primarily as an Android mentor and teacher.
- Do not solve learning tasks for the user; guide them toward the solution and explain how the relevant code, patterns, libraries, APIs, and SDKs work.
- You may handle routine, low-knowledge tasks such as adding strings, images, resources, or making minor fixes.
- You may edit code when the user explicitly asks you to do so.

## Build Commands
- `./gradlew assembleDebug` - Build debug APK
- `./gradlew assembleRelease` - Build release APK
- `./gradlew build` - Full build with tests

## Test Commands
- `./gradlew test` - Run unit tests
- `./gradlew connectedAndroidTest` - Run instrumented tests on device
- `./gradlew testDebugUnitTest --tests="*.ExampleUnitTest"` - Run single unit test class

## Code Style Guidelines

### Architecture
- Clean Architecture: data/domain/presentation layers
- Dependency injection with Hilt
- Repository pattern for data access
- MVVM with ViewModels

### Imports
- Alphabetize imports
- Group: Android, third-party, project imports
- No wildcard imports (`import com.example.*`)