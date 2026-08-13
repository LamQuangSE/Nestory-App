package com.example.nestory.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.nestory.data.local.dao.AttachmentDao
import com.example.nestory.data.local.dao.BackupRecordDao
import com.example.nestory.data.local.dao.ContainerDao
import com.example.nestory.data.local.database.converter.Converters
import com.example.nestory.data.local.dao.DocumentDao
import com.example.nestory.data.local.dao.DocumentKitDao
import com.example.nestory.data.local.dao.ReminderDao
import com.example.nestory.data.local.dao.CategoryDao 
import com.example.nestory.data.local.entity.AttachmentEntity
import com.example.nestory.data.local.entity.BackupRecordEntity
import com.example.nestory.data.local.entity.ContainerEntity
import com.example.nestory.data.local.entity.DocumentEntity
import com.example.nestory.data.local.entity.DocumentKitEntity
import com.example.nestory.data.local.entity.KitItemEntity
import com.example.nestory.data.local.entity.ReminderEntity
import com.example.nestory.data.local.entity.CategoryEntity

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
    version = 2,
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

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // categories was added in a commit that kept version = 1, so older
                // v1 databases have no categories table. Create it if missing.
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `categories` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `colorValue` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """
                )
                // Remove non-unique parent_id index; unique name index added below.
                db.execSQL("DROP INDEX IF EXISTS index_containers_parent_id")
                // Resolve any pre-existing duplicate names by appending the row id,
                // keeping the oldest row unchanged.
                db.execSQL(
                    """
                    UPDATE containers
                    SET name = name || ' (' || id || ')'
                    WHERE id NOT IN (
                        SELECT MIN(id) FROM containers GROUP BY name
                    )
                    """
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_containers_name ON containers(name)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_containers_parent_id ON containers(parent_id)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nestory_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}