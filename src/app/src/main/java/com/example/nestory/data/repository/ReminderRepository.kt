package com.example.nestory.data.repository

import com.example.nestory.data.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow
import kotlin.result.Result

interface ReminderRepository {
    // Observe all reminders
    fun observeAllReminders(): Flow<List<ReminderEntity>>

    // Get reminder by id
    fun getReminderById(id: Long): Flow<ReminderEntity?>

    // Get reminder by document id
    fun getReminderByDocumentId(documentId: Long): Flow<ReminderEntity?>

    // Get reminder by document kit id
    fun getReminderByDocumentKitId(documentKitId: Long): Flow<ReminderEntity?>

    // Get upcoming reminders (based on reminder time/date)
    fun getUpcomingReminders(): Flow<List<ReminderEntity>>

    // CRUD operations
    suspend fun createReminder(reminder: ReminderEntity): Result<Long>
    suspend fun updateReminder(reminder: ReminderEntity): Result<Unit>
    suspend fun deleteReminder(reminder: ReminderEntity): Result<Unit>
}