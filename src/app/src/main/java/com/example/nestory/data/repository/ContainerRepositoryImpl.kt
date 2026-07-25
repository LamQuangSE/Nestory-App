package com.example.nestory.data.repository

import com.example.nestory.data.dao.ContainerDao
import com.example.nestory.data.entity.ContainerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ContainerRepositoryImpl(
    private val containerDao: ContainerDao
) : ContainerRepository {

    override fun observeAllContainers(): Flow<List<ContainerEntity>> =
        containerDao.observeAllContainers()

    override fun observeContainerById(containerId: Long): Flow<ContainerEntity?> =
        containerDao.observeById(containerId)

    override fun observeChildContainers(parentId: Long?): Flow<List<ContainerEntity>> =
        containerDao.observeChildrenByParentId(parentId)

    override suspend fun getContainerPath(containerId: Long): List<ContainerEntity> {
        val path = mutableListOf<ContainerEntity>()

        var currentId: Long? = containerId

        while (currentId != null) {

            val container = containerDao.observeById(currentId).first()
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