package com.example.nestory.data.filesystem

import android.content.Context
import androidx.room.Room
import com.example.nestory.data.local.database.AppDatabase
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileSystemManager(private val context: Context) {

    fun isVaultInitialized(): Boolean {
        val prefs = context.getSharedPreferences("nestory_vault_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("vault_initialized", false)
    }

    suspend fun createVaultStructure(): VaultCreationResult = withContext(Dispatchers.IO) {
        val completedSteps = mutableListOf<VaultCreationStep>()

        val filesReady = runCatching { ensureDirectory(context.filesDir) }.getOrElse {
            return@withContext VaultCreationResult(
                completedSteps = completedSteps,
                failedStep = VaultCreationStep.FilesDirectory,
                errorCode = VaultCreationError.FilesDirectoryUnavailable,
            )
        }
        if (!filesReady) {
            return@withContext VaultCreationResult(
                completedSteps = completedSteps,
                failedStep = VaultCreationStep.FilesDirectory,
                errorCode = VaultCreationError.FilesDirectoryUnavailable,
            )
        }
        completedSteps += VaultCreationStep.FilesDirectory

        val cacheReady = runCatching { ensureDirectory(context.cacheDir) }.getOrElse {
            return@withContext VaultCreationResult(
                completedSteps = completedSteps,
                failedStep = VaultCreationStep.CacheDirectory,
                errorCode = VaultCreationError.CacheDirectoryUnavailable,
            )
        }
        if (!cacheReady) {
            return@withContext VaultCreationResult(
                completedSteps = completedSteps,
                failedStep = VaultCreationStep.CacheDirectory,
                errorCode = VaultCreationError.CacheDirectoryUnavailable,
            )
        }
        completedSteps += VaultCreationStep.CacheDirectory

        val prefs = context.getSharedPreferences("nestory_vault_prefs", Context.MODE_PRIVATE)
        val preferencesReady = runCatching {
            prefs.edit().putBoolean("vault_setup_started", true).commit()
        }.getOrDefault(false)
        if (!preferencesReady) {
            return@withContext VaultCreationResult(
                completedSteps = completedSteps,
                failedStep = VaultCreationStep.Preferences,
                errorCode = VaultCreationError.PreferencesWriteFailed,
            )
        }
        completedSteps += VaultCreationStep.Preferences

        val databaseReady = runCatching {
            val db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "nestory_database",
            ).addMigrations(AppDatabase.MIGRATION_1_2).build()

            try {
                db.openHelper.writableDatabase
            } finally {
                db.close()
            }
        }.isSuccess
        if (!databaseReady) {
            return@withContext VaultCreationResult(
                completedSteps = completedSteps,
                failedStep = VaultCreationStep.Database,
                errorCode = VaultCreationError.DatabaseOpenFailed,
            )
        }
        completedSteps += VaultCreationStep.Database

        val initialized = prefs.edit().putBoolean("vault_initialized", true).commit()
        if (!initialized) {
            return@withContext VaultCreationResult(
                completedSteps = completedSteps,
                failedStep = VaultCreationStep.Preferences,
                errorCode = VaultCreationError.PreferencesWriteFailed,
            )
        }

        VaultCreationResult(completedSteps = completedSteps)
    }

    private fun ensureDirectory(directory: File): Boolean =
        directory.exists() && directory.isDirectory || directory.mkdirs()
}
