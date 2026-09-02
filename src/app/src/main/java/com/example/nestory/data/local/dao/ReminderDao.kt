package com.example.nestory.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.nestory.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Insert
    suspend fun insert(reminder: ReminderEntity): Long

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders")
    fun observeAll(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders")
    suspend fun getAll(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<ReminderEntity?>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE document_id = :documentId ORDER BY id DESC LIMIT 1")
    fun observeByDocumentId(documentId: Long): Flow<ReminderEntity?>

    @Query("SELECT * FROM reminders WHERE document_id = :documentId ORDER BY id DESC LIMIT 1")
    suspend fun getByDocumentId(documentId: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE document_kit_id = :documentKitId ORDER BY id DESC LIMIT 1")
    fun observeByDocumentKitId(documentKitId: Long): Flow<ReminderEntity?>

    @Query("SELECT * FROM reminders WHERE document_kit_id = :documentKitId ORDER BY id DESC LIMIT 1")
    suspend fun getByDocumentKitId(documentKitId: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE is_enabled = 1")
    fun observeEnabled(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE is_enabled = 1")
    suspend fun getEnabled(): List<ReminderEntity>
}
