package com.example.nestory.data.local.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun migrate7To8_preservesAllRows() {
        val dbName = "migration-test-7to8"

        helper.createDatabase(dbName, 7).use { db ->
            db.execSQL(
                """
                INSERT INTO containers (id, name, parent_id) VALUES (1, 'Main', NULL)
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO documents (
                    id, title, category_id, expiration_date, notes, issue_date, holder_name,
                    document_number, ocr_text, is_favorite, container_id, last_notified_status
                ) VALUES (1, 'ID Card', 'cat-1', '2030-01-01', 'keep me', '2020-01-01', 'Holder A',
                    'ABC123', 'raw ocr', 1, 1, 'notified')
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO attachments (id, file_uri, document_id, display_order)
                VALUES (1, 'content://x/1', 1, 0)
                """.trimIndent()
            )
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
            db.execSQL(
                """
                INSERT INTO categories (id, name, colorValue) VALUES ('cat-1', 'Giấy tờ tùy thân', 0)
                """.trimIndent()
            )
        }

        val migrated: SupportSQLiteDatabase =
            helper.runMigrationsAndValidate(dbName, 8, true, AppDatabase.MIGRATION_7_8)

        migrated.use { db ->
            assertRowCount(db, "containers", 1)
            assertRowCount(db, "documents", 1)
            assertRowCount(db, "attachments", 1)
            assertRowCount(db, "document_kits", 1)
            assertRowCount(db, "kit_items", 1)
            assertRowCount(db, "reminders", 1)
            assertRowCount(db, "backup_records", 1)
            assertRowCount(db, "categories", 1)

            db.query("SELECT title, notes FROM documents WHERE id = 1").use { cursor ->
                assert(cursor.moveToFirst())
                assert(cursor.getString(0) == "ID Card")
                assert(cursor.getString(1) == "keep me")
            }
        }
    }

    private fun assertRowCount(db: SupportSQLiteDatabase, table: String, expected: Int) {
        db.query("SELECT COUNT(*) FROM $table").use { cursor ->
            assert(cursor.moveToFirst())
            assert(cursor.getInt(0) == expected) { "$table row count != $expected" }
        }
    }
}