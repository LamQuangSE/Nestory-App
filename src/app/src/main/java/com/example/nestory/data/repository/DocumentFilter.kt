package com.example.nestory.data.repository

import com.example.nestory.data.model.DocumentCategory

data class DocumentFilter(
    val category: DocumentCategory? = null,
    val isFavorite: Boolean? = null,
    val containerId: Long? = null,
)