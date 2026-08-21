package com.example.nestory.data.filesystem

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.provider.OpenableColumns
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.local.database.DATABASE_VERSION
import com.example.nestory.data.local.entity.BackupRecordEntity
import com.example.nestory.utils.notification.ReminderScheduler
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FilterOutputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

enum class BackupOperationStep {
    PREPARE_DATA,
    COMPRESS_DATA,
    ENCRYPT_DATA,
    SAVE_FILE,
    VERIFY_BACKUP,
    DECRYPT_DATA,
    RESTORE_DATA,
    COMPLETE,
}

enum class BackupOperationStepStatus {
    PENDING,
    SUCCESS,
    ERROR,
}

data class BackupProgressStep(
    val step: BackupOperationStep,
    val status: BackupOperationStepStatus,
)

data class BackupProgressSnapshot(
    val progress: Float,
    val steps: List<BackupProgressStep>,
)

data class BackupExportResult(
    val fileName: String,
    val locationLabel: String,
    val createdAt: String,
    val checksum: String,
    val fileSize: Long,
)

data class BackupImportResult(
    val totalItems: Int,
    val fileSize: Long,
    val completedAt: String,
)

data class BackupExportEstimate(
    val includedLabel: String,
    val encryptionLabel: String,
    val estimatedSize: Long,
)

class NestoryBackupManager(private val context: Context) {

    private val appContext = context.applicationContext
    private val contentResolver = appContext.contentResolver
    private val database = AppDatabase.getDatabase(appContext)
    private val secureRandom = SecureRandom()

    suspend fun exportBackup(
        password: String,
        targetUri: Uri,
        fileName: String,
        onProgress: (BackupProgressSnapshot) -> Unit,
    ): BackupExportResult = withContext(Dispatchers.IO) {
        require(password.isNotBlank()) { "Mật khẩu là bắt buộc." }

        val steps = exportSteps().toMutableList()
        fun update(step: BackupOperationStep, status: BackupOperationStepStatus, progress: Float) {
            val index = steps.indexOfFirst { it.step == step }
            if (index >= 0) steps[index] = steps[index].copy(status = status)
            onProgress(BackupProgressSnapshot(progress, steps.toList()))
        }

        val tempDir = File(appContext.cacheDir, "backup_export_${UUID.randomUUID()}").apply { mkdirs() }
        val zipFile = File(tempDir, "payload.zip")
        val createdAt = displayDateTime()

        try {
            update(BackupOperationStep.PREPARE_DATA, BackupOperationStepStatus.SUCCESS, 0.18f)
            val exportData = buildExportData()

            update(BackupOperationStep.COMPRESS_DATA, BackupOperationStepStatus.PENDING, 0.32f)
            writePayloadZip(zipFile, exportData)
            update(BackupOperationStep.COMPRESS_DATA, BackupOperationStepStatus.SUCCESS, 0.48f)

            update(BackupOperationStep.ENCRYPT_DATA, BackupOperationStepStatus.PENDING, 0.58f)
            val writeResult = encryptPayloadToUri(
                password = password,
                plaintextZip = zipFile,
                targetUri = targetUri,
            )
            update(BackupOperationStep.ENCRYPT_DATA, BackupOperationStepStatus.SUCCESS, 0.76f)

            update(BackupOperationStep.SAVE_FILE, BackupOperationStepStatus.SUCCESS, 0.9f)

            database.backupRecordDao().insertBackupRecord(
                BackupRecordEntity(
                    fileName = fileName,
                    filePath = targetUri.toString(),
                    createdAt = createdAt,
                    backupVersion = BACKUP_FORMAT_VERSION.toString(),
                    appVersion = appVersionName(),
                    checksum = writeResult.checksum,
                    fileSize = writeResult.bytesWritten,
                )
            )
            update(BackupOperationStep.COMPLETE, BackupOperationStepStatus.SUCCESS, 1f)

            BackupExportResult(
                fileName = fileName,
                locationLabel = locationLabel(targetUri),
                createdAt = createdAt,
                checksum = writeResult.checksum,
                fileSize = writeResult.bytesWritten,
            )
        } catch (t: Throwable) {
            markCurrentError(steps, onProgress)
            throw t
        } finally {
            tempDir.deleteRecursively()
        }
    }

