package com.example.nestory.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.nestory.data.local.entity.DocumentKitEntity
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

    @Query(
        """
        SELECT COUNT(*) FROM document_kits
        WHERE name = :name COLLATE NOCASE AND id != :excludeId
        """
    )
    suspend fun countByName(name: String, excludeId: Long): Int

    @Query("UPDATE document_kits SET is_favorite = :isFavorite WHERE id = :kitId")
    suspend fun updateFavoriteStatus(kitId: Long, isFavorite: Boolean)
}
