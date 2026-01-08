package com.healthapp.intervaltimer.repository

import com.healthapp.intervaltimer.api.ApiService
import com.healthapp.intervaltimer.api.models.SessionDto
import com.healthapp.intervaltimer.api.models.UserStatsResponse
import com.healthapp.intervaltimer.data.TimerSession
import com.healthapp.intervaltimer.data.TimerSessionDao
import kotlinx.coroutines.flow.Flow
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
            // Get all local sessions
            val sessions = mutableListOf<TimerSession>()
            localDataSource.getRecentSessions().collect { sessionList ->
                sessions.addAll(sessionList)
            }

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

    fun getEnhancedStats(): Flow<EnhancedStats> = flow {
        // Get local stats
        var totalSessions = 0
        var totalCycles = 0

        localDataSource.getTotalCompletedSessions().collect { sessions ->
            totalSessions = sessions
        }
        localDataSource.getTotalCompletedCycles().collect { cycles ->
            totalCycles = cycles ?: 0
        }

        // Try to get remote stats if sync is enabled
        val remoteStats = if (syncEnabled) {
            fetchRemoteStats().getOrNull()
        } else null

        emit(
            EnhancedStats(
                localTotalSessions = totalSessions,
                localTotalCycles = totalCycles,
                remoteStats = remoteStats
            )
        )
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
