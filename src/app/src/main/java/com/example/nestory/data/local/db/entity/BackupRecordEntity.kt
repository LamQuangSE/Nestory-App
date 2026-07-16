package com.example.nestory.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "backup_records")
data class BackupRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "file_name")
    val fileName: String,
    
    @ColumnInfo(name = "file_path")
    val filePath: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    
    @ColumnInfo(name = "backup_version")
    val backupVersion: String,
    
    @ColumnInfo(name = "app_version")
    val appVersion: String,
    
    val checksum: String,
    
    @ColumnInfo(name = "file_size")
    val fileSize: Long
)