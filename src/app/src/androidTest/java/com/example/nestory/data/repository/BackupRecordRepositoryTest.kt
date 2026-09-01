package com.example.nestory.data.repository

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nestory.data.local.database.AppDatabase
import com.example.nestory.data.local.entity.BackupRecordEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BackupRecordRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: BackupRecordRepositoryImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = BackupRecordRepositoryImpl(database.backupRecordDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun backupRecord(
        fileName: String = "backup_2026_08_18.zip",
        createdAt: String = "2026-08-18T10:00:00",
    ) = BackupRecordEntity(
        fileName = fileName,
        filePath = "/storage/emulated/0/Nestory/backup/$fileName",
        createdAt = createdAt,
        backupVersion = "1",
        appVersion = "1.0.0",
        checksum = "abc123",
        fileSize = 1024L,
    )

    @Test
    fun createBackupRecord_savesMetadataAndReturnsId() = runBlocking {
        val id = repository.createBackupRecord(backupRecord()).getOrThrow()

        val history = repository.getBackupHistory().getOrThrow()

        assertEquals(1, history.size)
        assertEquals(id, history.first().id)
        assertEquals("backup_2026_08_18.zip", history.first().fileName)
        assertEquals("abc123", history.first().checksum)
        assertEquals(1024L, history.first().fileSize)
    }

    @Test
    fun getBackupHistory_returnsRecordsOrderedByCreatedAtDescending() = runBlocking {
        repository.createBackupRecord(backupRecord(fileName = "old", createdAt = "2026-01-01T10:00:00"))
        repository.createBackupRecord(backupRecord(fileName = "newest", createdAt = "2026-08-18T10:00:00"))
        repository.createBackupRecord(backupRecord(fileName = "mid", createdAt = "2026-04-01T10:00:00"))

        val names = repository.getBackupHistory().getOrThrow().map { it.fileName }

        assertEquals(listOf("newest", "mid", "old"), names)
    }

    @Test
    fun getLatestBackup_returnsNewestRecord() = runBlocking {
        repository.createBackupRecord(backupRecord(fileName = "old", createdAt = "2026-01-01T10:00:00"))
        repository.createBackupRecord(backupRecord(fileName = "newest", createdAt = "2026-08-18T10:00:00"))

        val latest = repository.getLatestBackup().getOrThrow()

        assertNotNull(latest)
        assertEquals("newest", latest?.fileName)
    }

    @Test
    fun getLatestBackup_whenEmpty_returnsNull() = runBlocking {
        val latest = repository.getLatestBackup().getOrThrow()

        assertNull(latest)
    }

    @Test
    fun updateBackupRecord_persistsChangedMetadata() = runBlocking {
        val id = repository.createBackupRecord(backupRecord()).getOrThrow()
        val saved = repository.getBackupHistory().getOrThrow().first()

        repository.updateBackupRecord(
            saved.copy(checksum = "new-checksum", fileSize = 2048L),
        )
        val updated = repository.getBackupHistory().getOrThrow().first()

        assertEquals("new-checksum", updated.checksum)
        assertEquals(2048L, updated.fileSize)
    }

    @Test
    fun deleteBackupRecord_removesFromHistory() = runBlocking {
        repository.createBackupRecord(backupRecord(fileName = "keep", createdAt = "2026-01-01T10:00:00"))
        val toDelete = repository.createBackupRecord(
            backupRecord(fileName = "delete", createdAt = "2026-02-01T10:00:00"),
        ).getOrThrow()
        val deleteEntity = repository.getBackupHistory().getOrThrow().first { it.id == toDelete }

        repository.deleteBackupRecord(deleteEntity)
        val names = repository.getBackupHistory().getOrThrow().map { it.fileName }

        assertEquals(listOf("keep"), names)
        assertTrue("delete" !in names)
    }
}