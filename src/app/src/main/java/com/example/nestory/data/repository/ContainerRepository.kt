package com.example.nestory.data.repository

import com.example.nestory.data.entity.ContainerEntity
import kotlinx.coroutines.flow.Flow

interface ContainerRepository {
    fun observeAllContainers(): Flow<List<ContainerEntity>>
    fun getContainerById(containerId: Long): Flow<ContainerEntity?>
    fun getChildContainers(parentId: Long?): Flow<List<ContainerEntity>>

    suspend fun getContainerPath(containerId: Long): List<ContainerEntity>
    suspend fun createContainer(container: ContainerEntity): Result<Long>
    suspend fun updateContainer(container: ContainerEntity): Result<Unit>
    suspend fun deleteContainer(container: ContainerEntity): Result<Unit>
}