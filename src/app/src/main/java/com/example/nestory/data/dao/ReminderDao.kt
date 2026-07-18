package com.example.nestory.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.example.nestory.data.entity.ReminderEntity

@Dao
interface ReminderDao {

    @Insert
    suspend fun insert(reminder: ReminderEntity): Long

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders")
    fun getAll(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<ReminderEntity?>

    @Query("SELECT * FROM reminders WHERE document_id = :documentId LIMIT 1")
    fun getByDocumentId(documentId: Long): Flow<ReminderEntity?>

    @Query("SELECT * FROM reminders WHERE document_kit_id = :documentKitId LIMIT 1")
    fun getByDocumentKitId(documentKitId: Long): Flow<ReminderEntity?>

    @Query(" SELECT * FROM reminders WHERE is_enabled = 1")
    fun getEnabled(): Flow<List<ReminderEntity>>
    
}