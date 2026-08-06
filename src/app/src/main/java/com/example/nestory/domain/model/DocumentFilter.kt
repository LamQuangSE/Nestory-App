package com.example.nestory.domain.model

data class DocumentFilter(
    val category: String? = null,
    val isFavorite: Boolean? = null,
    val containerId: Long? = null,
)
