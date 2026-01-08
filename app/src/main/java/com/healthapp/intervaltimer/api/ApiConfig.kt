package com.healthapp.intervaltimer.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * API Configuration Manager
 * Controls whether to use stub or real API
 */
private val Context.apiConfigDataStore: DataStore<Preferences> by preferencesDataStore(name = "api_config")

class ApiConfig(private val context: Context) {

    companion object {
        private val USE_STUB_API_KEY = booleanPreferencesKey("use_stub_api")
        private val API_BASE_URL_KEY = stringPreferencesKey("api_base_url")
        private val SYNC_ENABLED_KEY = booleanPreferencesKey("sync_enabled")

        const val DEFAULT_BASE_URL = "https://api.example.com/v1/"
    }

    suspend fun setUseStubApi(useStub: Boolean) {
        context.apiConfigDataStore.edit { preferences ->
            preferences[USE_STUB_API_KEY] = useStub
        }
    }

    suspend fun isUsingStubApi(): Boolean {
        return context.apiConfigDataStore.data.map { preferences ->
            preferences[USE_STUB_API_KEY] ?: true // Default to stub for safety
        }.first()
    }

    fun isUsingStubApiFlow(): Flow<Boolean> {
        return context.apiConfigDataStore.data.map { preferences ->
            preferences[USE_STUB_API_KEY] ?: true
        }
    }

    suspend fun setBaseUrl(url: String) {
        context.apiConfigDataStore.edit { preferences ->
            preferences[API_BASE_URL_KEY] = url
        }
    }

    suspend fun getBaseUrl(): String {
        return context.apiConfigDataStore.data.map { preferences ->
            preferences[API_BASE_URL_KEY] ?: DEFAULT_BASE_URL
        }.first()
    }

    suspend fun setSyncEnabled(enabled: Boolean) {
        context.apiConfigDataStore.edit { preferences ->
            preferences[SYNC_ENABLED_KEY] = enabled
        }
    }

    suspend fun isSyncEnabled(): Boolean {
        return context.apiConfigDataStore.data.map { preferences ->
            preferences[SYNC_ENABLED_KEY] ?: false // Default to disabled
        }.first()
    }

    fun isSyncEnabledFlow(): Flow<Boolean> {
        return context.apiConfigDataStore.data.map { preferences ->
            preferences[SYNC_ENABLED_KEY] ?: false
        }
    }
}
