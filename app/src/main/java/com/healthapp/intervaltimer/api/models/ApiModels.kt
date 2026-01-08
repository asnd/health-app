package com.healthapp.intervaltimer.api.models

import com.google.gson.annotations.SerializedName

/**
 * API Data Transfer Objects (DTOs)
 */

// User authentication
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("email") val email: String
)

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("name") val name: String
)

// Session sync
data class SessionDto(
    @SerializedName("id") val id: Long?,
    @SerializedName("start_time") val startTime: Long,
    @SerializedName("end_time") val endTime: Long?,
    @SerializedName("activity_minutes") val activityMinutes: Int,
    @SerializedName("rest_minutes") val restMinutes: Int,
    @SerializedName("completed_cycles") val completedCycles: Int,
    @SerializedName("manually_ended") val manuallyEnded: Boolean,
    @SerializedName("synced") val synced: Boolean = false
)

data class SyncSessionsRequest(
    @SerializedName("sessions") val sessions: List<SessionDto>
)

data class SyncSessionsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("synced_count") val syncedCount: Int,
    @SerializedName("message") val message: String?
)

// User statistics
data class UserStatsResponse(
    @SerializedName("user_id") val userId: String,
    @SerializedName("total_sessions") val totalSessions: Int,
    @SerializedName("total_cycles") val totalCycles: Int,
    @SerializedName("total_activity_minutes") val totalActivityMinutes: Int,
    @SerializedName("total_rest_minutes") val totalRestMinutes: Int,
    @SerializedName("average_session_duration") val averageSessionDuration: Int,
    @SerializedName("last_session_date") val lastSessionDate: Long?,
    @SerializedName("streak_days") val streakDays: Int
)

// Leaderboard
data class LeaderboardEntry(
    @SerializedName("user_id") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("total_cycles") val totalCycles: Int,
    @SerializedName("rank") val rank: Int
)

data class LeaderboardResponse(
    @SerializedName("entries") val entries: List<LeaderboardEntry>,
    @SerializedName("user_rank") val userRank: Int?
)

// Generic API response wrapper
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: T?,
    @SerializedName("error") val error: String?
)

// Error response
data class ApiError(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String
)
