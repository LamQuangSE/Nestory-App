package com.example.nestory.domain.model

enum class DocumentCategory {
    IDENTITY,
    EDUCATION,
    FINANCE,
    PROPERTY,
    VEHICLE,
    HEALTH;

    fun toVietnameseLabel(): String = when (this) {
        IDENTITY -> "Danh tính"
        EDUCATION -> "Học vấn"
        FINANCE -> "Tài chính"
        PROPERTY -> "Tài sản"
        VEHICLE -> "Phương tiện"
        HEALTH -> "Sức khỏe"
    }
}