    suspend fun importBackup(
        password: String,
        sourceUri: Uri,
        onProgress: (BackupProgressSnapshot) -> Unit,
    ): BackupImportResult = withContext(Dispatchers.IO) {
        require(password.isNotBlank()) { "Mật khẩu là bắt buộc." }

        val steps = importSteps().toMutableList()
        fun update(step: BackupOperationStep, status: BackupOperationStepStatus, progress: Float) {
            val index = steps.indexOfFirst { it.step == step }
            if (index >= 0) steps[index] = steps[index].copy(status = status)
            onProgress(BackupProgressSnapshot(progress, steps.toList()))
        }

        val tempDir = File(appContext.cacheDir, "backup_import_${UUID.randomUUID()}").apply { mkdirs() }
        val payloadZip = File(tempDir, "payload.zip")
        val extractDir = File(tempDir, "extracted").apply { mkdirs() }

        try {
            update(BackupOperationStep.VERIFY_BACKUP, BackupOperationStepStatus.PENDING, 0.12f)
            update(BackupOperationStep.VERIFY_BACKUP, BackupOperationStepStatus.SUCCESS, 0.22f)
            update(BackupOperationStep.DECRYPT_DATA, BackupOperationStepStatus.PENDING, 0.3f)
            decryptUriToPayloadZip(password, sourceUri, payloadZip)
            update(BackupOperationStep.DECRYPT_DATA, BackupOperationStepStatus.SUCCESS, 0.44f)

            val dataJson = extractPayloadZip(payloadZip, extractDir)
            update(BackupOperationStep.RESTORE_DATA, BackupOperationStepStatus.PENDING, 0.58f)
            val totalItems = restoreData(dataJson, extractDir)
            update(BackupOperationStep.RESTORE_DATA, BackupOperationStepStatus.SUCCESS, 0.88f)

            val completedAt = displayDateTime()
            database.backupRecordDao().insertBackupRecord(
                BackupRecordEntity(
                    fileName = displayName(sourceUri) ?: "Nestory backup",
                    filePath = sourceUri.toString(),
                    createdAt = completedAt,
                    backupVersion = BACKUP_FORMAT_VERSION.toString(),
                    appVersion = appVersionName(),
                    checksum = "",
                    fileSize = sourceSize(sourceUri),
                )
            )
            update(BackupOperationStep.COMPLETE, BackupOperationStepStatus.SUCCESS, 1f)

            BackupImportResult(
                totalItems = totalItems,
                fileSize = sourceSize(sourceUri),
                completedAt = completedAt,
            )
        } catch (t: Throwable) {
            markCurrentError(steps, onProgress)
            throw t
        } finally {
            tempDir.deleteRecursively()
        }
    }

    fun defaultBackupFileName(): String =
        "Nestory_Backup_${SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(Date())}.nestory"

    suspend fun estimateExport(): BackupExportEstimate = withContext(Dispatchers.IO) {
        val sqlite = database.openHelper.writableDatabase
        val rowCount = BACKUP_TABLES.sumOf { tableName ->
            sqlite.query("SELECT COUNT(*) FROM $tableName").use { cursor ->
                if (cursor.moveToFirst()) cursor.getLong(0) else 0L
            }
        }
        val attachmentsSize = sqlite.query("SELECT file_uri FROM $TABLE_ATTACHMENTS").use { cursor ->
            var total = 0L
            while (cursor.moveToNext()) {
                total += File(cursor.getString(0)).takeIf { it.exists() }?.length() ?: 0L
            }
            total
        }
        BackupExportEstimate(
            includedLabel = if (rowCount > 0) "$rowCount mục" else "Chưa có dữ liệu",
            encryptionLabel = "AES-256-GCM",
            estimatedSize = attachmentsSize + (rowCount * ESTIMATED_ROW_BYTES) + ESTIMATED_BACKUP_OVERHEAD_BYTES,
        )
    }

