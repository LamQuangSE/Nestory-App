package com.example.nestory.data.repository

import com.example.nestory.data.dao.ContainerDao
import com.example.nestory.data.entity.ContainerEntity
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.first
import kotlin.runCatching

class ContainerRepositoryImpl(
    private val containerDao: ContainerDao
) : ContainerRepository {
    override fun observeAllContainers(): Flow<List<ContainerEntity>> =
        containerDao.getAllContainers()

    override fun getContainerById(containerId: Long): Flow<ContainerEntity?> =
        containerDao.getById(containerId)

    override fun getChildContainers(parentId: Long?): Flow<List<ContainerEntity>> =
        containerDao.getChildrenByParentId(parentId)

    override fun getContainerPath(containerId: Long): List<ContainerEntity> {
        val path = mutableListOf<ContainerEntity>()
        var currentId = containerId
        while (currentId != 0L) {
            val container = containerDao.getById(currentId).first() // suspend until first emission
            container?.let {
                // prepend to maintain order from root to current
                path.add(0, it)
                currentId = it.parentId ?: 0L // 0L as sentinel for no parent
            } ?: break
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