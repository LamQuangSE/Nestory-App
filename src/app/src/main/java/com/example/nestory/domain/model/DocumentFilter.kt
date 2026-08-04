package com.example.nestory.domain.model

import com.example.nestory.domain.model.DocumentCategory

data class DocumentFilter(
    val category: DocumentCategory? = null,
    val isFavorite: Boolean? = null,
    val containerId: Long? = null,
)