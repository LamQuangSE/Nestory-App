    package com.example.nestory.data.dao

    import androidx.room.*
    import com.example.nestory.data.entity.DocumentKitEntity
    import com.example.nestory.data.entity.KitItemEntity
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
    @Query("SELECT * FROM document_kits WHERE id = :kitId")
    fun observeKitWithItemsById(kitId: Long):  Flow<KitWithItems?>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertKitItem(item: KitItemEntity): Long

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertKitItems(items: List<KitItemEntity>)

        @Update
        suspend fun updateKitItem(item: KitItemEntity)

    @Delete
    suspend fun deleteKitItem(item: KitItemEntity)

    // --- Repository helpers ---
    @Query("SELECT * FROM document_kits WHERE id = :kitId")
    fun getKitById(kitId: Long): Flow<DocumentKitEntity?>

    @Query("SELECT * FROM kit_items WHERE id = :itemId")
    suspend fun observeKitItemById(itemId: Long): KitItemEntity?
}
