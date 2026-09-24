package com.example.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.db.AuthResultStatus
import com.example.data.db.SecurityEventType
import com.example.data.repository.SecurityRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeviceAdminSecurityReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "PowerGuard Device Administrator Enabled", Toast.LENGTH_SHORT).show()
        val repo = SecurityRepository(context)
        CoroutineScope(Dispatchers.IO).launch {
            repo.logSecurityEvent(
                eventType = SecurityEventType.SETTINGS_CHANGED,
                authStatus = AuthResultStatus.SUCCESS,
                authMethod = "System",
                details = "Device Administrator privileges granted to PowerGuard"
            )
        }
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "PowerGuard Device Administrator Disabled", Toast.LENGTH_SHORT).show()
        val repo = SecurityRepository(context)
        CoroutineScope(Dispatchers.IO).launch {
            repo.logSecurityEvent(
                eventType = SecurityEventType.SETTINGS_CHANGED,
                authStatus = AuthResultStatus.CANCELLED,
                authMethod = "System",
                details = "Device Administrator privileges revoked from PowerGuard"
            )
        }
    }
}
