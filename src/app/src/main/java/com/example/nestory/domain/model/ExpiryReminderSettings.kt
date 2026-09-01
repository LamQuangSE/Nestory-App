package com.example.nestory.domain.model

data class ExpiryReminderSettings(
    val enabled: Boolean = true,
    val leadTimeDays: Int = 7,
    val repeatDaily: Boolean = true,
    val inAppEnabled: Boolean = true,
    val pushEnabled: Boolean = true,
    val hour: Int = 12,
    val minute: Int = 0,
)
