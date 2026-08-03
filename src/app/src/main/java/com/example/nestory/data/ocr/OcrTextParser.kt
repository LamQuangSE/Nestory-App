package com.example.nestory.data.ocr

import com.example.nestory.data.model.OcrResult

/**
 * Parses raw OCR text into structured [OcrResult] fields.
 * This is business logic on top of ML Kit output (regex + normalization),
 * not machine learning.
 */
class OcrTextParser {

    private val number12 = Regex("""\b(\d{12})\b""")
    private val number9 = Regex("""\b(\d{9})\b""")
    private val passportNumber = Regex("""\b([A-Za-z]\d{7})\b""")

    private val labelPatterns = listOf(
        "họ và tên" to ExtractTarget.Holder,
        "full name" to ExtractTarget.Holder,
        "ngày cấp" to ExtractTarget.IssueDate,
        "ngày sinh" to ExtractTarget.IssueDate,
        "date of birth" to ExtractTarget.IssueDate,
        "có giá trị đến ngày" to ExtractTarget.ExpiryDate,
        "có giá trị đến" to ExtractTarget.ExpiryDate,
        "ngày hết hạn" to ExtractTarget.ExpiryDate,
        "hết hạn" to ExtractTarget.ExpiryDate,
        "expiry" to ExtractTarget.ExpiryDate,
        "date of expiry" to ExtractTarget.ExpiryDate,
        "date of issue" to ExtractTarget.IssueDate,
    )

    fun parse(rawText: String): OcrResult {
        val text = rawText.trim()
        val lines = text.lines().filter { it.isNotBlank() }

        return OcrResult(
            documentName = detectDocumentName(lines, text),
            documentNumber = extractDocumentNumber(text),
            issueDate = extractDate(lines, ExtractTarget.IssueDate),
            expiryDate = extractDate(lines, ExtractTarget.ExpiryDate),
            holderName = extractHolderName(lines),
            rawText = text,
        )
    }

    private fun detectDocumentName(lines: List<String>, text: String): String? {
        val known = listOf(
            "Căn cước công dân" to listOf("căn cước công dân", "căn cuoc cong dan"),
            "Chứng minh nhân dân" to listOf("chứng minh nhân dân", "chứng minh thư"),
            "Hộ chiếu" to listOf("hộ chiếu", "passeport", "passport"),
            "Bằng lái xe" to listOf("giấy phép lái", "bằng lái xe", "giay phep lai"),
            "Sổ hộ khẩu" to listOf("sổ hộ khẩu", "so ho khau"),
            "Giấy khai sinh" to listOf("giấy khai sinh", "giay khai sinh"),
            "Thẻ bảo hiểm y tế" to listOf("thẻ bảo hiểm y tế", "bảo hiểm y tế"),
        )

        // Check the first few lines first (header usually on top).
        val searchable = (lines.take(6).joinToString(" ") + "\n" + text).lowercase()

        for ((name, keys) in known) {
            if (keys.any { searchable.contains(it) }) return name
        }

        // Fallback: use the first short uppercase line as a pseudo-name.
        val firstLine = lines.firstOrNull { it.length in 3..40 }?.trim()
        return if (!firstLine.isNullOrBlank()) firstLine else null
    }

    private fun extractDocumentNumber(text: String): String? {
        // Prefer 12-digit (CCCD/CMND new), then 9-digit (old CMND), then passport.
        number12.find(text)?.let { return it.groupValues[1] }
        passportNumber.find(text)?.let { return it.groupValues[1] }
        number9.find(text)?.let { return it.groupValues[1] }
        return null
    }

    private fun extractDate(lines: List<String>, target: ExtractTarget): String? {
        for ((label, t) in labelPatterns) {
            if (t != target) continue
            for (line in lines) {
                if (line.lowercase().contains(label)) {
                    val date = DateNormalizer.normalize(line)
                    if (date != null) return date
                }
            }
        }
        return null
    }

    private fun extractHolderName(lines: List<String>): String? {
        for (line in lines) {
            val lower = line.lowercase()
            val label = labelPatterns
                .firstOrNull { it.second == ExtractTarget.Holder && lower.contains(it.first) }
                ?.first
                ?: continue

            // Take everything after the label, strip stray punctuation.
            val idx = lower.indexOf(label)
            val value = line
                .substring(idx + label.length)
                .trim()
                .trim(':')
                .trim(' ', '-')
            if (value.isNotBlank()) return value
        }
        return null
    }
}

internal enum class ExtractTarget { Holder, IssueDate, ExpiryDate }

