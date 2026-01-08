# Gemini Project Context

This file provides context for AI assistants working on the **Interval Timer** project.

## Project Overview
- **Type**: Native Android Application
- **Purpose**: Activity/Rest interval reminder with background execution.
- **Key Tech**: Kotlin, Jetpack Compose, Material 3, Room, Retrofit, Foreground Services.

## Architectural Patterns
- **MVVM**: `TimerViewModel` handles UI state via `TimerUiState` and `StateFlow`.
- **Repository Pattern**: `TimerRepository` manages data between Room (local) and Retrofit (remote).
- **Offline-First**: Local database is the source of truth; sync happens asynchronously.
- **Background Execution**: A `Foreground Service` maintains the timer state when the app is in the background, showing a persistent notification.

## Maintenance History

### Jan 8, 2026 - Initial Review & Refactor
- **Repository Fixes**: Resolved critical bugs in `TimerRepository.kt` where `collect` was called on infinite Flows inside suspend functions, causing the app to hang. Switched to `first()` for snapshots and `combine()` for reactive stats.
- **UI Performance**: Refactored `MainActivity.kt`'s `TimerScreen` to use a single unified `LazyColumn`. This fixed potential scroll-nesting issues and ensured the UI remains responsive and scrollable on all screen sizes.
- **I18n Preparation**: Removed hardcoded strings from the UI and moved them to `strings.xml`.
- **Logic Improvements**: Added format strings for dynamic UI content (e.g., "Total Sessions: %d").

## Critical Context for Future Agents
- **Timer Service**: The `TimerForegroundService` is the heartbeat of the app. Ensure any changes to timer logic are reflected in the notification updates.
- **Sync Logic**: Currently supports a "Stub API" for development. To enable real backend integration, check `ApiConfig.kt` and `ApiFactory.kt`.
- **State Synchronization**: Note that the Service and the ViewModel currently have somewhat separate states. Future work should involve a more robust bridge (e.g., the Service updating the Database, and the ViewModel observing the Database).

## Development Commands
- Build Debug: `./gradlew assembleDebug`
- Run Tests: `./gradlew test`
- Lint: `./gradlew lint`
