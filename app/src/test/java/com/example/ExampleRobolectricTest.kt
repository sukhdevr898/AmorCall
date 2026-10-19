package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.CoupleRepository
import com.example.model.CallType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Duora", appName)
  }

  @Test
  fun `couple repository initializes profile and memories`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = CoupleRepository(context)
    val profile = repo.profile.value
    assertNotNull(profile)
    assertEquals("My Love", profile.partnerName)

    val memories = repo.memories.value
    assertTrue(memories.isNotEmpty())

    repo.recordCall(CallType.VOICE, 120, true)
    val history = repo.callHistory.value
    assertTrue(history.isNotEmpty())
    assertEquals(CallType.VOICE, history.first().type)

    repo.clearCallHistory()
    assertTrue(repo.callHistory.value.isEmpty())

    repo.setNotificationsMuted(true)
    assertTrue(repo.profile.value.isNotificationsMuted)
  }
}
