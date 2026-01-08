# Interval Timer API Documentation

This document describes the REST API endpoints that the Interval Timer Android app expects from the backend server.

## Base URL

Configurable in app settings. Default: `https://api.example.com/v1/`

## Authentication

All authenticated endpoints require a Bearer token in the Authorization header:

```
Authorization: Bearer <token>
```

The token is obtained from `/auth/login` or `/auth/register` endpoints.

## Response Format

All responses follow this structure:

```json
{
  "success": true,
  "data": { /* response data */ },
  "error": null
}
```

Error responses:

```json
{
  "success": false,
  "data": null,
  "error": "Error message here"
}
```

---

## Endpoints

### Authentication

#### POST /auth/register

Register a new user account.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "securepassword123",
  "name": "John Doe"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user_id": "user_123",
    "email": "user@example.com"
  },
  "error": null
}
```

**Error Codes:**
- `400` - Invalid input (email already exists, weak password, etc.)
- `500` - Server error

---

#### POST /auth/login

Authenticate an existing user.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "securepassword123"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user_id": "user_123",
    "email": "user@example.com"
  },
  "error": null
}
```

**Error Codes:**
- `401` - Invalid credentials
- `500` - Server error

---

#### POST /auth/logout

Logout the current user (invalidate token).

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": null,
  "error": null
}
```

---

### Session Management

#### POST /sessions/sync

Sync timer sessions from the app to the backend.

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "sessions": [
    {
      "id": 1,
      "start_time": 1704067200000,
      "end_time": 1704070800000,
      "activity_minutes": 30,
      "rest_minutes": 10,
      "completed_cycles": 3,
      "manually_ended": false,
      "synced": false
    }
  ]
}
```

**Field Descriptions:**
- `id`: Local database ID (nullable for new sessions)
- `start_time`: Unix timestamp in milliseconds
- `end_time`: Unix timestamp in milliseconds (null if session still running)
- `activity_minutes`: Activity interval duration
- `rest_minutes`: Rest interval duration
- `completed_cycles`: Number of complete activity/rest cycles
- `manually_ended`: Whether user stopped timer early
- `synced`: Whether session was previously synced

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "success": true,
    "synced_count": 1,
    "message": "Successfully synced 1 sessions"
  },
  "error": null
}
```

**Error Codes:**
- `400` - Invalid session data
- `401` - Unauthorized
- `500` - Server error

---

#### GET /sessions

Fetch all sessions for the authenticated user.

**Headers:**
```
Authorization: Bearer <token>
```

**Query Parameters:** (optional)
- `limit`: Max number of sessions to return (default: 50)
- `offset`: Pagination offset (default: 0)
- `start_date`: Filter sessions after this timestamp
- `end_date`: Filter sessions before this timestamp

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "start_time": 1704067200000,
      "end_time": 1704070800000,
      "activity_minutes": 30,
      "rest_minutes": 10,
      "completed_cycles": 3,
      "manually_ended": false,
      "synced": true
    }
  ],
  "error": null
}
```

---

### Statistics

#### GET /stats

Get comprehensive statistics for the authenticated user.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "user_id": "user_123",
    "total_sessions": 42,
    "total_cycles": 168,
    "total_activity_minutes": 5040,
    "total_rest_minutes": 1680,
    "average_session_duration": 120,
    "last_session_date": 1704067200000,
    "streak_days": 7
  },
  "error": null
}
```

**Field Descriptions:**
- `total_sessions`: All-time completed sessions
- `total_cycles`: Sum of all completed cycles
- `total_activity_minutes`: Cumulative activity time
- `total_rest_minutes`: Cumulative rest time
- `average_session_duration`: Average session length in minutes
- `last_session_date`: Unix timestamp of most recent session
- `streak_days`: Current consecutive days with sessions

---

### Leaderboard

#### GET /leaderboard

Get global leaderboard ranked by total cycles.

**Headers:**
```
Authorization: Bearer <token>
```

**Query Parameters:** (optional)
- `limit`: Number of entries to return (default: 50, max: 100)
- `period`: Time period filter: `all`, `week`, `month` (default: `all`)

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "entries": [
      {
        "user_id": "user_456",
        "name": "Jane Doe",
        "total_cycles": 500,
        "rank": 1
      },
      {
        "user_id": "user_123",
        "name": "John Doe",
        "total_cycles": 168,
        "rank": 2
      }
    ],
    "user_rank": 2
  },
  "error": null
}
```

