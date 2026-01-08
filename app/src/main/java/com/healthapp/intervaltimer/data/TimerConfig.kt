package com.healthapp.intervaltimer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Configuration for interval timer
 */
data class TimerConfig(
    val activityMinutes: Int = 30,
    val restMinutes: Int = 10
) {
    fun isValid(): Boolean {
        return activityMinutes in 10..120 && restMinutes in 5..180
    }
}

/**
 * Timer state
 */
enum class TimerPhase {
    IDLE,
    ACTIVITY,
    REST
}

/**
 * Timer session data for history tracking
 */
@Entity(tableName = "timer_sessions")
data class TimerSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val activityMinutes: Int,
    val restMinutes: Int,
    val completedCycles: Int = 0,
    val manuallyEnded: Boolean = false
)
