package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityEventDao {
    @Query("SELECT * FROM security_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<SecurityEvent>>

    @Query("SELECT * FROM security_events WHERE eventType = :type ORDER BY timestamp DESC")
    fun getEventsByType(type: SecurityEventType): Flow<List<SecurityEvent>>

    @Query("SELECT * FROM security_events WHERE authStatus = :status ORDER BY timestamp DESC")
    fun getEventsByStatus(status: AuthResultStatus): Flow<List<SecurityEvent>>

    @Query("SELECT COUNT(*) FROM security_events")
    fun getEventCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: SecurityEvent): Long

    @Query("DELETE FROM security_events")
    suspend fun clearAllEvents()

    @Query("DELETE FROM security_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)
}
