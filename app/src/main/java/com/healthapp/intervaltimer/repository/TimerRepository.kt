package com.healthapp.intervaltimer.repository

import com.healthapp.intervaltimer.api.ApiService
import com.healthapp.intervaltimer.api.models.SessionDto
import com.healthapp.intervaltimer.api.models.UserStatsResponse
import com.healthapp.intervaltimer.data.TimerSession
import com.healthapp.intervaltimer.data.TimerSessionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * Repository that coordinates between local database and remote API
 * Implements offline-first approach with sync capabilities
 */
class TimerRepository(
    private val localDataSource: TimerSessionDao,
    private val remoteDataSource: ApiService,
    private val syncEnabled: Boolean = false
) {

    // Local database operations
    suspend fun insertSession(session: TimerSession): Long {
        val sessionId = localDataSource.insert(session)

        // Optionally sync to backend
        if (syncEnabled) {
            syncSessionToBackend(session.copy(id = sessionId))
        }

        return sessionId
    }

    suspend fun updateSession(session: TimerSession) {
        localDataSource.update(session)

        // Optionally sync to backend
        if (syncEnabled) {
            syncSessionToBackend(session)
        }
    }

    fun getRecentSessions(): Flow<List<TimerSession>> {
        return localDataSource.getRecentSessions()
    }

    suspend fun getSessionById(id: Long): TimerSession? {
        return localDataSource.getSessionById(id)
    }

    fun getTotalCompletedSessions(): Flow<Int> {
        return localDataSource.getTotalCompletedSessions()
    }

    fun getTotalCompletedCycles(): Flow<Int> {
        return localDataSource.getTotalCompletedCycles()
    }

    // API operations
    suspend fun syncAllSessions(): Result<Int> {
        if (!syncEnabled) {
            return Result.failure(Exception("Sync is disabled"))
        }

        return try {
            // Get all local sessions (snapshot)
            val sessions = localDataSource.getRecentSessions().first()

            // Convert to DTOs
            val sessionDtos = sessions.map { it.toDto() }

            // Sync to backend
            val result = remoteDataSource.syncSessions(sessionDtos)

            if (result.isSuccess) {
                val response = result.getOrNull()
                Result.success(response?.syncedCount ?: 0)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Sync failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchRemoteStats(): Result<UserStatsResponse> {
        if (!syncEnabled) {
            return Result.failure(Exception("Sync is disabled"))
        }

        return remoteDataSource.getUserStats()
    }

    fun getEnhancedStats(): Flow<EnhancedStats> {
        return combine(
            localDataSource.getTotalCompletedSessions(),
            localDataSource.getTotalCompletedCycles()
        ) { totalSessions, totalCycles ->
            // Try to get remote stats if sync is enabled
            // Note: This makes the flow suspend on network call, which isn't ideal for a UI flow.
            // A better approach would be to fetch remote stats separately or cache them.
            // For now, we'll return null for remote stats in the flow and let the ViewModel fetch them.
            // Or we could use a separate suspend function to refresh remote stats.
            
            EnhancedStats(
                localTotalSessions = totalSessions,
                localTotalCycles = totalCycles ?: 0,
                remoteStats = null // Placeholder, actual remote stats fetching should be separate or non-blocking
            )
        }
    }

    private suspend fun syncSessionToBackend(session: TimerSession) {
        try {
            remoteDataSource.syncSessions(listOf(session.toDto()))
        } catch (e: Exception) {
            // Log error but don't fail the local operation
            android.util.Log.e("TimerRepository", "Failed to sync session", e)
        }
    }

    // Helper functions
    private fun TimerSession.toDto() = SessionDto(
        id = this.id,
        startTime = this.startTime,
        endTime = this.endTime,
        activityMinutes = this.activityMinutes,
        restMinutes = this.restMinutes,
        completedCycles = this.completedCycles,
        manuallyEnded = this.manuallyEnded,
        synced = false
    )
}

data class EnhancedStats(
    val localTotalSessions: Int,
    val localTotalCycles: Int,
    val remoteStats: UserStatsResponse?
)
