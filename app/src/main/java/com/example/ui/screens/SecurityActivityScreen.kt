package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AuthResultStatus
import com.example.data.db.SecurityEvent
import com.example.data.db.SecurityEventType
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedBg
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ShieldGreen
import com.example.ui.theme.ShieldGreenBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.PowerGuardUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SecurityActivityScreen(
    state: PowerGuardUiState,
    onClearLogs: () -> Unit,
    onDeleteLog: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<AuthResultStatus?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }

    val filteredEvents = remember(state.recentEvents, selectedFilter) {
        if (selectedFilter == null) {
            state.recentEvents
        } else {
            state.recentEvents.filter { it.authStatus == selectedFilter }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = CyberNavyCard,
            title = {
                Text("Clear Security Activity Log?", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("This will permanently remove all local power-off audit records.", color = TextSecondary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearLogs()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberNavyDark)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Security Activity",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "Immutable audit of all power authentication events",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            if (state.recentEvents.isNotEmpty()) {
                IconButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.testTag("clear_logs_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear logs",
                        tint = AlertRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { selectedFilter = null },
                label = { Text("All (${state.recentEvents.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NeonCyan,
                    selectedLabelColor = CyberNavyDark,
                    containerColor = CyberNavyCard,
                    labelColor = TextSecondary
                )
            )

            FilterChip(
                selected = selectedFilter == AuthResultStatus.SUCCESS,
                onClick = {
                    selectedFilter = if (selectedFilter == AuthResultStatus.SUCCESS) null else AuthResultStatus.SUCCESS
                },
                label = { Text("Successful", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ShieldGreen,
                    selectedLabelColor = CyberNavyDark,
                    containerColor = CyberNavyCard,
                    labelColor = TextSecondary
                )
            )

            FilterChip(
                selected = selectedFilter == AuthResultStatus.FAILED,
                onClick = {
                    selectedFilter = if (selectedFilter == AuthResultStatus.FAILED) null else AuthResultStatus.FAILED
                },
                label = { Text("Failed / Cancelled", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AlertRed,
                    selectedLabelColor = CyberNavyDark,
                    containerColor = CyberNavyCard,
                    labelColor = TextSecondary
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Activity Found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Events are logged when shutdown or restart is attempted.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("security_events_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredEvents, key = { it.id }) { event ->
                    SecurityEventItemCard(event = event, onDelete = { onDeleteLog(event.id) })
                }
            }
        }
    }
}

@Composable
fun SecurityEventItemCard(
    event: SecurityEvent,
    onDelete: () -> Unit
) {
    val formatter = SimpleDateFormat("dd MMM yyyy – hh:mm a", Locale.getDefault())
    val dateStr = formatter.format(Date(event.timestamp))

    val icon = when (event.eventType) {
        SecurityEventType.POWER_OFF_ATTEMPT -> Icons.Default.PowerSettingsNew
        SecurityEventType.RESTART_ATTEMPT -> Icons.Default.Refresh
        SecurityEventType.DEVICE_BOOT -> Icons.Default.Security
        SecurityEventType.PROTECTION_TOGGLED -> Icons.Default.Lock
        else -> Icons.Default.History
    }

    val actionTitle = when (event.eventType) {
        SecurityEventType.POWER_OFF_ATTEMPT -> "Power-off authentication requested"
        SecurityEventType.RESTART_ATTEMPT -> "Restart authentication requested"
        SecurityEventType.DEVICE_BOOT -> "Device booted / startup check"
        SecurityEventType.PROTECTION_TOGGLED -> "Protection toggled"
        SecurityEventType.SETTINGS_CHANGED -> "Security configuration changed"
        SecurityEventType.TEST_SIMULATION -> "Authentication test simulation"
    }

    val statusColor = when (event.authStatus) {
        AuthResultStatus.SUCCESS -> ShieldGreen
        AuthResultStatus.FAILED -> AlertRed
        AuthResultStatus.BLOCKED -> WarningAmber
        AuthResultStatus.CANCELLED -> TextMuted
    }

    val statusText = when (event.authStatus) {
        AuthResultStatus.SUCCESS -> "Authentication successful"
        AuthResultStatus.FAILED -> "Authentication failed"
        AuthResultStatus.BLOCKED -> "Action blocked"
        AuthResultStatus.CANCELLED -> "Authentication cancelled"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberNavyBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )

                if (event.isSimulated) {
                    Text(
                        text = "TEST SIMULATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarningAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (event.authStatus == AuthResultStatus.SUCCESS) ShieldGreenBg else AlertRedBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = actionTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Method: ${event.authMethod}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )

                Text(
                    text = event.details,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    maxLines = 1
                )
            }
        }
    }
}
