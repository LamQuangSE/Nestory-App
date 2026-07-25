package com.example.nestory.data.repository

import com.example.nestory.data.entity.DocumentKitEntity
import com.example.nestory.data.entity.KitItemEntity
import com.example.nestory.relation.KitWithItems
import kotlinx.coroutines.flow.Flow

interface DocumentKitRepository {
    // Kit operations
    fun observeAllKits(): Flow<List<KitWithItems>>
    fun getKitById(kitId: Long): Flow<KitWithItems?>
    suspend fun createKit(kit: DocumentKitEntity): Result<Long>
    suspend fun updateKit(kit: DocumentKitEntity): Result<Unit>
    suspend fun deleteKit(kit: DocumentKitEntity): Result<Unit>

    // Kit item CRUD
    suspend fun addKitItem(item: KitItemEntity): Result<Long>
    suspend fun updateKitItem(item: KitItemEntity): Result<Unit>
    suspend fun deleteKitItem(item: KitItemEntity): Result<Unit>

}