package com.example.nestory.ui.screen.documentkit

import com.example.nestory.data.local.entity.KitItemEntity
import com.example.nestory.relation.KitWithItems

data class DocumentKitUiState(
    val kits: List<KitWithItems> = emptyList(),
    val selectedKit: KitWithItems? = null,
    val kitItems: List<KitItemEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)