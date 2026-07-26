package com.example.nestory.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.nestory.data.dao.AttachmentDao
import com.example.nestory.data.dao.BackupRecordDao
import com.example.nestory.data.dao.ContainerDao
import com.example.nestory.data.database.converter.Converters
import com.example.nestory.data.dao.DocumentDao
import com.example.nestory.data.dao.DocumentKitDao
import com.example.nestory.data.dao.ReminderDao
import com.example.nestory.data.dao.CategoryDao 
import com.example.nestory.data.entity.AttachmentEntity
import com.example.nestory.data.entity.BackupRecordEntity
import com.example.nestory.data.entity.ContainerEntity
import com.example.nestory.data.entity.DocumentEntity
import com.example.nestory.data.entity.DocumentKitEntity
import com.example.nestory.data.entity.KitItemEntity
import com.example.nestory.data.entity.ReminderEntity
import com.example.nestory.data.entity.CategoryEntity 

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
        BackupRecordEntity::class,
        
        // SCRUM-98
        CategoryEntity::class // Đăng ký bảng mới
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun containerDao(): ContainerDao
    abstract fun documentDao(): DocumentDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun reminderDao(): ReminderDao
    abstract fun documentKitDao(): DocumentKitDao
    abstract fun backupRecordDao(): BackupRecordDao
    
    // Đăng ký Dao mới
    abstract fun categoryDao(): CategoryDao 

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nestory_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}