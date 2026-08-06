package com.example.nestory.security

import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VaultUnlockSessionState(
    val isUnlocked: Boolean = false,
    val unlockedAtMillis: Long? = null,
    val backgroundedAtMillis: Long? = null,
)

class VaultUnlockSessionManager(
    private val clock: () -> Long = { SystemClock.elapsedRealtime() },
    private val backgroundTimeoutMillis: Long = DEFAULT_BACKGROUND_TIMEOUT_MILLIS,
) {
    private val _state = MutableStateFlow(VaultUnlockSessionState())
    val state: StateFlow<VaultUnlockSessionState> = _state.asStateFlow()

    fun markUnlocked() {
        val now = clock()
        _state.value = VaultUnlockSessionState(
            isUnlocked = true,
            unlockedAtMillis = now,
            backgroundedAtMillis = null,
        )
    }

    fun lock() {
        _state.value = VaultUnlockSessionState()
    }

    fun onAppBackgrounded() {
        val current = _state.value
        if (!current.isUnlocked) return

        _state.value = current.copy(backgroundedAtMillis = clock())
    }

    fun onAppForegrounded(): Boolean = isSessionValid()

    fun isSessionValid(): Boolean {
        val current = _state.value
        if (!current.isUnlocked) return false

        val backgroundedAt = current.backgroundedAtMillis ?: return true
        val isExpired = clock() - backgroundedAt >= backgroundTimeoutMillis
        if (isExpired) {
            lock()
            return false
        }

        _state.value = current.copy(backgroundedAtMillis = null)
        return true
    }

    companion object {
        const val DEFAULT_BACKGROUND_TIMEOUT_MILLIS: Long = 5 * 60 * 1000L
    }
}

object VaultUnlockSessionProvider {
    val manager = VaultUnlockSessionManager()
}