    fun displayName(uri: Uri): String? =
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }

    private data class AttachmentFile(
        val entryName: String,
        val sourceFile: File,
    )

    private data class ExportData(
        val json: JSONObject,
        val attachments: List<AttachmentFile>,
    )

    private data class WriteResult(
        val checksum: String,
        val bytesWritten: Long,
    )

    private fun buildExportData(): ExportData {
        val sqlite = database.openHelper.writableDatabase
        val tablesJson = JSONObject()
        val attachmentFiles = mutableListOf<AttachmentFile>()

        BACKUP_TABLES.forEach { tableName ->
            val rows = JSONArray()
            sqlite.query("SELECT * FROM $tableName").use { cursor ->
                while (cursor.moveToNext()) {
                    val row = cursorRowToJson(cursor)
                    if (tableName == TABLE_ATTACHMENTS) {
                        val filePath = row.optString("file_uri")
                        val sourceFile = File(filePath)
                        if (!sourceFile.exists() || !sourceFile.isFile) {
                            error("Không tìm thấy file scan: ${sourceFile.name}")
                        }
                        val entryName = "attachments/att_${row.optLong("id")}_${safeArchiveName(sourceFile.name)}"
                        row.put("file_uri", entryName)
                        attachmentFiles += AttachmentFile(entryName, sourceFile)
                    }
                    rows.put(row)
                }
            }
            tablesJson.put(tableName, rows)
        }

        val manifest = JSONObject()
            .put("backupFormatVersion", BACKUP_FORMAT_VERSION)
            .put("databaseVersion", DATABASE_VERSION)
            .put("appVersion", appVersionName())
            .put("createdAt", displayDateTime())
            .put("encrypted", true)

        return ExportData(
            json = JSONObject()
                .put("manifest", manifest)
                .put("tables", tablesJson),
            attachments = attachmentFiles,
        )
    }

    private fun writePayloadZip(zipFile: File, exportData: ExportData) {
        ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
            zip.putNextEntry(ZipEntry(DATA_JSON_ENTRY))
            zip.write(exportData.json.toString().toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            exportData.attachments.forEach { attachment ->
                zip.putNextEntry(ZipEntry(attachment.entryName))
                FileInputStream(attachment.sourceFile).use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    private fun encryptPayloadToUri(
        password: String,
        plaintextZip: File,
        targetUri: Uri,
    ): WriteResult {
        val salt = randomBytes(SALT_SIZE_BYTES)
        val iv = randomBytes(GCM_IV_SIZE_BYTES)
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_SIZE_BITS, iv))
        }
        val header = JSONObject()
            .put("formatVersion", BACKUP_FORMAT_VERSION)
            .put("kdf", KDF_ALGORITHM)
            .put("iterations", KDF_ITERATIONS)
            .put("salt", Base64.getEncoder().encodeToString(salt))
            .put("cipher", CIPHER_TRANSFORMATION)
            .put("iv", Base64.getEncoder().encodeToString(iv))
            .toString()
            .toByteArray(Charsets.UTF_8)

        val digest = MessageDigest.getInstance("SHA-256")
        val rawOutput = contentResolver.openOutputStream(targetUri, "wt")
            ?: error("Không thể mở vị trí lưu file sao lưu.")
        val countingOutput = CountingOutputStream(rawOutput)
        val digestOutput = java.security.DigestOutputStream(countingOutput, digest)

        digestOutput.use { output ->
            DataOutputStream(output).use { dataOutput ->
                dataOutput.write(MAGIC)
                dataOutput.writeInt(header.size)
                dataOutput.write(header)
                CipherOutputStream(dataOutput, cipher).use { cipherOutput ->
                    FileInputStream(plaintextZip).use { input -> input.copyTo(cipherOutput) }
                }
            }
        }

        return WriteResult(
            checksum = digest.digest().toHex(),
            bytesWritten = countingOutput.bytesWritten,
        )
    }

    private fun decryptUriToPayloadZip(password: String, sourceUri: Uri, outputZip: File) {
        val rawInput = contentResolver.openInputStream(sourceUri)
            ?: error("Không thể mở file sao lưu.")

        rawInput.use { input ->
            val dataInput = DataInputStream(input)
            val magic = ByteArray(MAGIC.size)
            dataInput.readFully(magic)
            if (!magic.contentEquals(MAGIC)) {
                error("File sao lưu không đúng định dạng Nestory.")
            }
            val headerLength = dataInput.readInt()
            if (headerLength <= 0 || headerLength > MAX_HEADER_SIZE_BYTES) {
                error("Header file sao lưu không hợp lệ.")
            }
            val headerBytes = ByteArray(headerLength)
            dataInput.readFully(headerBytes)
            val header = JSONObject(String(headerBytes, Charsets.UTF_8))
            if (header.optInt("formatVersion") != BACKUP_FORMAT_VERSION) {
                error("Phiên bản file sao lưu chưa được hỗ trợ.")
            }

            val salt = Base64.getDecoder().decode(header.getString("salt"))
            val iv = Base64.getDecoder().decode(header.getString("iv"))
            val key = deriveKey(password, salt)
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION).apply {
                init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_SIZE_BITS, iv))
            }

            try {
                CipherInputStream(dataInput, cipher).use { cipherInput ->
                    FileOutputStream(outputZip).use { output -> cipherInput.copyTo(output) }
                }
            } catch (t: Throwable) {
                error("Mật khẩu không đúng hoặc file sao lưu đã bị thay đổi.")
            }
        }
    }

    private fun extractPayloadZip(payloadZip: File, extractDir: File): JSONObject {
        var dataJson: JSONObject? = null
        ZipInputStream(FileInputStream(payloadZip)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val safeFile = safeZipTarget(extractDir, entry.name)
                if (!entry.isDirectory) {
                    safeFile.parentFile?.mkdirs()
                    if (entry.name == DATA_JSON_ENTRY) {
                        val bytes = ByteArrayOutputStream()
                        zip.copyTo(bytes)
                        dataJson = JSONObject(bytes.toString(Charsets.UTF_8.name()))
                    } else {
                        FileOutputStream(safeFile).use { zip.copyTo(it) }
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return dataJson ?: error("File sao lưu thiếu dữ liệu khôi phục.")
    }

    private suspend fun restoreData(dataJson: JSONObject, extractDir: File): Int {
        val oldReminders = database.reminderDao().getAll()
        val oldAttachmentPaths = database.attachmentDao().getAllAttachments().map { it.fileUri }
        val restoredAttachmentPaths = mutableSetOf<String>()
        val scheduler = ReminderScheduler(appContext)
        oldReminders.forEach(scheduler::cancel)

        val tables = dataJson.getJSONObject("tables")
        val attachmentsDir = File(appContext.filesDir, "attachments").apply { mkdirs() }
        val sqlite = database.openHelper.writableDatabase

        sqlite.execSQL("PRAGMA foreign_keys=OFF")
        sqlite.beginTransaction()
        try {
            CLEAR_TABLES.forEach { sqlite.execSQL("DELETE FROM $it") }
            runCatching {
                sqlite.execSQL(
                    "DELETE FROM sqlite_sequence WHERE name IN (${CLEAR_TABLES.joinToString(",") { "'$it'" }})"
                )
            }

            IMPORT_TABLES.forEach { tableName ->
                val rows = tables.optJSONArray(tableName) ?: JSONArray()
                for (i in 0 until rows.length()) {
                    val row = rows.getJSONObject(i)
                    if (tableName == TABLE_ATTACHMENTS) {
                        val entryName = row.getString("file_uri")
                        val sourceFile = safeZipTarget(extractDir, entryName)
                        if (!sourceFile.exists()) {
                            error("File scan trong backup bị thiếu: $entryName")
                        }
                        val extension = sourceFile.extension.ifBlank { "bin" }
                        val restoredFile = File(
                            attachmentsDir,
                            "imported_${UUID.randomUUID()}.$extension"
                        )
                        sourceFile.copyTo(restoredFile, overwrite = true)
                        restoredAttachmentPaths += restoredFile.absolutePath
                        row.put("file_uri", restoredFile.absolutePath)
                    }
                    sqlite.insert(tableName, SQLiteDatabase.CONFLICT_REPLACE, row.toContentValues())
                }
            }

            sqlite.setTransactionSuccessful()
        } finally {
            sqlite.endTransaction()
            sqlite.execSQL("PRAGMA foreign_keys=ON")
        }

        oldAttachmentPaths
            .filterNot { it in restoredAttachmentPaths }
            .forEach { path -> runCatching { File(path).takeIf { it.exists() }?.delete() } }
        database.reminderDao().getEnabled().forEach(scheduler::schedule)
        return IMPORT_TABLES.sumOf { tableName -> tables.optJSONArray(tableName)?.length() ?: 0 }
    }

    private fun cursorRowToJson(cursor: Cursor): JSONObject {
        val row = JSONObject()
        for (index in 0 until cursor.columnCount) {
            val name = cursor.getColumnName(index)
            when (cursor.getType(index)) {
                Cursor.FIELD_TYPE_NULL -> row.put(name, JSONObject.NULL)
                Cursor.FIELD_TYPE_INTEGER -> row.put(name, cursor.getLong(index))
                Cursor.FIELD_TYPE_FLOAT -> row.put(name, cursor.getDouble(index))
                Cursor.FIELD_TYPE_STRING -> row.put(name, cursor.getString(index))
                Cursor.FIELD_TYPE_BLOB -> row.put(
                    name,
                    Base64.getEncoder().encodeToString(cursor.getBlob(index))
                )
            }
        }
        return row
    }

    private fun JSONObject.toContentValues(): ContentValues {
        val values = ContentValues()
        keys().forEach { key ->
            val value = get(key)
            when (value) {
                JSONObject.NULL -> values.putNull(key)
                is Int -> values.put(key, value)
                is Long -> values.put(key, value)
                is Double -> values.put(key, value)
                is Boolean -> values.put(key, if (value) 1 else 0)
                else -> values.put(key, value.toString())
            }
        }
        return values
    }

    private fun exportSteps(): List<BackupProgressStep> = listOf(
        BackupProgressStep(BackupOperationStep.PREPARE_DATA, BackupOperationStepStatus.PENDING),
        BackupProgressStep(BackupOperationStep.COMPRESS_DATA, BackupOperationStepStatus.PENDING),
        BackupProgressStep(BackupOperationStep.ENCRYPT_DATA, BackupOperationStepStatus.PENDING),
        BackupProgressStep(BackupOperationStep.SAVE_FILE, BackupOperationStepStatus.PENDING),
        BackupProgressStep(BackupOperationStep.COMPLETE, BackupOperationStepStatus.PENDING),
    )

    private fun importSteps(): List<BackupProgressStep> = listOf(
        BackupProgressStep(BackupOperationStep.VERIFY_BACKUP, BackupOperationStepStatus.PENDING),
        BackupProgressStep(BackupOperationStep.DECRYPT_DATA, BackupOperationStepStatus.PENDING),
        BackupProgressStep(BackupOperationStep.RESTORE_DATA, BackupOperationStepStatus.PENDING),
        BackupProgressStep(BackupOperationStep.COMPLETE, BackupOperationStepStatus.PENDING),
    )

    private fun markCurrentError(
        steps: MutableList<BackupProgressStep>,
        onProgress: (BackupProgressSnapshot) -> Unit,
    ) {
        val errorIndex = steps.indexOfFirst { it.status == BackupOperationStepStatus.PENDING }
            .takeIf { it >= 0 }
            ?: steps.lastIndex
        steps[errorIndex] = steps[errorIndex].copy(status = BackupOperationStepStatus.ERROR)
        onProgress(BackupProgressSnapshot(0.62f, steps.toList()))
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, KDF_ITERATIONS, AES_KEY_SIZE_BITS)
        val bytes = SecretKeyFactory.getInstance(KDF_ALGORITHM).generateSecret(spec).encoded
        return SecretKeySpec(bytes, "AES")
    }

    private fun randomBytes(size: Int): ByteArray = ByteArray(size).also(secureRandom::nextBytes)

    private fun safeZipTarget(root: File, entryName: String): File {
        val target = File(root, entryName)
        val rootPath = root.canonicalPath + File.separator
        val targetPath = target.canonicalPath
        if (!targetPath.startsWith(rootPath)) {
            error("File sao lưu chứa đường dẫn không hợp lệ.")
        }
        return target
    }

    private fun safeArchiveName(name: String): String =
        name.replace(Regex("""[\\/:*?"<>|\u0000-\u001f]"""), "_").ifBlank { "attachment" }

    private fun locationLabel(uri: Uri): String =
        displayName(uri)?.let { "Vị trí bạn đã chọn" } ?: "Vị trí bạn đã chọn"

    private fun sourceSize(uri: Uri): Long =
        contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L

    @Suppress("DEPRECATION")
    private fun appVersionName(): String =
        runCatching {
            appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName ?: "unknown"
        }.getOrDefault("unknown")

    private fun displayDateTime(): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date())

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private class CountingOutputStream(output: OutputStream) : FilterOutputStream(output) {
        var bytesWritten: Long = 0
            private set

        override fun write(b: Int) {
            out.write(b)
            bytesWritten += 1
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            out.write(b, off, len)
            bytesWritten += len
        }
    }

    companion object {
        private const val BACKUP_FORMAT_VERSION = 1
        private const val DATA_JSON_ENTRY = "data.json"
        private const val TABLE_ATTACHMENTS = "attachments"

        private val MAGIC = "NESTORY_BKP_V1\n".toByteArray(Charsets.UTF_8)
        private const val MAX_HEADER_SIZE_BYTES = 16 * 1024
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_SIZE_BYTES = 12
        private const val GCM_TAG_SIZE_BITS = 128
        private const val AES_KEY_SIZE_BITS = 256
        private const val SALT_SIZE_BYTES = 32
        private const val KDF_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val KDF_ITERATIONS = 310_000
        private const val ESTIMATED_ROW_BYTES = 512L
        private const val ESTIMATED_BACKUP_OVERHEAD_BYTES = 16 * 1024L

        private val BACKUP_TABLES = listOf(
            "categories",
            "containers",
            "documents",
            "document_kits",
            "attachments",
            "kit_items",
            "reminders",
        )

        private val IMPORT_TABLES = listOf(
            "categories",
            "containers",
            "documents",
            "document_kits",
            "attachments",
            "kit_items",
            "reminders",
        )

        private val CLEAR_TABLES = listOf(
            "reminders",
            "kit_items",
            "attachments",
            "document_kits",
            "documents",
            "containers",
            "categories",
            "backup_records",
        )
    }
}
