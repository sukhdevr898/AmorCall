package com.example

import com.example.network.AppUpdateManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `test semver newer comparisons`() {
        assertTrue(AppUpdateManager.isSemverNewer("2.0.1", "2.0"))
        assertTrue(AppUpdateManager.isSemverNewer("2.0.1", "1.2"))
        assertTrue(AppUpdateManager.isSemverNewer("2.0", "1.2"))
        assertTrue(AppUpdateManager.isSemverNewer("1.3", "1.2"))
        assertTrue(AppUpdateManager.isSemverNewer("1.2.1", "1.2"))
        assertTrue(AppUpdateManager.isSemverNewer("v2.0.0", "1.2.0"))

        assertFalse(AppUpdateManager.isSemverNewer("2.0.1", "2.0.1"))
        assertFalse(AppUpdateManager.isSemverNewer("2.0", "2.0.1"))
        assertFalse(AppUpdateManager.isSemverNewer("1.2", "2.0.1"))
    }

    @Test
    fun `test privacy icon types and aliases`() {
        val defaultType = com.example.privacy.PrivacyIconType.fromId("default")
        org.junit.Assert.assertEquals("Duora (Original)", defaultType.title)
        org.junit.Assert.assertEquals("com.example.MainActivityDefault", defaultType.aliasClassName)

        val calcType = com.example.privacy.PrivacyIconType.fromId("calculator")
        org.junit.Assert.assertEquals("Calculator", calcType.title)
        org.junit.Assert.assertEquals("com.example.MainActivityCalculator", calcType.aliasClassName)

        val notesType = com.example.privacy.PrivacyIconType.fromId("notes")
        org.junit.Assert.assertEquals("Notes", notesType.title)

        val clockType = com.example.privacy.PrivacyIconType.fromId("clock")
        org.junit.Assert.assertEquals("Clock", clockType.title)

        val weatherType = com.example.privacy.PrivacyIconType.fromId("weather")
        org.junit.Assert.assertEquals("Weather", weatherType.title)
    }
}
