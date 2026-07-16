package com.example.nestory.data.repository

data class DocumentFilter(
    val category: String? = null,
    val isFavorite: Boolean? = null,
    val containerId: Long? = null,
    val expiresBefore: String? = null,
    val expiresAfter: String? = null,
)
