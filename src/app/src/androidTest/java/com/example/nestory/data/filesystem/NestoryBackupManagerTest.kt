package com.example.nestory.data.filesystem

import android.content.Context
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nestory.data.local.database.AppDatabase
import java.io.File
import java.io.RandomAccessFile
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NestoryBackupManagerTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var backupManager: NestoryBackupManager
    private lateinit var testDir: File

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        resetDatabase()
        database = AppDatabase.getDatabase(context)
        backupManager = NestoryBackupManager(context)
        testDir = File(context.cacheDir, "backup-manager-test").apply {
            deleteRecursively()
            mkdirs()
        }
    }

    @After
    fun tearDown() {
        database.close()
        resetDatabase()
        testDir.deleteRecursively()
    }

    @Test
    fun exportThenImport_restoresDatabaseRowsAndAttachmentsWithNewPaths() = runBlocking {
        seedFullVault()
        val oldAttachmentPath = database.attachmentDao().getAllAttachments().single().fileUri
        val backupFile = exportToFile(password = VALID_PASSWORD)

        replaceCurrentVaultWithDifferentData()

        val result = backupManager.importBackup(
            password = VALID_PASSWORD,
            sourceUri = Uri.fromFile(backupFile),
            onProgress = {},
        )

        val documents = database.documentDao().getAllDocuments()
        val attachments = database.attachmentDao().getAllAttachments()
        val kits = database.documentKitDao().getAllKitsWithItems()
        val reminders = database.reminderDao().getAll()
        val backupRecords = database.backupRecordDao().getAllBackupRecords()

        assertTrue(result.totalItems > 0)
        assertEquals(listOf("Passport"), documents.map { it.title })
        assertEquals("cat-id", documents.single().categoryId)
        assertEquals("2030-12-31", documents.single().expirationDate)
        assertEquals(1, attachments.size)
        assertNotEquals(oldAttachmentPath, attachments.single().fileUri)
        assertTrue(File(attachments.single().fileUri).exists())
        assertEquals("Family Kit", kits.single().kit.name)
        assertEquals("ready", kits.single().items.single().status)
        assertEquals(1L, kits.single().items.single().linkedDocumentId)
        assertEquals(2, reminders.size)
        assertEquals(1, backupRecords.size)
        assertEquals("1", backupRecords.single().backupVersion)
    }

    @Test
    fun importWithWrongPassword_doesNotReplaceCurrentVault() = runBlocking {
        seedFullVault()
        val backupFile = exportToFile(password = VALID_PASSWORD)
        replaceCurrentVaultWithDifferentData()

        val failure = runCatching {
            backupManager.importBackup(
                password = "WrongPassword123!",
                sourceUri = Uri.fromFile(backupFile),
                onProgress = {},
            )
        }.exceptionOrNull()

        assertNotNull(failure)
        assertEquals(listOf("Existing Phone Data"), database.documentDao().getAllDocuments().map { it.title })
        assertTrue(database.attachmentDao().getAllAttachments().single().fileUri.contains("existing"))
    }

    @Test
    fun importTamperedBackup_failsAndDoesNotReplaceCurrentVault() = runBlocking {
        seedFullVault()
        val backupFile = exportToFile(password = VALID_PASSWORD)
        tamperLastByte(backupFile)
        replaceCurrentVaultWithDifferentData()

        val failure = runCatching {
            backupManager.importBackup(
                password = VALID_PASSWORD,
                sourceUri = Uri.fromFile(backupFile),
                onProgress = {},
            )
        }.exceptionOrNull()

        assertNotNull(failure)
        assertEquals(listOf("Existing Phone Data"), database.documentDao().getAllDocuments().map { it.title })
    }

    @Test
    fun exportedFileDoesNotExposePlaintextData() = runBlocking {
        seedFullVault()
        val backupFile = exportToFile(password = VALID_PASSWORD)

        val backupBytesAsText = backupFile.readBytes().toString(Charsets.ISO_8859_1)

        assertFalse(backupBytesAsText.contains("Passport"))
        assertFalse(backupBytesAsText.contains("P123456"))
        assertFalse(backupBytesAsText.contains("Family Kit"))
        assertFalse(backupBytesAsText.contains("scanned attachment content"))
    }

    @Test
    fun exportSameDataTwiceWithSamePassword_producesDifferentFiles() = runBlocking {
        seedFullVault()

        val first = exportToFile(password = VALID_PASSWORD, name = "first.nestory")
        val second = exportToFile(password = VALID_PASSWORD, name = "second.nestory")

        assertNotEquals(first.readBytes().toList(), second.readBytes().toList())
    }

    @Test
    fun estimateExport_returnsRealEncryptionAndNonZeroSize() = runBlocking {
        seedFullVault()

        val estimate = backupManager.estimateExport()

        assertEquals("AES-256-GCM", estimate.encryptionLabel)
        assertTrue(estimate.includedLabel.endsWith("mục"))
        assertTrue(estimate.estimatedSize > 0L)
    }

    private suspend fun exportToFile(
        password: String,
        name: String = "backup.nestory",
    ): File {
        val backupFile = File(testDir, name)
        val result = backupManager.exportBackup(
            password = password,
            targetUri = Uri.fromFile(backupFile),
            fileName = name,
            onProgress = {},
        )
        assertTrue(backupFile.exists())
        assertTrue(result.fileSize > 0)
        assertTrue(result.checksum.isNotBlank())
        return backupFile
    }

    private suspend fun seedFullVault() {
        val attachmentFile = File(context.filesDir, "attachments/source-passport.pdf").apply {
            parentFile?.mkdirs()
            writeText("scanned attachment content")
        }
        val sqlite = database.openHelper.writableDatabase
        sqlite.execSQL("DELETE FROM reminders")
        sqlite.execSQL("DELETE FROM kit_items")
        sqlite.execSQL("DELETE FROM attachments")
        sqlite.execSQL("DELETE FROM document_kits")
        sqlite.execSQL("DELETE FROM documents")
        sqlite.execSQL("DELETE FROM containers")
        sqlite.execSQL("DELETE FROM categories")
        sqlite.execSQL("DELETE FROM backup_records")
        sqlite.execSQL("INSERT INTO categories (id, name, colorValue) VALUES ('cat-id', 'Identity', 123)")
        sqlite.execSQL("INSERT INTO containers (id, name, parent_id) VALUES (1, 'Main', NULL)")
        sqlite.execSQL(
            """
            INSERT INTO documents (
                id, title, category_id, expiration_date, notes, issue_date, holder_name,
                document_number, ocr_text, is_favorite, container_id, last_notified_status,
                last_opened_at
            ) VALUES (
                1, 'Passport', 'cat-id', '2030-12-31', 'Important note', '2020-01-01',
                'Holder A', 'P123456', 'raw ocr passport', 1, 1, 'valid', 123456
            )
            """.trimIndent()
        )
        sqlite.execSQL(
            """
            INSERT INTO attachments (id, file_uri, document_id, display_order)
            VALUES (1, '${attachmentFile.absolutePath}', 1, 0)
            """.trimIndent()
        )
        sqlite.execSQL(
            """
            INSERT INTO document_kits (
                id, name, category, description, note, target_completion_date, is_favorite
            ) VALUES (1, 'Family Kit', 'Personal', 'desc', 'kit note', '2027-01-01', 1)
            """.trimIndent()
        )
        sqlite.execSQL(
            """
            INSERT INTO kit_items (
                id, status, document_kit_id, linked_document_id, name, description, note,
                required_documents
            ) VALUES (1, 'ready', 1, 1, 'Passport item', 'desc', 'item note', 1)
            """.trimIndent()
        )
        sqlite.execSQL(
            """
            INSERT INTO reminders (
                id, document_id, document_kit_id, is_enabled, reminder_date, reminder_time
            ) VALUES (1, 1, NULL, 1, '31/12/2099', '08:00')
            """.trimIndent()
        )
        sqlite.execSQL(
            """
            INSERT INTO reminders (
                id, document_id, document_kit_id, is_enabled, reminder_date, reminder_time
            ) VALUES (2, NULL, 1, 1, '31/12/2099', '09:00')
            """.trimIndent()
        )
        sqlite.execSQL(
            """
            INSERT INTO backup_records (
                id, file_name, file_path, created_at, backup_version, app_version, checksum,
                file_size
            ) VALUES (1, 'old-history.nestory', '/old', '2026-01-01', '1', '1.0', 'abc', 10)
            """.trimIndent()
        )
    }

    private fun replaceCurrentVaultWithDifferentData() {
        val existingFile = File(context.filesDir, "attachments/existing.pdf").apply {
            parentFile?.mkdirs()
            writeText("existing phone attachment")
        }
        val sqlite = database.openHelper.writableDatabase
        sqlite.execSQL("DELETE FROM reminders")
        sqlite.execSQL("DELETE FROM kit_items")
        sqlite.execSQL("DELETE FROM attachments")
        sqlite.execSQL("DELETE FROM document_kits")
        sqlite.execSQL("DELETE FROM documents")
        sqlite.execSQL("DELETE FROM containers")
        sqlite.execSQL("DELETE FROM categories")
        sqlite.execSQL("DELETE FROM backup_records")
        sqlite.execSQL("INSERT INTO categories (id, name, colorValue) VALUES ('existing-cat', 'Existing', 456)")
        sqlite.execSQL("INSERT INTO containers (id, name, parent_id) VALUES (10, 'Existing Main', NULL)")
        sqlite.execSQL(
            """
            INSERT INTO documents (
                id, title, category_id, expiration_date, notes, issue_date, holder_name,
                document_number, ocr_text, is_favorite, container_id, last_notified_status,
                last_opened_at
            ) VALUES (
                10, 'Existing Phone Data', 'existing-cat', NULL, NULL, NULL, NULL, NULL,
                NULL, 0, 10, NULL, NULL
            )
            """.trimIndent()
        )
        sqlite.execSQL(
            """
            INSERT INTO attachments (id, file_uri, document_id, display_order)
            VALUES (10, '${existingFile.absolutePath}', 10, 0)
            """.trimIndent()
        )
    }

    private fun tamperLastByte(file: File) {
        RandomAccessFile(file, "rw").use { raf ->
            raf.seek(file.length() - 1)
            val original = raf.read()
            raf.seek(file.length() - 1)
            raf.write(original xor 0x01)
        }
    }

    private fun resetDatabase() {
        runCatching {
            val field = AppDatabase::class.java.getDeclaredField("INSTANCE")
            field.isAccessible = true
            (field.get(null) as? AppDatabase)?.close()
            field.set(null, null)
        }
        context.deleteDatabase("nestory_database")
        File(context.filesDir, "attachments").deleteRecursively()
    }

    companion object {
        private const val VALID_PASSWORD = "StrongPassword123!"
    }
}
