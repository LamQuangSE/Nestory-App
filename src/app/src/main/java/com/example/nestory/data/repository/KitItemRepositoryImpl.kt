package com.example.nestory.data.repository

import com.example.nestory.data.local.dao.KitItemDao
import com.example.nestory.data.local.entity.KitItemEntity
import com.example.nestory.domain.repository.KitItemRepository
import kotlinx.coroutines.flow.Flow

class KitItemRepositoryImpl(
    private val dao: KitItemDao,
) : KitItemRepository {

    override fun observeItemsByKit(kitId: Long): Flow<List<KitItemEntity>> =
        dao.observeItemsByKit(kitId)

    override suspend fun getItemById(itemId: Long): Result<KitItemEntity?> =
        runCatching { dao.getItemById(itemId) }

    override suspend fun addItem(item: KitItemEntity): Result<Long> =
        runCatching {
            item.name?.let { name ->
                if (name.isNotBlank() && dao.countByNameInKit(name, item.documentKitId, 0) > 0) {
                    throw IllegalArgumentException("Tên item đã tồn tại trong bộ hồ sơ này")
                }
            }
            dao.insertItem(item)
        }

    override suspend fun updateItem(item: KitItemEntity): Result<Unit> =
        runCatching {
            item.name?.let { name ->
                if (name.isNotBlank() && dao.countByNameInKit(name, item.documentKitId, item.id) > 0) {
                    throw IllegalArgumentException("Tên item đã tồn tại trong bộ hồ sơ này")
                }
            }
            dao.updateItem(item)
        }

    override suspend fun deleteItem(item: KitItemEntity): Result<Unit> =
        runCatching { dao.deleteItem(item) }
}
