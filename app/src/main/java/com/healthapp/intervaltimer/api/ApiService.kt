package com.healthapp.intervaltimer.api

import com.healthapp.intervaltimer.api.models.*

/**
 * API Service Interface
 * Defines all backend operations
 */
interface ApiService {

    // Authentication
    suspend fun login(email: String, password: String): Result<LoginResponse>
    suspend fun register(email: String, password: String, name: String): Result<LoginResponse>
    suspend fun logout(): Result<Unit>

    // Session sync
    suspend fun syncSessions(sessions: List<SessionDto>): Result<SyncSessionsResponse>
    suspend fun fetchUserSessions(): Result<List<SessionDto>>

    // Statistics
    suspend fun getUserStats(): Result<UserStatsResponse>
    suspend fun getLeaderboard(limit: Int = 50): Result<LeaderboardResponse>

    // Health check
    suspend fun ping(): Result<Boolean>
}

/**
 * Result wrapper for API operations
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val exception: Exception, val message: String) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
}
