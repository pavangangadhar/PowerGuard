package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AuthResultStatus
import com.example.data.db.SecurityEvent
import com.example.data.db.SecurityEventType
import com.example.security.DeviceSecurityDetector
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
    fun `read string from context matches PowerGuard`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PowerGuard", appName)
    }

    @Test
    fun `device security detector produces valid report`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val report = DeviceSecurityDetector.performAudit(context)
        assertNotNull(report)
        assertNotNull(report.manufacturerGuidance)
        assertTrue(report.androidVersionName.isNotEmpty())
    }

    @Test
    fun `security event data model holds valid attributes`() {
        val event = SecurityEvent(
            eventType = SecurityEventType.POWER_OFF_ATTEMPT,
            authStatus = AuthResultStatus.SUCCESS,
            authMethod = "Device Credential",
            details = "Authentication passed"
        )
        assertEquals(SecurityEventType.POWER_OFF_ATTEMPT, event.eventType)
        assertEquals(AuthResultStatus.SUCCESS, event.authStatus)
        assertEquals("Device Credential", event.authMethod)
    }
}
