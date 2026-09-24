package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.db.AuthResultStatus
import com.example.data.db.SecurityEventType
import com.example.data.repository.SecurityRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val repository = SecurityRepository(context)
            CoroutineScope(Dispatchers.IO).launch {
                val bootAuthRequired = repository.bootAuthEnabled.first()
                repository.logSecurityEvent(
                    eventType = SecurityEventType.DEVICE_BOOT,
                    authStatus = if (bootAuthRequired) AuthResultStatus.BLOCKED else AuthResultStatus.SUCCESS,
                    authMethod = "System Boot",
                    details = "Device restarted. Boot protection is ${if (bootAuthRequired) "ACTIVE (screen lock verified)" else "INACTIVE"}"
                )
            }
        }
    }
}
