package com.example.nestory.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.nestory.data.local.entity.KitItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KitItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: KitItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<KitItemEntity>)

    @Update
    suspend fun updateItem(item: KitItemEntity)

    @Delete
    suspend fun deleteItem(item: KitItemEntity)

    @Query("SELECT * FROM kit_items WHERE id = :itemId")
    suspend fun getItemById(itemId: Long): KitItemEntity?

    @Query("SELECT * FROM kit_items WHERE document_kit_id = :kitId")
    fun observeItemsByKit(kitId: Long): Flow<List<KitItemEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM kit_items
        WHERE name = :name COLLATE NOCASE
            AND document_kit_id = :kitId AND id != :excludeId
        """
    )
    suspend fun countByNameInKit(name: String, kitId: Long, excludeId: Long): Int
}
