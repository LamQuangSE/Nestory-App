package com.example.nestory.data.repository

import com.example.nestory.data.dao.DocumentKitDao
import com.example.nestory.data.entity.DocumentKitEntity
import com.example.nestory.data.entity.KitItemEntity
import com.example.nestory.relation.KitWithItems
import kotlinx.coroutines.flow.Flow
import kotlin.runCatching

class DocumentKitRepositoryImpl(
    private val dao: DocumentKitDao
) : DocumentKitRepository {

    // Kit operations
    override fun observeAllKits(): Flow<List<KitWithItems>> =
        dao.observeAllKitsWithItems()

    override fun getKitById(kitId: Long): Flow<KitWithItems?> =
        dao.observeKitWithItemsById(kitId)
    
    override suspend fun createKit(kit: DocumentKitEntity): Result<Long> =
        runCatching { dao.insertKit(kit) }

    override suspend fun updateKit(kit: DocumentKitEntity): Result<Unit> =
        runCatching { dao.updateKit(kit) }

    override suspend fun deleteKit(kit: DocumentKitEntity): Result<Unit> =
        runCatching { dao.deleteKit(kit) }

    // KitItem operations
    override suspend fun addKitItem(item: KitItemEntity): Result<Long> =
        runCatching { dao.insertKitItem(item) }

    override suspend fun updateKitItem(item: KitItemEntity): Result<Unit> =
        runCatching { dao.updateKitItem(item) }

    override suspend fun deleteKitItem(item: KitItemEntity): Result<Unit> =
        runCatching { dao.deleteKitItem(item) }

}