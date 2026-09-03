package com.example.nestory.data.local.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val context: Context =
        InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

    @After
    fun tearDown() {
        context.deleteDatabase(DATABASE_NAME)
    }

    @Test
    fun migrate7ToLatest_preservesAllRows() {
        createVersion7Database()

        val migrated = AppDatabase.databaseBuilder(context).build()
        try {
            val db = migrated.openHelper.writableDatabase
            assertRowCount(db, "containers", 1)
            assertRowCount(db, "documents", 1)
            assertRowCount(db, "attachments", 1)
            assertRowCount(db, "document_kits", 1)
            assertRowCount(db, "kit_items", 1)
            assertRowCount(db, "reminders", 1)
            assertRowCount(db, "backup_records", 1)
            assertRowCount(db, "categories", 1)

            db.query("SELECT title, notes, last_opened_at FROM documents WHERE id = 1").use { cursor ->
                assert(cursor.moveToFirst())
                assert(cursor.getString(0) == "ID Card")
                assert(cursor.getString(1) == "keep me")
                assert(cursor.isNull(2))
            }

            db.query("SELECT lead_time_days, custom_lead_time_mode, repeat_daily, in_app_enabled, push_enabled FROM reminders WHERE id = 1").use { cursor ->
                assert(cursor.moveToFirst())
                assert(cursor.getInt(0) == 7)
                assert(cursor.getInt(1) == 0)
                assert(cursor.getInt(2) == 1)
                assert(cursor.getInt(3) == 1)
                assert(cursor.getInt(4) == 1)
            }
        } finally {
            migrated.close()
        }
    }

    @Test
    fun migrate8To9_replacesGlobalContainerNameIndex() {
        createVersion8Database()

        val migrated = AppDatabase.databaseBuilder(context).build()
        try {
            val db = migrated.openHelper.writableDatabase

            assertIndexMissing(db, "containers", "index_containers_name")
            assertIndexPresent(db, "containers", "index_containers_name_parent_id", unique = false)
            assertIndexPresent(db, "containers", "index_containers_parent_id", unique = false)
        } finally {
            migrated.close()
        }
    }

    private fun createVersion7Database() {
        context.deleteDatabase(DATABASE_NAME)
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        dbFile.parentFile?.mkdirs()

        SQLiteDatabase.openOrCreateDatabase(dbFile, null).use { db ->
            db.execSQL("PRAGMA foreign_keys=OFF")
            createVersion7Schema(db)
            insertVersion7Rows(db)
            db.version = 7
        }
    }

    private fun createVersion8Database() {
        context.deleteDatabase(DATABASE_NAME)
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        dbFile.parentFile?.mkdirs()

        SQLiteDatabase.openOrCreateDatabase(dbFile, null).use { db ->
            db.execSQL("PRAGMA foreign_keys=OFF")
            createVersion7Schema(db)
            db.execSQL("ALTER TABLE documents ADD COLUMN last_opened_at INTEGER")
            insertVersion7Rows(db)
            db.version = 8
        }
    }

    private fun createVersion7Schema(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `containers` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `parent_id` INTEGER,
                FOREIGN KEY(`parent_id`) REFERENCES `containers`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_containers_name` ON `containers` (`name`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_containers_parent_id` ON `containers` (`parent_id`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `documents` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title` TEXT NOT NULL,
                `category_id` TEXT NOT NULL,
                `expiration_date` TEXT,
                `notes` TEXT,
                `issue_date` TEXT,
                `holder_name` TEXT,
                `document_number` TEXT,
                `ocr_text` TEXT,
                `is_favorite` INTEGER NOT NULL,
                `container_id` INTEGER NOT NULL,
                `last_notified_status` TEXT,
                FOREIGN KEY(`container_id`) REFERENCES `containers`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_documents_container_id` ON `documents` (`container_id`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `attachments` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `file_uri` TEXT NOT NULL,
                `document_id` INTEGER NOT NULL,
                `display_order` INTEGER NOT NULL,
                FOREIGN KEY(`document_id`) REFERENCES `documents`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_attachments_document_id` ON `attachments` (`document_id`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_attachments_document_id_display_order` ON `attachments` (`document_id`, `display_order`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `document_kits` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `category` TEXT,
                `description` TEXT,
                `note` TEXT,
                `target_completion_date` TEXT NOT NULL,
                `is_favorite` INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `kit_items` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `status` TEXT NOT NULL,
                `document_kit_id` INTEGER NOT NULL,
                `linked_document_id` INTEGER,
                `name` TEXT,
                `description` TEXT,
                `note` TEXT,
                `required_documents` INTEGER,
                FOREIGN KEY(`document_kit_id`) REFERENCES `document_kits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`linked_document_id`) REFERENCES `documents`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_kit_items_document_kit_id` ON `kit_items` (`document_kit_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_kit_items_linked_document_id` ON `kit_items` (`linked_document_id`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `reminders` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `document_id` INTEGER,
                `document_kit_id` INTEGER,
                `is_enabled` INTEGER NOT NULL,
                `reminder_date` TEXT,
                `reminder_time` TEXT,
                FOREIGN KEY(`document_id`) REFERENCES `documents`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`document_kit_id`) REFERENCES `document_kits`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_document_id` ON `reminders` (`document_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_document_kit_id` ON `reminders` (`document_kit_id`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `backup_records` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `file_name` TEXT NOT NULL,
                `file_path` TEXT NOT NULL,
                `created_at` TEXT NOT NULL,
                `backup_version` TEXT NOT NULL,
                `app_version` TEXT NOT NULL,
                `checksum` TEXT NOT NULL,
                `file_size` INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `categories` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `colorValue` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }

    private fun insertVersion7Rows(db: SQLiteDatabase) {
        db.execSQL("INSERT INTO containers (id, name, parent_id) VALUES (1, 'Main', NULL)")
        db.execSQL(
            """
            INSERT INTO documents (
                id, title, category_id, expiration_date, notes, issue_date, holder_name,
                document_number, ocr_text, is_favorite, container_id, last_notified_status
            ) VALUES (1, 'ID Card', 'cat-1', '2030-01-01', 'keep me', '2020-01-01', 'Holder A',
                'ABC123', 'raw ocr', 1, 1, 'notified')
            """.trimIndent()
        )
        db.execSQL("INSERT INTO attachments (id, file_uri, document_id, display_order) VALUES (1, 'content://x/1', 1, 0)")
        db.execSQL(
            """
            INSERT INTO document_kits (
                id, name, category, description, note, target_completion_date, is_favorite
            ) VALUES (1, 'Wedding', 'Personal', 'desc', 'note', '2026-12-31', 1)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO kit_items (id, status, document_kit_id, linked_document_id, name,
                description, note, required_documents)
            VALUES (1, 'pending', 1, 1, 'Passport', 'desc', 'note', 2)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO reminders (id, document_id, document_kit_id, is_enabled,
                reminder_date, reminder_time)
            VALUES (1, 1, NULL, 1, '2026-09-01', '08:00')
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO backup_records (id, file_name, file_path, created_at,
                backup_version, app_version, checksum, file_size)
            VALUES (1, 'backup.bak', '/path', '2026-01-01', '1', '1.0', 'abc', 100)
            """.trimIndent()
        )
        db.execSQL("INSERT INTO categories (id, name, colorValue) VALUES ('cat-1', 'Giấy tờ tùy thân', 0)")
    }

    private fun assertRowCount(db: SupportSQLiteDatabase, table: String, expected: Int) {
        db.query("SELECT COUNT(*) FROM $table").use { cursor ->
            assert(cursor.moveToFirst())
            assert(cursor.getInt(0) == expected) { "$table row count != $expected" }
        }
    }

    private fun assertIndexPresent(
        db: SupportSQLiteDatabase,
        table: String,
        indexName: String,
        unique: Boolean,
    ) {
        db.query("PRAGMA index_list(`$table`)").use { cursor ->
            val nameColumn = cursor.getColumnIndexOrThrow("name")
            val uniqueColumn = cursor.getColumnIndexOrThrow("unique")
            while (cursor.moveToNext()) {
                if (cursor.getString(nameColumn) == indexName) {
                    assert(cursor.getInt(uniqueColumn) == if (unique) 1 else 0) {
                        "$indexName unique flag != $unique"
                    }
                    return
                }
            }
        }
        error("Missing index $indexName on $table")
    }

    private fun assertIndexMissing(
        db: SupportSQLiteDatabase,
        table: String,
        indexName: String,
    ) {
        db.query("PRAGMA index_list(`$table`)").use { cursor ->
            val nameColumn = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) {
                assert(cursor.getString(nameColumn) != indexName) {
                    "Unexpected index $indexName on $table"
                }
            }
        }
    }

    companion object {
        private const val DATABASE_NAME = "nestory_database"
    }
}
