package com.example.nestory.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nestory.data.dao.AttachmentDao
import com.example.nestory.data.dao.ContainerDao
import com.example.nestory.data.dao.DocumentDao
import com.example.nestory.data.dao.ReminderDao
import com.example.nestory.data.entity.AttachmentEntity
import com.example.nestory.data.entity.ContainerEntity
import com.example.nestory.data.entity.DocumentEntity
import com.example.nestory.data.entity.ReminderEntity

@Database(
    entities = [
        ContainerEntity::class,
        DocumentEntity::class,
        AttachmentEntity::class,
        ReminderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun containerDao(): ContainerDao

    abstract fun documentDao(): DocumentDao

    abstract fun attachmentDao(): AttachmentDao

    abstract fun reminderDao(): ReminderDao
}