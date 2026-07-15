package com.example.nestory.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nestory.data.local.db.dao.BackupRecordDao
import com.example.nestory.data.local.db.dao.DocumentKitDao
import com.example.nestory.data.local.db.entity.BackupRecordEntity
import com.example.nestory.data.local.db.entity.DocumentKitEntity
import com.example.nestory.data.local.db.entity.KitItemEntity

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