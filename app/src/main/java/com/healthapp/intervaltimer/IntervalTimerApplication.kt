package com.healthapp.intervaltimer

import android.app.Application
import com.healthapp.intervaltimer.api.ApiConfig
import com.healthapp.intervaltimer.api.ApiFactory
import com.healthapp.intervaltimer.data.TimerDatabase
import com.healthapp.intervaltimer.notifications.NotificationHelper
import com.healthapp.intervaltimer.repository.TimerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class IntervalTimerApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: TimerDatabase by lazy { TimerDatabase.getDatabase(this) }
    val apiConfig: ApiConfig by lazy { ApiConfig(this) }

    val repository: TimerRepository by lazy {
        var syncEnabled = false
        applicationScope.launch {
            syncEnabled = apiConfig.isSyncEnabled()
        }

        TimerRepository(
            localDataSource = database.timerSessionDao(),
            remoteDataSource = ApiFactory.createApiService(
                context = this,
                useStub = true // Default to stub, can be changed in settings
            ),
            syncEnabled = syncEnabled
        )
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)

        // Initialize with stub API by default
        applicationScope.launch {
            if (!apiConfig.isUsingStubApiFlow().toString().contains("true")) {
                apiConfig.setUseStubApi(true)
            }
        }
    }
}
