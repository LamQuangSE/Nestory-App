package com.example.nestory.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.nestory.data.local.entity.DocumentKitEntity
import com.example.nestory.data.local.entity.KitItemEntity
import com.example.nestory.relation.KitWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentKitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKit(kit: DocumentKitEntity): Long

    @Update
    suspend fun updateKit(kit: DocumentKitEntity)

    @Delete
    suspend fun deleteKit(kit: DocumentKitEntity)

    @Transaction
    @Query("SELECT * FROM document_kits")
    fun observeAllKitsWithItems(): Flow<List<KitWithItems>>

    @Transaction
    @Query("SELECT * FROM document_kits")
    suspend fun getAllKitsWithItems(): List<KitWithItems>

    @Transaction
    @Query("SELECT * FROM document_kits WHERE id = :kitId")
    fun observeKitWithItemsById(kitId: Long): Flow<KitWithItems?>

    @Transaction
    @Query("SELECT * FROM document_kits WHERE id = :kitId")
    suspend fun getKitWithItemsById(kitId: Long): KitWithItems?

    @Query("SELECT * FROM document_kits WHERE id = :kitId")
    suspend fun getKitById(kitId: Long): DocumentKitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKitItem(item: KitItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKitItems(items: List<KitItemEntity>)

    @Update
    suspend fun updateKitItem(item: KitItemEntity)

    @Delete
    suspend fun deleteKitItem(item: KitItemEntity)

    @Query("SELECT * FROM kit_items WHERE id = :itemId")
    suspend fun getKitItemById(itemId: Long): KitItemEntity?
}
