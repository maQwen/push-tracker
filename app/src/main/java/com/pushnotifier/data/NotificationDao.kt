package com.pushnotifier.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY postedTime DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert
    suspend fun insert(notification: NotificationEntity): Long

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(notification: NotificationEntity)
}
