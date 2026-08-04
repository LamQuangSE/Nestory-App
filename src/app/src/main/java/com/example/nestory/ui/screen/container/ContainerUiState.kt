package com.example.nestory.ui.screen.container

import com.example.nestory.data.local.entity.ContainerEntity
import kotlin.collections.emptyList
import kotlin.collections.emptySet

data class ContainerUiState(
    val allContainers: List<ContainerEntity> = emptyList(),
    val expandedIds: Set<Long> = emptySet(),
    val selectedContainerId: Long? = null,
    val containerPath: List<ContainerEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val rootContainers: List<ContainerEntity>
        get() = allContainers.filter { it.parentId == null }

    fun getChildren(parentId: Long): List<ContainerEntity> =
        allContainers.filter { it.parentId == parentId }

    fun isExpanded(containerId: Long): Boolean =
        containerId in expandedIds
}
