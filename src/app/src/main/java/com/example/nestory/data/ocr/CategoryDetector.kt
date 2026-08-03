package com.example.nestory.data.ocr

import com.example.nestory.data.model.DocumentCategory
import com.example.nestory.data.model.OcrResult

/**
 * Detects [DocumentCategory] from OCR output using keyword heuristics.
 * Pure business logic; returns null when the category is unknown.
 */
class CategoryDetector {

    private val rules: List<Pair<DocumentCategory, List<String>>> = listOf(
        DocumentCategory.IDENTITY to listOf(
            "căn cước công dân", "chứng minh nhân dân", "chứng minh thư",
            "hộ chiếu", "passport", "cccd", "cmnd", "identity card",
        ),
        DocumentCategory.EDUCATION to listOf(
            "bằng tốt nghiệp", "giấy khai sinh", "học bạ", "thẻ sinh viên",
            "diploma", "transcript", "student", "trường đại học",
        ),
        DocumentCategory.FINANCE to listOf(
            "hóa đơn", "hoá đơn", "hợp đồng vay", "sao kê", "invoice",
            "receipt", "ngân hàng",
        ),
        DocumentCategory.PROPERTY to listOf(
            "sổ đỏ", "sổ hồng", "giấy chứng nhận quyền sử dụng đất",
            "hợp đồng mua bán", "đăng ký xe", "land", "property",
        ),
        DocumentCategory.VEHICLE to listOf(
            "giấy phép lái xe", "bằng lái", "đăng ký xe máy", "đăng ký ô tô",
            "driving license", "vehicle registration",
        ),
        DocumentCategory.HEALTH to listOf(
            "bảo hiểm y tế", "giấy khám bệnh", "kết quả xét nghiệm",
            "health insurance", "medical", "prescription",
        ),
    )

    fun detect(result: OcrResult): DocumentCategory? {
        val searchable = buildString {
            result.documentName?.let { append(" ").append(it) }
            result.rawText?.let { append(" ").append(it) }
        }.lowercase()

        for ((category, keys) in rules) {
            if (keys.any { searchable.contains(it) }) return category
        }
        return null
    }
}

