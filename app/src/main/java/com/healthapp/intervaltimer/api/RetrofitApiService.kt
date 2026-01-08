package com.healthapp.intervaltimer.api

import com.healthapp.intervaltimer.api.models.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API Interface
 * Define HTTP endpoints for real backend
 */
interface RetrofitApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<LoginResponse>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @POST("sessions/sync")
    suspend fun syncSessions(@Body request: SyncSessionsRequest): Response<ApiResponse<SyncSessionsResponse>>

    @GET("sessions")
    suspend fun fetchUserSessions(): Response<ApiResponse<List<SessionDto>>>

    @GET("stats")
    suspend fun getUserStats(): Response<ApiResponse<UserStatsResponse>>

    @GET("leaderboard")
    suspend fun getLeaderboard(@Query("limit") limit: Int): Response<ApiResponse<LeaderboardResponse>>

    @GET("ping")
    suspend fun ping(): Response<ApiResponse<Boolean>>
}

/**
 * Real API implementation using Retrofit
 */
class RealApiService(
    private val retrofitService: RetrofitApiService,
    private val tokenManager: TokenManager
) : ApiService {

    override suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = retrofitService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val loginResponse = response.body()?.data!!
                tokenManager.saveToken(loginResponse.token)
                Result.success(loginResponse)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, name: String): Result<LoginResponse> {
        return try {
            val response = retrofitService.register(RegisterRequest(email, password, name))
            if (response.isSuccessful && response.body()?.success == true) {
                val loginResponse = response.body()?.data!!
                tokenManager.saveToken(loginResponse.token)
                Result.success(loginResponse)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            val response = retrofitService.logout()
            tokenManager.clearToken()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Logout failed"))
            }
        } catch (e: Exception) {
            tokenManager.clearToken()
            Result.failure(e)
        }
    }

    override suspend fun syncSessions(sessions: List<SessionDto>): Result<SyncSessionsResponse> {
        return try {
            val response = retrofitService.syncSessions(SyncSessionsRequest(sessions))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Sync failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchUserSessions(): Result<List<SessionDto>> {
        return try {
            val response = retrofitService.fetchUserSessions()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Fetch failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserStats(): Result<UserStatsResponse> {
        return try {
            val response = retrofitService.getUserStats()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to fetch stats"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLeaderboard(limit: Int): Result<LeaderboardResponse> {
        return try {
            val response = retrofitService.getLeaderboard(limit)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to fetch leaderboard"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun ping(): Result<Boolean> {
        return try {
            val response = retrofitService.ping()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: false)
            } else {
                Result.failure(Exception("Ping failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
