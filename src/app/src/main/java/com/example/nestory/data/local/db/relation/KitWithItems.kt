package com.example.nestory.data.local.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.nestory.data.local.db.entity.DocumentKitEntity
import com.example.nestory.data.local.db.entity.KitItemEntity

data class KitWithItems(
    @Embedded val kit: DocumentKitEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "document_kit_id" 
    )
    val items: List<KitItemEntity>
)