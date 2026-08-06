package com.example.nestory.data.filesystem

data class VaultCreationResult(
    val completedSteps: List<VaultCreationStep>,
    val failedStep: VaultCreationStep? = null,
    val errorCode: VaultCreationError? = null
) {
    val isSuccess: Boolean = errorCode == null
}

enum class VaultCreationStep {
    FilesDirectory,
    CacheDirectory,
    Preferences,
    Database
}

enum class VaultCreationError {
    FilesDirectoryUnavailable,
    CacheDirectoryUnavailable,
    PreferencesWriteFailed,
    DatabaseOpenFailed,
    Unknown
}
