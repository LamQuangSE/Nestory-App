package com.example.nestory.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VaultUnlockSessionManagerTest {

    private fun createManager(
        now: () -> Long,
        timeoutMillis: Long = VaultUnlockSessionManager.DEFAULT_BACKGROUND_TIMEOUT_MILLIS,
    ): VaultUnlockSessionManager = VaultUnlockSessionManager(clock = now, backgroundTimeoutMillis = timeoutMillis)

    @Test
    fun markUnlocked_setsUnlockedStateWithTimestamp() {
        var now = 1000L
        val manager = createManager(now = { now })

        manager.markUnlocked()

        val state = manager.state.value
        assertTrue(state.isUnlocked)
        assertEquals(1000L, state.unlockedAtMillis)
        assertNull(state.backgroundedAtMillis)
    }

    @Test
    fun backgroundTimeout_exceedingFiveMinutes_locksSession() {
        var now = 0L
        val manager = createManager(now = { now })
        manager.markUnlocked()

        manager.onAppBackgrounded()
        now = VaultUnlockSessionManager.DEFAULT_BACKGROUND_TIMEOUT_MILLIS + 1

        assertFalse(manager.isSessionValid())
        assertFalse(manager.state.value.isUnlocked)
    }

    @Test
    fun resumeBeforeTimeout_keepsSessionValidAndClearsTimer() {
        var now = 0L
        val manager = createManager(now = { now })
        manager.markUnlocked()

        manager.onAppBackgrounded()
        now = VaultUnlockSessionManager.DEFAULT_BACKGROUND_TIMEOUT_MILLIS - 1

        assertTrue(manager.onAppForegrounded())
        val state = manager.state.value
        assertTrue(state.isUnlocked)
        assertNull(state.backgroundedAtMillis)
    }

    @Test
    fun isSessionValid_whenNeverUnlocked_returnsFalse() {
        var now = 0L
        val manager = createManager(now = { now })

        assertFalse(manager.isSessionValid())
    }

    @Test
    fun lock_resetsSessionState() {
        var now = 0L
        val manager = createManager(now = { now })
        manager.markUnlocked()

        manager.lock()

        val state = manager.state.value
        assertFalse(state.isUnlocked)
        assertNull(state.unlockedAtMillis)
        assertNull(state.backgroundedAtMillis)
    }

    @Test
    fun onAppBackgrounded_whenLocked_doesNotSetTimer() {
        var now = 0L
        val manager = createManager(now = { now })

        manager.onAppBackgrounded()

        assertNull(manager.state.value.backgroundedAtMillis)
    }
}