**Field Descriptions:**
- `entries`: Array of leaderboard entries
- `user_rank`: Current user's rank in the leaderboard (null if not ranked)

---

### Health Check

#### GET /ping

Check if the API is alive.

**Response:** `200 OK`
```json
{
  "success": true,
  "data": true,
  "error": null
}
```

---

## Error Handling

### Standard Error Response

```json
{
  "success": false,
  "data": null,
  "error": "Descriptive error message"
}
```

### HTTP Status Codes

- `200` - Success
- `400` - Bad Request (invalid input)
- `401` - Unauthorized (missing or invalid token)
- `403` - Forbidden (valid token but insufficient permissions)
- `404` - Not Found
- `429` - Too Many Requests (rate limiting)
- `500` - Internal Server Error

---

## Implementation Examples

### Python (FastAPI)

```python
from fastapi import FastAPI, Depends, HTTPException
from pydantic import BaseModel
from typing import Optional, List

app = FastAPI()

class LoginRequest(BaseModel):
    email: str
    password: str

class ApiResponse(BaseModel):
    success: bool
    data: Optional[any] = None
    error: Optional[str] = None

@app.post("/auth/login")
async def login(request: LoginRequest):
    # Your authentication logic here
    if authenticate(request.email, request.password):
        token = generate_token(request.email)
        return ApiResponse(
            success=True,
            data={
                "token": token,
                "user_id": "user_123",
                "email": request.email
            }
        )
    raise HTTPException(status_code=401, detail="Invalid credentials")
```

### Node.js (Express)

```javascript
const express = require('express');
const app = express();

app.use(express.json());

app.post('/auth/login', async (req, res) => {
  const { email, password } = req.body;

  // Your authentication logic here
  const user = await authenticateUser(email, password);

  if (user) {
    const token = generateToken(user);
    res.json({
      success: true,
      data: {
        token: token,
        user_id: user.id,
        email: user.email
      },
      error: null
    });
  } else {
    res.status(401).json({
      success: false,
      data: null,
      error: 'Invalid credentials'
    });
  }
});
```

### Kotlin (Ktor)

```kotlin
fun Application.configureRouting() {
    routing {
        post("/auth/login") {
            val request = call.receive<LoginRequest>()

            // Your authentication logic here
            val user = authenticateUser(request.email, request.password)

            if (user != null) {
                val token = generateToken(user)
                call.respond(ApiResponse(
                    success = true,
                    data = LoginResponse(
                        token = token,
                        userId = user.id,
                        email = user.email
                    )
                ))
            } else {
                call.respond(HttpStatusCode.Unauthorized, ApiResponse(
                    success = false,
                    error = "Invalid credentials"
                ))
            }
        }
    }
}
```

---

## Database Schema Recommendations

### Users Table

```sql
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Sessions Table

```sql
CREATE TABLE sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(36) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    activity_minutes INT NOT NULL,
    rest_minutes INT NOT NULL,
    completed_cycles INT DEFAULT 0,
    manually_ended BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### Indexes

```sql
CREATE INDEX idx_sessions_user_id ON sessions(user_id);
CREATE INDEX idx_sessions_start_time ON sessions(start_time);
CREATE INDEX idx_users_email ON users(email);
```

---

## Rate Limiting

Recommended rate limits:
- Authentication endpoints: 5 requests per minute per IP
- Session sync: 30 requests per minute per user
- Stats/Leaderboard: 60 requests per minute per user

---

## Security Considerations

1. **Password Storage**: Use bcrypt or Argon2 for password hashing
2. **JWT Tokens**: Include expiration time, use strong secret
3. **HTTPS Only**: All API calls must use HTTPS in production
4. **Input Validation**: Validate all inputs server-side
5. **SQL Injection**: Use parameterized queries
6. **CORS**: Configure appropriate CORS headers
7. **Rate Limiting**: Implement rate limiting to prevent abuse

---

## Testing with Stub API

The Android app includes a stub implementation that can be used for testing without a backend:

```kotlin
// In your tests or during development
val apiService = StubApiService(tokenManager)

// Stub provides realistic fake data with network delays
val result = apiService.getUserStats()
```

The stub automatically generates:
- 10 fake sessions over the past 10 days
- Random statistics
- Fake leaderboard entries

---

## Support

For questions or issues with the API integration:
- Check the app's `StubApiService.kt` for expected behavior
- Review `ApiModels.kt` for complete DTO definitions
- Test with stub mode first before implementing real backend
