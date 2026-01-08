# Backend Integration Guide

Quick guide for integrating the Interval Timer app with your backend or using stub mode.

## Quick Start

### Using Stub Mode (No Backend Required)

The app works out of the box with stub mode enabled:

1. Build and run the app
2. The app automatically uses stub API (fake data)
3. All features work without network connectivity
4. Perfect for development and testing

### Switching to Real Backend

1. **Prepare your backend** following [API_DOCUMENTATION.md](./API_DOCUMENTATION.md)
2. **Open the app** and tap the ⚙️ Settings icon
3. **Toggle "Use Stub API" to OFF**
4. **Enter your API Base URL** (e.g., `https://api.myserver.com/v1/`)
5. **Enable Sync** to start syncing sessions
6. **Done!** The app will now communicate with your backend

---

## Architecture Overview

```
┌─────────────┐
│   UI Layer  │  MainActivity, SettingsScreen
└──────┬──────┘
       │
┌──────▼──────┐
│  ViewModel  │  TimerViewModel (business logic)
└──────┬──────┘
       │
┌──────▼──────┐
│ Repository  │  TimerRepository (data coordination)
└──────┬──────┘
       │
       ├───────────────┬─────────────┐
       │               │             │
┌──────▼──────┐ ┌─────▼─────┐ ┌────▼────┐
│  Local DB   │ │ Stub API  │ │Real API │
│   (Room)    │ │  (Fake)   │ │(Retrofit)│
└─────────────┘ └───────────┘ └─────────┘
```

**Key Principles:**
- **Offline-first**: Local database is source of truth
- **Optional sync**: Backend sync can be enabled/disabled
- **Seamless switching**: Toggle between stub and real API without code changes
- **No data loss**: All data stored locally, synced when available

---

## Implementation Details

### 1. API Configuration (`ApiConfig.kt`)

Manages API settings using DataStore:

```kotlin
val apiConfig = ApiConfig(context)

// Configure stub vs real
apiConfig.setUseStubApi(true)  // or false

// Set backend URL
apiConfig.setBaseUrl("https://api.example.com/v1/")

// Enable/disable sync
apiConfig.setSyncEnabled(true)
```

### 2. API Factory (`ApiFactory.kt`)

Creates appropriate API instance:

```kotlin
// Automatically uses configured settings
val apiService = ApiFactory.getConfiguredApiService(context)

// Or manually specify
val stubApi = ApiFactory.createApiService(context, useStub = true)
val realApi = ApiFactory.createApiService(context, useStub = false, baseUrl = "...")
```

### 3. Repository Layer (`TimerRepository.kt`)

Coordinates local and remote data:

```kotlin
val repository = TimerRepository(
    localDataSource = database.timerSessionDao(),
    remoteDataSource = apiService,
    syncEnabled = true  // Enable background sync
)

// Insert saves locally and optionally syncs
repository.insertSession(session)

// Manual sync all sessions
repository.syncAllSessions()

// Fetch remote stats
repository.fetchRemoteStats()
```

### 4. Stub API (`StubApiService.kt`)

Provides fake data without backend:

- Simulates network delays (300-1000ms)
- Generates 10 fake historical sessions
- Returns realistic statistics
- Creates fake leaderboards
- No network required

**Stub Features:**
- ✅ Login (accepts any email/password with length ≥ 6)
- ✅ Register (validates email format)
- ✅ Session sync (stores in memory)
- ✅ Statistics (calculates from fake sessions)
- ✅ Leaderboard (random rankings)
- ✅ Ping (always returns true)

### 5. Real API (`RealApiService.kt`)

Production-ready Retrofit implementation:

- Bearer token authentication
- Request/response logging
- 30-second timeouts
- Error handling
- Token management via `TokenManager`

---

## Data Flow Examples

### Creating a Timer Session

1. User starts timer in UI
2. `TimerViewModel.startTimer()` called
3. Session created and inserted via `repository.insertSession()`
4. Repository saves to **local Room database** ✅
5. If sync enabled, repository calls API to sync session
6. Session stored both locally and remotely (if sync succeeds)

**Key Point:** Local save always succeeds. API sync failure doesn't affect user.

### Viewing Statistics

1. User opens app
2. `TimerViewModel` loads stats from repository
3. Repository queries **local database** for immediate display
4. If sync enabled, repository also fetches remote stats
5. UI shows combined local + remote statistics

---

## Testing Guide

### Test with Stub API

```kotlin
@Test
fun testStubApiLogin() = runTest {
    val tokenManager = TokenManager(context)
    val stubApi = StubApiService(tokenManager)

    val result = stubApi.login("test@example.com", "password123")

    assertTrue(result.isSuccess)
    assertNotNull(result.getOrNull()?.token)
}
```

