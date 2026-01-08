package com.healthapp.intervaltimer.ui

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthapp.intervaltimer.IntervalTimerApplication
import com.healthapp.intervaltimer.data.TimerConfig
import com.healthapp.intervaltimer.data.TimerPhase
import com.healthapp.intervaltimer.data.TimerSession
import com.healthapp.intervaltimer.worker.TimerForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TimerUiState(
    val config: TimerConfig = TimerConfig(),
    val isRunning: Boolean = false,
    val currentPhase: TimerPhase = TimerPhase.IDLE,
    val recentSessions: List<TimerSession> = emptyList(),
    val totalCompletedSessions: Int = 0,
    val totalCompletedCycles: Int = 0
)

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as IntervalTimerApplication).repository

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var currentSessionId: Long? = null

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            repository.getRecentSessions().collect { sessions ->
                _uiState.value = _uiState.value.copy(recentSessions = sessions)
            }
        }
        viewModelScope.launch {
            repository.getTotalCompletedSessions().collect { count ->
                _uiState.value = _uiState.value.copy(totalCompletedSessions = count)
            }
        }
        viewModelScope.launch {
            repository.getTotalCompletedCycles().collect { count ->
                _uiState.value = _uiState.value.copy(totalCompletedCycles = count ?: 0)
            }
        }
    }

    fun updateActivityMinutes(minutes: Int) {
        _uiState.value = _uiState.value.copy(
            config = _uiState.value.config.copy(activityMinutes = minutes)
        )
    }

    fun updateRestMinutes(minutes: Int) {
        _uiState.value = _uiState.value.copy(
            config = _uiState.value.config.copy(restMinutes = minutes)
        )
    }

    fun startTimer() {
        if (!_uiState.value.config.isValid()) return

        viewModelScope.launch {
            val session = TimerSession(
                startTime = System.currentTimeMillis(),
                activityMinutes = _uiState.value.config.activityMinutes,
                restMinutes = _uiState.value.config.restMinutes
            )
            currentSessionId = repository.insertSession(session)
        }

        val intent = Intent(getApplication(), TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_START_TIMER
            putExtra(
                TimerForegroundService.EXTRA_ACTIVITY_MINUTES,
                _uiState.value.config.activityMinutes
            )
            putExtra(
                TimerForegroundService.EXTRA_REST_MINUTES,
                _uiState.value.config.restMinutes
            )
        }

        ContextCompat.startForegroundService(getApplication(), intent)
        _uiState.value = _uiState.value.copy(
            isRunning = true,
            currentPhase = TimerPhase.ACTIVITY
        )
    }

    fun stopTimer() {
        currentSessionId?.let { sessionId ->
            viewModelScope.launch {
                val session = repository.getSessionById(sessionId)
                session?.let {
                    repository.updateSession(
                        it.copy(
                            endTime = System.currentTimeMillis(),
                            manuallyEnded = true
                        )
                    )
                }
            }
        }

        val intent = Intent(getApplication(), TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_STOP_TIMER
        }
        getApplication<Application>().startService(intent)

        _uiState.value = _uiState.value.copy(
            isRunning = false,
            currentPhase = TimerPhase.IDLE
        )
        currentSessionId = null
    }
}
