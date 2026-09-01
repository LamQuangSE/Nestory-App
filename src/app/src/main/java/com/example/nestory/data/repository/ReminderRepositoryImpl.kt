package com.example.nestory.data.repository

import android.content.Context
import com.example.nestory.data.local.dao.ReminderDao
import com.example.nestory.data.local.entity.ReminderEntity
import com.example.nestory.domain.repository.ReminderRepository
import com.example.nestory.utils.notification.ReminderScheduler
import kotlinx.coroutines.flow.Flow

class ReminderRepositoryImpl(
    private val reminderDao: ReminderDao,
    context: Context
) : ReminderRepository {

    private val scheduler = ReminderScheduler(context)

    override fun observeAllReminders(): Flow<List<ReminderEntity>> =
        reminderDao.observeAll()

    override suspend fun getAllReminders(): Result<List<ReminderEntity>> =
        runCatching { reminderDao.getAll() }

    override fun observeReminderById(id: Long): Flow<ReminderEntity?> =
        reminderDao.observeById(id)

    override suspend fun getReminderById(id: Long): Result<ReminderEntity?> =
        runCatching { reminderDao.getById(id) }

    override fun observeReminderByDocumentId(documentId: Long): Flow<ReminderEntity?> =
        reminderDao.observeByDocumentId(documentId)

    override suspend fun getReminderByDocumentId(documentId: Long): Result<ReminderEntity?> =
        runCatching { reminderDao.getByDocumentId(documentId) }

    override fun observeReminderByDocumentKitId(documentKitId: Long): Flow<ReminderEntity?> =
        reminderDao.observeByDocumentKitId(documentKitId)

    override suspend fun getReminderByDocumentKitId(documentKitId: Long): Result<ReminderEntity?> =
        runCatching { reminderDao.getByDocumentKitId(documentKitId) }

    override fun observeEnabledReminders(): Flow<List<ReminderEntity>> =
        reminderDao.observeEnabled()

    override suspend fun getEnabledReminders(): Result<List<ReminderEntity>> =
        runCatching { reminderDao.getEnabled() }

    override suspend fun createReminder(reminder: ReminderEntity): Result<Long> =
        runCatching {
            val id = reminderDao.insert(reminder)
            val inserted = reminder.copy(id = id)
            scheduler.schedule(inserted)
            id
        }

    override suspend fun updateReminder(reminder: ReminderEntity): Result<Unit> =
        runCatching {
            reminderDao.update(reminder)
            scheduler.schedule(reminder)
        }

    override suspend fun deleteReminder(reminder: ReminderEntity): Result<Unit> =
        runCatching {
            scheduler.cancel(reminder)
            reminderDao.delete(reminder)
        }
}
