package com.example.nestory.data.repository

import com.example.nestory.data.local.dao.BackupRecordDao
import com.example.nestory.data.local.entity.BackupRecordEntity
import com.example.nestory.domain.repository.BackupRecordRepository
import kotlinx.coroutines.flow.Flow

class BackupRecordRepositoryImpl(
    private val backupRecordDao: BackupRecordDao,
) : BackupRecordRepository {

    override suspend fun createBackupRecord(record: BackupRecordEntity): Result<Long> =
        runCatching { backupRecordDao.insertBackupRecord(record) }

    override fun observeBackupHistory(): Flow<List<BackupRecordEntity>> =
        backupRecordDao.observeAllBackupRecords()

    override suspend fun getBackupHistory(): Result<List<BackupRecordEntity>> =
        runCatching { backupRecordDao.getAllBackupRecords() }

    override fun observeLatestBackup(): Flow<BackupRecordEntity?> =
        backupRecordDao.observeLatestBackup()

    override suspend fun getLatestBackup(): Result<BackupRecordEntity?> =
        runCatching { backupRecordDao.getLatestBackup() }

    override suspend fun deleteBackupRecord(record: BackupRecordEntity): Result<Unit> =
        runCatching { backupRecordDao.deleteBackupRecord(record) }

    override suspend fun updateBackupRecord(record: BackupRecordEntity): Result<Unit> =
        runCatching { backupRecordDao.updateBackupRecord(record) }
}
