package com.example.nestory.domain.model

data class ExpiryReminderSettings(
    val enabled: Boolean = true,
    val leadTimeDays: Int = 7,
    val repeatDaily: Boolean = true,
    val inAppEnabled: Boolean = true,
    val emailEnabled: Boolean = false,
    val pushEnabled: Boolean = true,
)
