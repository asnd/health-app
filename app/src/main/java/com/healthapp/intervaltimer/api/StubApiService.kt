package com.healthapp.intervaltimer.api

import com.healthapp.intervaltimer.api.models.*
import kotlinx.coroutines.delay
import java.util.*
import kotlin.random.Random

/**
 * Stub/Mock API implementation for testing without a real backend
 * Simulates API responses with fake data and network delays
 */
class StubApiService(private val tokenManager: TokenManager) : ApiService {

    private val stubUserId = "stub_user_123"
    private val stubToken = "stub_token_${UUID.randomUUID()}"
    private val stubSessions = mutableListOf<SessionDto>()

    init {
        // Pre-populate with some fake sessions
        generateFakeSessions()
    }

    private fun generateFakeSessions() {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L

        for (i in 1..10) {
            val startTime = now - (i * oneDayMs) - Random.nextLong(0, oneDayMs)
            val duration = Random.nextInt(30, 180) * 60 * 1000L
            stubSessions.add(
                SessionDto(
                    id = i.toLong(),
                    startTime = startTime,
                    endTime = startTime + duration,
                    activityMinutes = Random.nextInt(20, 60),
                    restMinutes = Random.nextInt(10, 30),
                    completedCycles = Random.nextInt(2, 8),
                    manuallyEnded = Random.nextBoolean(),
                    synced = true
                )
            )
        }
    }

    private suspend fun simulateNetworkDelay() {
        delay(Random.nextLong(300, 1000)) // Simulate 300ms-1s network delay
    }

    override suspend fun login(email: String, password: String): Result<LoginResponse> {
        simulateNetworkDelay()

        return if (email.isNotEmpty() && password.length >= 6) {
            tokenManager.saveToken(stubToken)
            Result.success(
                LoginResponse(
                    token = stubToken,
                    userId = stubUserId,
                    email = email
                )
            )
        } else {
            Result.failure(Exception("Invalid credentials"))
        }
    }

    override suspend fun register(email: String, password: String, name: String): Result<LoginResponse> {
        simulateNetworkDelay()

        return if (email.contains("@") && password.length >= 6 && name.isNotEmpty()) {
            tokenManager.saveToken(stubToken)
            Result.success(
                LoginResponse(
                    token = stubToken,
                    userId = stubUserId,
                    email = email
                )
            )
        } else {
            Result.failure(Exception("Invalid registration data"))
        }
    }

    override suspend fun logout(): Result<Unit> {
        simulateNetworkDelay()
        tokenManager.clearToken()
        return Result.success(Unit)
    }

    override suspend fun syncSessions(sessions: List<SessionDto>): Result<SyncSessionsResponse> {
        simulateNetworkDelay()

        // Add new sessions to our stub storage
        sessions.forEach { newSession ->
            val exists = stubSessions.any { it.id == newSession.id }
            if (!exists && newSession.id != null) {
                stubSessions.add(newSession.copy(synced = true))
            }
        }

        return Result.success(
            SyncSessionsResponse(
                success = true,
                syncedCount = sessions.size,
                message = "Successfully synced ${sessions.size} sessions"
            )
        )
    }

    override suspend fun fetchUserSessions(): Result<List<SessionDto>> {
        simulateNetworkDelay()
        return Result.success(stubSessions.sortedByDescending { it.startTime })
    }

    override suspend fun getUserStats(): Result<UserStatsResponse> {
        simulateNetworkDelay()

        val totalCycles = stubSessions.sumOf { it.completedCycles }
        val totalActivityMinutes = stubSessions.sumOf {
            it.activityMinutes * it.completedCycles
        }
        val totalRestMinutes = stubSessions.sumOf {
            it.restMinutes * it.completedCycles
        }
        val avgDuration = if (stubSessions.isNotEmpty()) {
            stubSessions.mapNotNull { session ->
                session.endTime?.let { it - session.startTime }
            }.average().toInt() / (60 * 1000)
        } else 0

        return Result.success(
            UserStatsResponse(
                userId = stubUserId,
                totalSessions = stubSessions.size,
                totalCycles = totalCycles,
                totalActivityMinutes = totalActivityMinutes,
                totalRestMinutes = totalRestMinutes,
                averageSessionDuration = avgDuration,
                lastSessionDate = stubSessions.maxOfOrNull { it.startTime },
                streakDays = Random.nextInt(1, 15)
            )
        )
    }

    override suspend fun getLeaderboard(limit: Int): Result<LeaderboardResponse> {
        simulateNetworkDelay()

        val fakeNames = listOf(
            "Alex", "Jordan", "Morgan", "Casey", "Riley",
            "Sam", "Taylor", "Drew", "Quinn", "Avery"
        )

        val entries = fakeNames.mapIndexed { index, name ->
            LeaderboardEntry(
                userId = "user_$index",
                name = name,
                totalCycles = Random.nextInt(50, 500),
                rank = index + 1
            )
        }.sortedByDescending { it.totalCycles }
            .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
            .take(limit)

        return Result.success(
            LeaderboardResponse(
                entries = entries,
                userRank = Random.nextInt(1, 100)
            )
        )
    }

    override suspend fun ping(): Result<Boolean> {
        simulateNetworkDelay()
        return Result.success(true)
    }
}