### Test with Mock Server

Use tools like:
- **WireMock** for API mocking
- **MockWebServer** (OkHttp) for HTTP testing
- **Postman Mock Server** for quick prototypes

### Integration Testing

1. Start with stub mode
2. Implement backend endpoints one by one
3. Test each endpoint with real API
4. Switch between stub/real to verify behavior matches

---

## Backend Implementation Checklist

### Minimum Viable Backend

- [ ] **POST /auth/login** - Return JWT token
- [ ] **POST /sessions/sync** - Accept and store sessions
- [ ] **GET /sessions** - Return user's sessions

### Full Feature Backend

- [ ] User registration and authentication
- [ ] Session CRUD operations
- [ ] Statistics aggregation
- [ ] Leaderboard calculation
- [ ] Token refresh mechanism
- [ ] Rate limiting
- [ ] Database schema setup
- [ ] HTTPS configuration

### Recommended Tech Stacks

**Fast Development:**
```
FastAPI (Python) + PostgreSQL + JWT
└── Quick to build, excellent docs
```

**High Performance:**
```
Ktor (Kotlin) + PostgreSQL + Exposed ORM
└── Same language as Android app
```

**Scalable:**
```
Go (Gin) + PostgreSQL + Redis cache
└── Excellent performance, easy deployment
```

**Full Stack:**
```
Node.js (Express) + MongoDB + Passport.js
└── Large ecosystem, flexible
```

---

## Common Issues

### Issue: "Network error" when sync enabled

**Solution:**
1. Check if backend URL is correct in Settings
2. Verify backend is accessible from device/emulator
3. Check Android logs: `adb logcat | grep Retrofit`
4. Test with `curl` from command line first

### Issue: Sessions not syncing

**Solution:**
1. Verify "Enable Sync" is ON in Settings
2. Check that "Use Stub API" is OFF
3. Ensure valid auth token (login first)
4. Check backend logs for incoming requests

### Issue: Authentication fails with real backend

**Solution:**
1. Verify backend returns correct response format (see API_DOCUMENTATION.md)
2. Check token is saved: `TokenManager.getToken()`
3. Verify Bearer token in Authorization header
4. Test with Postman first to isolate issue

### Issue: Stub data doesn't update

**Solution:**
Stub data is generated on initialization. To refresh:
1. Clear app data
2. Or restart the app
3. Or toggle stub mode off and back on

---

## Deployment Considerations

### App Release

1. **Disable stub mode by default** for production
2. **Remove or hide settings** for end users (optional)
3. **Configure production API URL** in build config
4. **Enable ProGuard** for release builds

Example build configuration:

```kotlin
android {
    buildTypes {
        release {
            buildConfigField("String", "API_BASE_URL", '"https://api.production.com/v1/"')
            buildConfigField("Boolean", "ALLOW_STUB_MODE", "false")
        }
        debug {
            buildConfigField("String", "API_BASE_URL", '"http://10.0.2.2:8000/v1/"')
            buildConfigField("Boolean", "ALLOW_STUB_MODE", "true")
        }
    }
}
```

### Backend Deployment

1. **SSL/TLS Required**: Android requires HTTPS for production
2. **Database Backups**: Schedule regular backups
3. **Monitoring**: Set up logging and monitoring (e.g., Sentry)
4. **Rate Limiting**: Prevent API abuse
5. **CORS**: Configure if needed for web client

---

## File Reference

| File | Purpose |
|------|---------|
| `ApiService.kt` | API interface definition |
| `StubApiService.kt` | Stub implementation (fake data) |
| `RealApiService.kt` | Real API with Retrofit |
| `ApiFactory.kt` | Creates API instances |
| `ApiConfig.kt` | Manages API settings |
| `TokenManager.kt` | Stores auth tokens |
| `TimerRepository.kt` | Coordinates local + remote data |
| `ApiModels.kt` | API request/response DTOs |
| `SettingsScreen.kt` | UI for toggling stub/real mode |

---

## Next Steps

1. **Start with stub mode** - Get familiar with the app
2. **Read API_DOCUMENTATION.md** - Understand expected endpoints
3. **Build your backend** - Choose your tech stack
4. **Test locally** - Use `10.0.2.2` for emulator or device IP
5. **Switch to real API** - Configure in Settings
6. **Deploy** - Backend to cloud, app to Play Store

## Support

Need help? Check:
- Main README.md for app overview
- API_DOCUMENTATION.md for endpoint specs
- Source code comments for implementation details
- StubApiService.kt to see expected behavior

Good luck building! 🚀
