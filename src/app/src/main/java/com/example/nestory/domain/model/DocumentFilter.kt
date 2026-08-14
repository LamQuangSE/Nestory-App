package com.example.nestory.domain.model

data class DocumentFilter(
    val categoryId: String? = null,
    val isFavorite: Boolean? = null,
    val containerId: Long? = null,
)
