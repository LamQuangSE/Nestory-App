package com.example.nestory.data.repository

import com.example.nestory.data.entity.BackupRecordEntity
import kotlinx.coroutines.flow.Flow

interface BackupRecordRepository {
    suspend fun createBackupRecord(record: BackupRecordEntity): Result<Long>
    fun getBackupHistory(): Flow<List<BackupRecordEntity>>
    fun getLatestBackup(): Flow<BackupRecordEntity?>
    suspend fun deleteBackupRecord(record: BackupRecordEntity): Result<Unit>
    suspend fun updateBackupRecord(record: BackupRecordEntity): Result<Unit>
}