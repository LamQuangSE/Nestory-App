package com.example.nestory.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "backup_records")
data class BackupRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "file_name")
    val fileName: String, // file_name text NN
    
    @ColumnInfo(name = "file_path")
    val filePath: String, // file_path text NN
    
    @ColumnInfo(name = "created_at")
    val createdAt: String, 
    
    @ColumnInfo(name = "backup_version")
    val backupVersion: String, // backup_version text NN
    
    @ColumnInfo(name = "app_version")
    val appVersion: String, // app_version text NN
    
    val checksum: String, // checksum text NN
    
    @ColumnInfo(name = "file_size")
    val fileSize: Long // file_size integer NN
)