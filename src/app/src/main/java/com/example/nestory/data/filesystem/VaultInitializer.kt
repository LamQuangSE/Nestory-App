package com.example.nestory.data.filesystem

interface VaultInitializer {
    suspend fun createVaultStructure(): VaultCreationResult
}
