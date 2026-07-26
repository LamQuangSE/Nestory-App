package com.example.nestory.ui.screens.container

import com.example.nestory.data.entity.ContainerEntity
import kotlin.collections.emptyList

/**
 * UI state for the Container screen.
 */
data class ContainerUiState(
    val containerList: List<ContainerEntity> = emptyList(),
    val parentId: Long? = null,
    val containerPath: List<ContainerEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)