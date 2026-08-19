# AGENTS.md - Observer Android App

Do not make edits yourself, only if user said explicitly!

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

### Compose UI
- Use `@Composable` functions for UI components
- Apply `Modifier` parameters consistently
- Use `remember` for state that survives recomposition
- Follow Material3 design system

### Database/Networking
- Room entities: suffix with `Entity`
- DTOs: suffix with `Dto`
- Use suspend functions for async operations
- Handle API responses with status checks</content>
