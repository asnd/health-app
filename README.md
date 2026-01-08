# Interval Timer - Health App

A native Android application for long-interval activity reminders, built with Kotlin and Jetpack Compose.

## Features

- **Configurable Intervals**: Set activity time (10-120 minutes) and rest time (5-180 minutes)
- **Background Execution**: Reliable timer execution using Foreground Service
- **Persistent Notifications**: Always-on notification showing current phase and remaining time
- **Session History**: Track your timer sessions with Room database
- **Statistics**: View total completed sessions and cycles
- **Backend API Integration**: Sync sessions to backend with stub mode for development
- **Offline-First**: Works without internet, syncs when available
- **Material Design 3**: Modern UI with Jetpack Compose

## Architecture

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material Design 3
- **Background Work**: Foreground Service for reliable long-interval timers
- **Database**: Room for local data persistence
- **Networking**: Retrofit + OkHttp for API calls
- **Architecture**: MVVM + Repository Pattern
- **Data Layer**: Offline-first with optional backend sync

## Project Structure

```
app/src/main/java/com/healthapp/intervaltimer/
├── api/
│   ├── models/
│   │   └── ApiModels.kt         # API DTOs
│   ├── ApiService.kt            # API interface
│   ├── StubApiService.kt        # Stub implementation (no backend)
│   ├── RetrofitApiService.kt    # Real API implementation
│   ├── ApiClient.kt             # Retrofit configuration
│   ├── ApiConfig.kt             # API settings manager
│   ├── ApiFactory.kt            # Factory for API instances
│   └── TokenManager.kt          # Auth token management
├── data/
│   ├── TimerConfig.kt           # Data models
│   ├── TimerDatabase.kt         # Room database
│   └── TimerSessionDao.kt       # Database access
├── repository/
│   └── TimerRepository.kt       # Data repository (local + remote)
├── notifications/
│   └── NotificationHelper.kt    # Notification management
├── ui/
│   ├── TimerViewModel.kt        # Business logic
│   ├── SettingsScreen.kt        # Settings UI
│   └── theme/
│       └── Theme.kt             # Material theme
├── worker/
│   └── TimerForegroundService.kt # Background timer service
├── IntervalTimerApplication.kt  # Application class
└── MainActivity.kt              # Main UI
```

## Requirements

- Android Studio Hedgehog or later
- Android SDK 26+ (Android 8.0 Oreo)
- Target SDK 34 (Android 14)
- Kotlin 1.9.20+

## Building

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on device or emulator (API 26+)

```bash
./gradlew assembleDebug
```

## Permissions

The app requires the following permissions:
- `INTERNET` - API communication
- `ACCESS_NETWORK_STATE` - Check connectivity
- `POST_NOTIFICATIONS` - Show timer notifications (Android 13+)
- `SCHEDULE_EXACT_ALARM` - Precise timer execution
- `WAKE_LOCK` - Keep device awake for timer
- `FOREGROUND_SERVICE` - Background timer service

## Backend API Integration

### Stub Mode (Default)

By default, the app uses **Stub API mode** which provides fake data without requiring a backend server. This is perfect for:
- Development and testing
- Demo purposes
- Learning the codebase
- Offline usage

The stub implementation (`StubApiService.kt`) simulates network delays and provides realistic fake data for:
- User authentication
- Session syncing
- User statistics
- Leaderboards

### Real Backend Mode

To connect to a real backend:

1. **Open Settings** in the app (gear icon in top-right)
2. **Toggle "Use Stub API"** to OFF
3. **Enter your API Base URL** (e.g., `https://api.yourserver.com/v1/`)
4. **Enable Sync** to automatically sync sessions to the backend

### Switching Between Modes

```kotlin
// In code, you can configure via ApiConfig:
val apiConfig = ApiConfig(context)

// Use stub mode
apiConfig.setUseStubApi(true)

// Use real backend
apiConfig.setUseStubApi(false)
apiConfig.setBaseUrl("https://api.example.com/v1/")

// Enable/disable sync
apiConfig.setSyncEnabled(true)
```

### API Endpoints

See [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) for complete API specification.

**Base URL**: Configurable in settings

**Endpoints**:
- `POST /auth/login` - User authentication
- `POST /auth/register` - User registration
- `POST /sessions/sync` - Sync timer sessions
- `GET /sessions` - Fetch user sessions
- `GET /stats` - Get user statistics
- `GET /leaderboard` - Global leaderboard

### Building Your Own Backend

The app expects JSON responses with this structure:

```json
{
  "success": true,
  "data": { /* your data here */ },
  "error": null
}
```

Example backend stack recommendations:
- **Kotlin**: Ktor + PostgreSQL
- **Python**: FastAPI + PostgreSQL
- **Node.js**: Express + MongoDB
- **Go**: Gin + PostgreSQL

See API_DOCUMENTATION.md for detailed endpoint specifications.

## Future Enhancements

- [x] Backend integration for cloud sync
- [x] Stub API for development without backend
- [ ] User authentication UI
- [ ] Advanced statistics and charts
- [ ] Customizable notification sounds
- [ ] Multiple timer profiles
- [ ] Export session data (CSV/JSON)
- [ ] Social features (leaderboard UI)
- [ ] WebSocket for real-time updates

## License

MIT License
