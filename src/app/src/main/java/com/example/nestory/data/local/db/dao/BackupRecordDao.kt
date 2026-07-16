package com.example.nestory.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nestory.data.local.db.entity.BackupRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BackupRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackupRecord(record: BackupRecordEntity): Long

    @Query("SELECT * FROM backup_records ORDER BY id DESC")
    fun getAllBackupRecords(): Flow<List<BackupRecordEntity>>
}