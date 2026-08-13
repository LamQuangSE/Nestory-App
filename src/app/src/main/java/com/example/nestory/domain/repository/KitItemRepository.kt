package com.example.nestory.domain.repository

import com.example.nestory.data.local.entity.KitItemEntity
import kotlinx.coroutines.flow.Flow

interface KitItemRepository {
    fun observeItemsByKit(kitId: Long): Flow<List<KitItemEntity>>

    suspend fun getItemById(itemId: Long): Result<KitItemEntity?>

    suspend fun addItem(item: KitItemEntity): Result<Long>

    suspend fun updateItem(item: KitItemEntity): Result<Unit>

    suspend fun deleteItem(item: KitItemEntity): Result<Unit>
}
