package com.example.nestory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a reminder that can be associated with either a document or a document kit, but not both.
 * Business rule: Exactly one of document_id or document_kit_id must be non-null (XOR).
 * Validation will be enforced in the data layer (Phase 4).
 */
@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["document_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
        ForeignKey(
            entity = DocumentKitEntity::class,
            parentColumns = ["id"],
            childColumns = ["document_kit_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [
        Index(value = ["document_id"], unique = true),
        Index(value = ["document_kit_id"], unique = true),
    ],
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "document_id")
    val documentId: Long? = null,

    @ColumnInfo(name = "document_kit_id")
    val documentKitId: Long? = null,

    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,

    @ColumnInfo(name = "reminder_date")
    val reminderDate: String? = null,

    @ColumnInfo(name = "reminder_time")
    val reminderTime: String? = null,

    @ColumnInfo(name = "lead_time_days")
    val leadTimeDays: Int = 7,

    @ColumnInfo(name = "custom_lead_time_mode")
    val customLeadTimeMode: Boolean = false,

    @ColumnInfo(name = "repeat_daily")
    val repeatDaily: Boolean = true,

    @ColumnInfo(name = "in_app_enabled")
    val inAppEnabled: Boolean = true,

    @ColumnInfo(name = "push_enabled")
    val pushEnabled: Boolean = true,
)
