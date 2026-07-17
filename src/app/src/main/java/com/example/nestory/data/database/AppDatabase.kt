package com.example.nestory.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nestory.data.dao.AttachmentDao
import com.example.nestory.data.dao.BackupRecordDao
import com.example.nestory.data.dao.ContainerDao
import com.example.nestory.data.dao.DocumentDao
import com.example.nestory.data.dao.DocumentKitDao
import com.example.nestory.data.dao.ReminderDao
import com.example.nestory.data.entity.AttachmentEntity
import com.example.nestory.data.entity.BackupRecordEntity
import com.example.nestory.data.entity.ContainerEntity
import com.example.nestory.data.entity.DocumentEntity
import com.example.nestory.data.entity.DocumentKitEntity
import com.example.nestory.data.entity.KitItemEntity
import com.example.nestory.data.entity.ReminderEntity

@Database(
    entities = [
        // SCRUM-92
        ContainerEntity::class,
        DocumentEntity::class,
        AttachmentEntity::class,
        ReminderEntity::class,

        // SCRUM-93
        DocumentKitEntity::class,
        KitItemEntity::class,
        BackupRecordEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    // SCRUM-92
    abstract fun containerDao(): ContainerDao

    abstract fun documentDao(): DocumentDao

    abstract fun attachmentDao(): AttachmentDao

    abstract fun reminderDao(): ReminderDao

    // SCRUM-93
    abstract fun documentKitDao(): DocumentKitDao

    abstract fun backupRecordDao(): BackupRecordDao
}
