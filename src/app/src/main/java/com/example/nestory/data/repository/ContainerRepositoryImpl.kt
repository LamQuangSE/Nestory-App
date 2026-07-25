package com.example.nestory.data.repository

import com.example.nestory.data.dao.ContainerDao
import com.example.nestory.data.entity.ContainerEntity
import kotlinx.coroutines.flow.Flow

class ContainerRepositoryImpl(
    private val containerDao: ContainerDao
) : ContainerRepository {

    override fun observeAllContainers(): Flow<List<ContainerEntity>> =
        containerDao.observeAllContainers()

    override suspend fun getAllContainers(): Result<List<ContainerEntity>> =
        runCatching { containerDao.getAllContainers() }

    override fun observeContainerById(containerId: Long): Flow<ContainerEntity?> =
        containerDao.observeById(containerId)

    override suspend fun getContainerById(containerId: Long): Result<ContainerEntity?> =
        runCatching { containerDao.getById(containerId) }

    override fun observeChildContainers(parentId: Long?): Flow<List<ContainerEntity>> =
        containerDao.observeChildrenByParentId(parentId)

    override suspend fun getChildContainers(parentId: Long?): Result<List<ContainerEntity>> =
        runCatching { containerDao.getChildrenByParentId(parentId) }

    override suspend fun getContainerPath(containerId: Long): List<ContainerEntity> {
        val path = mutableListOf<ContainerEntity>()

        var currentId: Long? = containerId

        while (currentId != null) {

            val container = containerDao.getById(currentId)
                ?: break

            path.add(0, container)

            currentId = container.parentId
        }

        return path
    }

    override suspend fun createContainer(container: ContainerEntity): Result<Long> =
        runCatching { containerDao.insert(container) }

    override suspend fun updateContainer(container: ContainerEntity): Result<Unit> =
        runCatching { containerDao.update(container) }

    override suspend fun deleteContainer(container: ContainerEntity): Result<Unit> =
        runCatching { containerDao.delete(container) }
}
