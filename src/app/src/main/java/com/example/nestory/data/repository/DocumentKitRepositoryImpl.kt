package com.example.nestory.data.repository

import com.example.nestory.data.local.dao.DocumentKitDao
import com.example.nestory.data.local.entity.DocumentKitEntity
import com.example.nestory.domain.repository.DocumentKitRepository
import com.example.nestory.relation.KitWithItems
import kotlinx.coroutines.flow.Flow
import kotlin.runCatching

class DocumentKitRepositoryImpl(
    private val dao: DocumentKitDao,
) : DocumentKitRepository {

    override fun observeAllKits(): Flow<List<KitWithItems>> =
        dao.observeAllKitsWithItems()

    override suspend fun getAllKits(): Result<List<KitWithItems>> =
        runCatching { dao.getAllKitsWithItems() }

    override fun observeKitById(kitId: Long): Flow<KitWithItems?> =
        dao.observeKitWithItemsById(kitId)

    override suspend fun getKitById(kitId: Long): Result<KitWithItems?> =
        runCatching { dao.getKitWithItemsById(kitId) }

    override suspend fun createKit(kit: DocumentKitEntity): Result<Long> =
        runCatching { dao.insertKit(kit) }

    override suspend fun updateKit(kit: DocumentKitEntity): Result<Unit> =
        runCatching { dao.updateKit(kit) }

    override suspend fun deleteKit(kit: DocumentKitEntity): Result<Unit> =
        runCatching { dao.deleteKit(kit) }
}