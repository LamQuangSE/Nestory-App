package com.example.nestory.domain.repository

import com.example.nestory.data.local.entity.BackupRecordEntity
import kotlinx.coroutines.flow.Flow

interface BackupRecordRepository {
    suspend fun createBackupRecord(record: BackupRecordEntity): Result<Long>
    fun observeBackupHistory(): Flow<List<BackupRecordEntity>>
    suspend fun getBackupHistory(): Result<List<BackupRecordEntity>>
    fun observeLatestBackup(): Flow<BackupRecordEntity?>
    suspend fun getLatestBackup(): Result<BackupRecordEntity?>
    suspend fun deleteBackupRecord(record: BackupRecordEntity): Result<Unit>
    suspend fun updateBackupRecord(record: BackupRecordEntity): Result<Unit>
}
