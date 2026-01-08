package com.healthapp.intervaltimer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerSessionDao {
    @Insert
    suspend fun insert(session: TimerSession): Long

    @Update
    suspend fun update(session: TimerSession)

    @Query("SELECT * FROM timer_sessions ORDER BY startTime DESC LIMIT 50")
    fun getRecentSessions(): Flow<List<TimerSession>>

    @Query("SELECT * FROM timer_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): TimerSession?

    @Query("SELECT COUNT(*) FROM timer_sessions WHERE endTime IS NOT NULL")
    fun getTotalCompletedSessions(): Flow<Int>

    @Query("SELECT SUM(completedCycles) FROM timer_sessions")
    fun getTotalCompletedCycles(): Flow<Int>
}
