package com.example.nestory.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.nestory.data.entity.ContainerEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the `containers` table.
 * Provides CRUD operations and queries for container (folder) entities.
 */
@Dao
interface ContainerDao {
    /** Insert a new container; returns the new row ID. Conflict strategy ABORT. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(container: ContainerEntity): Long

    /** Update an existing container (matched by primary key). */
    @Update
    suspend fun update(container: ContainerEntity)

    /** Delete a container by its primary key. */
    @Delete
    suspend fun delete(container: ContainerEntity)

    /** Retrieve all containers ordered by name (ascending). */
    @Query("SELECT * FROM containers ORDER BY name ASC")
    fun observeAllContainers(): Flow<List<ContainerEntity>>

    /** Retrieve all containers once, ordered by name (ascending). */
    @Query("SELECT * FROM containers ORDER BY name ASC")
    suspend fun getAllContainers(): List<ContainerEntity>

    /** Retrieve a single container by its primary key. */
    @Query("SELECT * FROM containers WHERE id = :containerId LIMIT 1")
    fun observeById(containerId: Long): Flow<ContainerEntity?>

    /** Retrieve a single container once by its primary key. */
    @Query("SELECT * FROM containers WHERE id = :containerId LIMIT 1")
    suspend fun getById(containerId: Long): ContainerEntity?

    /**
     * Retrieve all child containers of a given parent container.
     * If [parentId] is null, returns top‑level containers (where parent_id IS NULL).
     */
    @Query(
        """
        SELECT * FROM containers
        WHERE (:parentId IS NULL AND parent_id IS NULL)
           OR (:parentId IS NOT NULL AND parent_id = :parentId)
        ORDER BY name ASC
        """
    )
    fun observeChildrenByParentId(parentId: Long?): Flow<List<ContainerEntity>>

    /** Retrieve child containers once for a parent. Null returns top-level containers. */
    @Query(
        """
        SELECT * FROM containers
        WHERE (:parentId IS NULL AND parent_id IS NULL)
           OR (:parentId IS NOT NULL AND parent_id = :parentId)
        ORDER BY name ASC
        """
    )
    suspend fun getChildrenByParentId(parentId: Long?): List<ContainerEntity>
}
