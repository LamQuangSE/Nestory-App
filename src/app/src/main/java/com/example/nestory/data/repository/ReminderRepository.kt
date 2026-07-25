package com.example.nestory.data.repository

import com.example.nestory.data.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun observeAllReminders(): Flow<List<ReminderEntity>>
    suspend fun getAllReminders(): Result<List<ReminderEntity>>

    fun observeReminderById(id: Long): Flow<ReminderEntity?>
    suspend fun getReminderById(id: Long): Result<ReminderEntity?>

    fun observeReminderByDocumentId(documentId: Long): Flow<ReminderEntity?>
    suspend fun getReminderByDocumentId(documentId: Long): Result<ReminderEntity?>

    fun observeReminderByDocumentKitId(documentKitId: Long): Flow<ReminderEntity?>
    suspend fun getReminderByDocumentKitId(documentKitId: Long): Result<ReminderEntity?>

    fun observeEnabledReminders(): Flow<List<ReminderEntity>>
    suspend fun getEnabledReminders(): Result<List<ReminderEntity>>

    suspend fun createReminder(reminder: ReminderEntity): Result<Long>
    suspend fun updateReminder(reminder: ReminderEntity): Result<Unit>
    suspend fun deleteReminder(reminder: ReminderEntity): Result<Unit>
}
