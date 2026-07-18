package com.example.nestory.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.nestory.data.entity.BackupRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BackupRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackupRecord(record: BackupRecordEntity): Long

    @Query("SELECT * FROM backup_records ORDER BY created_at DESC")
    fun getAllBackupRecords(): Flow<List<BackupRecordEntity>>

    // --- NEW METHODS FOR REPOSITORY ---
    @Update
    suspend fun updateBackupRecord(record: BackupRecordEntity)

    @Delete
    suspend fun deleteBackupRecord(record: BackupRecordEntity)

    @Query("SELECT * FROM backup_records ORDER BY created_at DESC LIMIT 1")
    fun getLatestBackup(): Flow<BackupRecordEntity?>
}