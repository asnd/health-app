package com.healthapp.intervaltimer.api

import android.content.Context

/**
 * Factory for creating API service instances
 * Handles switching between stub and real implementations
 */
object ApiFactory {

    private var cachedApiService: ApiService? = null
    private var cachedTokenManager: TokenManager? = null

    fun createApiService(
        context: Context,
        useStub: Boolean = true,
        baseUrl: String = ApiConfig.DEFAULT_BASE_URL
    ): ApiService {
        val tokenManager = getTokenManager(context)

        return if (useStub) {
            StubApiService(tokenManager)
        } else {
            val retrofitService = ApiClient.create(
                baseUrl = baseUrl,
                tokenManager = tokenManager,
                enableLogging = true // Set to false in production
            )
            RealApiService(retrofitService, tokenManager)
        }
    }

    fun getTokenManager(context: Context): TokenManager {
        return cachedTokenManager ?: TokenManager(context.applicationContext).also {
            cachedTokenManager = it
        }
    }

    suspend fun getConfiguredApiService(context: Context): ApiService {
        val apiConfig = ApiConfig(context)
        val useStub = apiConfig.isUsingStubApi()
        val baseUrl = apiConfig.getBaseUrl()

        return createApiService(context, useStub, baseUrl)
    }

    fun clearCache() {
        cachedApiService = null
        cachedTokenManager = null
    }
}
