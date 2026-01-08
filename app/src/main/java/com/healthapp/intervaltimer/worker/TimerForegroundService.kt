package com.healthapp.intervaltimer.worker

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.healthapp.intervaltimer.data.TimerPhase
import com.healthapp.intervaltimer.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var timerJob: Job? = null

    private var currentPhase = TimerPhase.IDLE
    private var activityMinutes = 30
    private var restMinutes = 10
    private var remainingSeconds = 0

    override fun onCreate() {
        super.onCreate()
        startForeground(
            NotificationHelper.NOTIFICATION_ID,
            NotificationHelper.createTimerNotification(this, TimerPhase.IDLE, 0)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> {
                activityMinutes = intent.getIntExtra(EXTRA_ACTIVITY_MINUTES, 30)
                restMinutes = intent.getIntExtra(EXTRA_REST_MINUTES, 10)
                startTimer()
            }
            ACTION_STOP_TIMER -> {
                stopTimer()
            }
        }
        return START_STICKY
    }

    private fun startTimer() {
        timerJob?.cancel()
        currentPhase = TimerPhase.ACTIVITY
        remainingSeconds = activityMinutes * 60

        timerJob = serviceScope.launch {
            while (remainingSeconds > 0) {
                updateNotification()
                delay(1000)
                remainingSeconds--

                if (remainingSeconds <= 0) {
                    switchPhase()
                }
            }
        }
    }

    private fun switchPhase() {
        currentPhase = when (currentPhase) {
            TimerPhase.ACTIVITY -> {
                remainingSeconds = restMinutes * 60
                NotificationHelper.showPhaseChangeNotification(this, TimerPhase.REST)
                TimerPhase.REST
            }
            TimerPhase.REST -> {
                remainingSeconds = activityMinutes * 60
                NotificationHelper.showPhaseChangeNotification(this, TimerPhase.ACTIVITY)
                TimerPhase.ACTIVITY
            }
            TimerPhase.IDLE -> TimerPhase.ACTIVITY
        }
    }

    private fun updateNotification() {
        val notification = NotificationHelper.createTimerNotification(
            this,
            currentPhase,
            remainingSeconds / 60
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NotificationHelper.NOTIFICATION_ID, notification)
    }

    private fun stopTimer() {
        timerJob?.cancel()
        currentPhase = TimerPhase.IDLE
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
    }

    companion object {
        const val ACTION_START_TIMER = "com.healthapp.intervaltimer.START_TIMER"
        const val ACTION_STOP_TIMER = "com.healthapp.intervaltimer.STOP_TIMER"
        const val EXTRA_ACTIVITY_MINUTES = "activity_minutes"
        const val EXTRA_REST_MINUTES = "rest_minutes"
    }
}
