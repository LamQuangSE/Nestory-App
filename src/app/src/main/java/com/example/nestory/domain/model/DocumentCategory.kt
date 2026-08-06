package com.example.nestory.domain.model

enum class DocumentCategory {
    IDENTITY,
    EDUCATION,
    FINANCE,
    PROPERTY,
    VEHICLE,
    HEALTH,
    OTHER;

    fun toVietnameseLabel(): String = when (this) {
        IDENTITY -> "Nhân thân"
        EDUCATION -> "Học vấn"
        FINANCE -> "Tài chính"
        PROPERTY -> "Bất động sản"
        VEHICLE -> "Phương tiện"
        HEALTH -> "Sức khỏe"
        OTHER -> "Khác"
    }
}
