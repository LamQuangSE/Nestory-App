package com.example.nestory.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.nestory.data.entity.ContainerEntity

@Dao
interface ContainerDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(container: ContainerEntity): Long

    @Update
    suspend fun update(container: ContainerEntity)

    @Delete
    suspend fun delete(container: ContainerEntity)

    @Query("SELECT * FROM containers ORDER BY name ASC")
    suspend fun getAllContainers(): List<ContainerEntity>

    @Query("SELECT * FROM containers WHERE id = :containerId LIMIT 1")
    suspend fun getById(containerId: Long): ContainerEntity?
}