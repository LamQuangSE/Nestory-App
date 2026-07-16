package com.example.nestory.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
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
    suspend fun getAll(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE document_id = :documentId LIMIT 1")
    suspend fun getByDocumentId(documentId: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE document_kit_id = :documentKitId LIMIT 1")
    suspend fun getByDocumentKitId(documentKitId: Long): ReminderEntity?
}