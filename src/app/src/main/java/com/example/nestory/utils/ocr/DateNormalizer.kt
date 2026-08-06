package com.example.nestory.utils.ocr

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Normalizes date strings found in OCR text into a consistent `DD/MM/YYYY` format.
 * OCR often returns dates without leading zeros or with different separators.
 */
object DateNormalizer {

    private val ymd = Regex("""\b(\d{4})[-/.](\d{1,2})[-/.](\d{1,2})\b""")
    private val dmy = Regex("""\b(\d{1,2})[-/.](\d{1,2})[-/.](\d{4})\b""")
    private val compact = Regex("""\b(\d{8})\b""")

    /**
     * Attempts to parse and normalize a date string. Returns normalized `DD/MM/YYYY`
     * string on success, or null if it cannot be understood.
     */
    fun normalize(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val trimmed = raw.trim()

        return try {
            parseYmd(trimmed)
                ?: parseDmy(trimmed)
                ?: parseCompact(trimmed)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseYmd(input: String): String? {
        val m = ymd.find(input) ?: return null
        val date = LocalDate.of(m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.groupValues[3].toInt())
        return toDdmmyyyy(date)
    }

    private fun parseDmy(input: String): String? {
        val m = dmy.find(input) ?: return null
        val date = LocalDate.of(m.groupValues[3].toInt(), m.groupValues[2].toInt(), m.groupValues[1].toInt())
        return toDdmmyyyy(date)
    }

    private fun parseCompact(input: String): String? {
        val m = compact.find(input) ?: return null
        val y = m.groupValues[1].take(4).toInt()
        val mo = m.groupValues[1].substring(4, 6).toInt()
        val d = m.groupValues[1].substring(6, 8).toInt()
        return toDdmmyyyy(LocalDate.of(y, mo, d))
    }

    private fun toDdmmyyyy(date: LocalDate): String =
        "%02d/%02d/%04d".format(date.dayOfMonth, date.monthValue, date.year)
}
