package com.example.nestory.domain.repository

import com.example.nestory.data.local.entity.DocumentKitEntity
import com.example.nestory.relation.KitWithItems
import kotlinx.coroutines.flow.Flow

interface DocumentKitRepository {
    fun observeAllKits(): Flow<List<KitWithItems>>
    suspend fun getAllKits(): Result<List<KitWithItems>>
    fun observeKitById(kitId: Long): Flow<KitWithItems?>
    suspend fun getKitById(kitId: Long): Result<KitWithItems?>
    suspend fun createKit(kit: DocumentKitEntity): Result<Long>
    suspend fun updateKit(kit: DocumentKitEntity): Result<Unit>
    suspend fun deleteKit(kit: DocumentKitEntity): Result<Unit>
    suspend fun updateFavoriteStatus(kitId: Long, isFavorite: Boolean): Result<Unit>
}