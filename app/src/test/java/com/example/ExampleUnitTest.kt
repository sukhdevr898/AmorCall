package com.example

import com.example.network.AppUpdateManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `test semver newer comparisons`() {
        assertTrue(AppUpdateManager.isSemverNewer("2.0", "1.2"))
        assertTrue(AppUpdateManager.isSemverNewer("1.3", "1.2"))
        assertTrue(AppUpdateManager.isSemverNewer("1.2.1", "1.2"))
        assertTrue(AppUpdateManager.isSemverNewer("v2.0.0", "1.2.0"))

        assertFalse(AppUpdateManager.isSemverNewer("1.2", "1.2"))
        assertFalse(AppUpdateManager.isSemverNewer("1.1", "1.2"))
        assertFalse(AppUpdateManager.isSemverNewer("1.0.9", "1.2"))
    }
}
