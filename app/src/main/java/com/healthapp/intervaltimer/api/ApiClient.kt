package com.healthapp.intervaltimer.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit client builder for real API calls
 */
object ApiClient {

    private const val DEFAULT_BASE_URL = "https://api.example.com/v1/" // Change to your backend URL

    fun create(
        baseUrl: String = DEFAULT_BASE_URL,
        tokenManager: TokenManager,
        enableLogging: Boolean = true
    ): RetrofitApiService {
        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val token = kotlin.runCatching {
                kotlinx.coroutines.runBlocking { tokenManager.getToken() }
            }.getOrNull()

            val requestBuilder = originalRequest.newBuilder()
            token?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }

            chain.proceed(requestBuilder.build())
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (enableLogging) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(RetrofitApiService::class.java)
    }
}
