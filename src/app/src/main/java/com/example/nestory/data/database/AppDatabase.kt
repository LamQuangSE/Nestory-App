package com.example.nestory.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nestory.data.dao.BackupRecordDao
import com.example.nestory.data.dao.DocumentKitDao
import com.example.nestory.data.entity.BackupRecordEntity
import com.example.nestory.data.entity.DocumentKitEntity
import com.example.nestory.data.entity.KitItemEntity

@Database(
    entities = [
        DocumentKitEntity::class,
        KitItemEntity::class,
        BackupRecordEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun documentKitDao(): DocumentKitDao
    abstract fun backupRecordDao(): BackupRecordDao
}