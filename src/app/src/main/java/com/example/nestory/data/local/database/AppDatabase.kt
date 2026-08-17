package com.example.nestory.data.local.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
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
import com.example.nestory.data.local.dao.KitItemDao
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
version = DATABASE_VERSION,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun containerDao(): ContainerDao
    abstract fun documentDao(): DocumentDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun reminderDao(): ReminderDao
    abstract fun documentKitDao(): DocumentKitDao
    abstract fun kitItemDao(): KitItemDao
    abstract fun backupRecordDao(): BackupRecordDao
    
    // Đăng ký Dao mới
    abstract fun categoryDao(): CategoryDao 

    companion object {
        const val TAG = "Nestory-DB-Migration"
        private const val DATABASE_NAME = "nestory_database"

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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // document_kits gained optional category and note columns.
                db.execSQL("ALTER TABLE document_kits ADD COLUMN category TEXT")
                db.execSQL("ALTER TABLE document_kits ADD COLUMN note TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // kit_items gained optional display and metadata columns.
                db.execSQL("ALTER TABLE kit_items ADD COLUMN name TEXT")
                db.execSQL("ALTER TABLE kit_items ADD COLUMN description TEXT")
                db.execSQL("ALTER TABLE kit_items ADD COLUMN note TEXT")
                db.execSQL("ALTER TABLE kit_items ADD COLUMN required_documents INTEGER")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // document_kits gained the is_favorite flag.
                db.execSQL("ALTER TABLE document_kits ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Devices created by earlier v5 builds are missing columns that were
                // added to the schema without a version bump, leaving the stored
                // identity hash out of sync with the compiled schema. Reconcile
                // those tables non-destructively so the database can open again.
                db.execSQL("ALTER TABLE documents ADD COLUMN last_notified_status TEXT")
                db.execSQL("ALTER TABLE reminders ADD COLUMN reminder_date TEXT")
                db.execSQL("ALTER TABLE reminders ADD COLUMN reminder_time TEXT")

                // document_kits was written with category/note appended at the end and
                // a DEFAULT on is_favorite; rebuild it in the declared layout.
                db.execSQL(
                    """
                    CREATE TABLE document_kits_new (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `category` TEXT,
                        `description` TEXT,
                        `note` TEXT,
                        `target_completion_date` TEXT NOT NULL,
                        `is_favorite` INTEGER NOT NULL
                    )
                    """
                )
                db.execSQL(
                    """
                    INSERT INTO document_kits_new (id, name, category, description, note, target_completion_date, is_favorite)
                    SELECT id, name, category, description, note, target_completion_date, is_favorite
                    FROM document_kits
                    """
                )
                db.execSQL("DROP TABLE document_kits")
                db.execSQL("ALTER TABLE document_kits_new RENAME TO document_kits")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // documents gained OCR metadata columns to persist the fields the
                // user edits in the review screen (issue date, holder, number, raw text).
                db.execSQL("ALTER TABLE documents ADD COLUMN issue_date TEXT")
                db.execSQL("ALTER TABLE documents ADD COLUMN holder_name TEXT")
                db.execSQL("ALTER TABLE documents ADD COLUMN document_number TEXT")
                db.execSQL("ALTER TABLE documents ADD COLUMN ocr_text TEXT")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // documents gained a timestamp for "recently opened" tracking so the
                // Home screen can list the documents the user actually opened recently.
                db.execSQL("ALTER TABLE documents ADD COLUMN last_opened_at INTEGER")
            }
        }

        private val ALL_MIGRATIONS: List<Migration> = listOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8,
        )

        /**
         * Builds the Room database with every registered non-destructive migration applied
         * automatically, and logs the device/expected schema versions and migration outcome
         * under the [TAG] log tag.
         */
        fun databaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
            val appContext = context.applicationContext
            val expected = DATABASE_VERSION
            val deviceVersion = readDeviceDatabaseVersion(appContext)
            val path = migrationPath(deviceVersion)

            when {
                deviceVersion == 0 -> {
                    Log.i(TAG, "Fresh install: creating new database at schema v$expected.")
                }
                deviceVersion == expected -> {
                    Log.i(TAG, "Database version up to date. Device=$deviceVersion, Expected=$expected. No migration needed.")
                }
                deviceVersion < expected -> {
                    if (path.lastOrNull() == expected) {
                        Log.i(TAG, "Database version mismatch. Device=$deviceVersion, Expected=$expected. Migration path: ${path.joinToString(" -> ")}.")
                    } else {
                        Log.e(TAG, "Database version mismatch. Device=$deviceVersion, Expected=$expected. No complete migration path available. Attempted path: ${path.joinToString(" -> ")}.")
                    }
                }
                else -> {
                    Log.e(TAG, "Database version mismatch. Device=$deviceVersion, Expected=$expected. Device schema is newer than app; downgrade is not supported.")
                }
            }

            return Room.databaseBuilder(appContext, AppDatabase::class.java, DATABASE_NAME)
                .addMigrations(*ALL_MIGRATIONS.map { it.withMigrationLogging() }.toTypedArray())
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        Log.i(TAG, "Fresh database created at schema v${db.version}.")
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        if (deviceVersion > 0 && deviceVersion < expected && db.version == expected) {
                            Log.i(TAG, "Migration SUCCESS: $deviceVersion -> $expected.")
                        }
                    }
                })
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = databaseBuilder(context).build()
                INSTANCE = instance
                instance
            }
        }

        private fun Migration.withMigrationLogging(): Migration = object : Migration(startVersion, endVersion) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val step = "$startVersion -> $endVersion"
                Log.i(TAG, "Migration step started: $step")
                try {
                    this@withMigrationLogging.migrate(db)
                    Log.i(TAG, "Migration step SUCCESS: $step")
                } catch (t: Throwable) {
                    Log.e(TAG, "Migration FAILED: $step. Reason: ${t.message}", t)
                    throw t
                }
            }
        }

        /**
         * Resolves the concrete migration steps available between the device version
         * and the current schema (e.g. device=2 -> [2, 3, 4, 5]).
         */
        private fun migrationPath(fromVersion: Int): List<Int> {
            val path = mutableListOf(fromVersion)
            var current = fromVersion
            while (current < DATABASE_VERSION) {
                val next = ALL_MIGRATIONS.firstOrNull { it.startVersion == current } ?: break
                path.add(next.endVersion)
                current = next.endVersion
            }
            return path
        }

        /**
         * Reads the raw schema version (PRAGMA user_version) currently stored on the device
         * before Room opens the database, so mismatches can be diagnosed without touching
         * existing data. Returns 0 when no database exists yet (fresh install).
         */
        private fun readDeviceDatabaseVersion(context: Context): Int {
            return try {
                val databaseFile = context.getDatabasePath(DATABASE_NAME)
                if (!databaseFile.exists()) return 0
                SQLiteDatabase.openDatabase(databaseFile.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                    db.rawQuery("PRAGMA user_version", null).use { cursor ->
                        if (cursor.moveToFirst()) cursor.getInt(0) else 0
                    }
                }
            } catch (t: Throwable) {
                Log.w(TAG, "Could not read device database version. Assuming fresh install.", t)
                0
            }
        }
    }
}