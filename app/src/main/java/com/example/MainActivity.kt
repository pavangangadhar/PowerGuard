package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProtectionSettingsScreen
import com.example.ui.screens.SecurityActivityScreen
import com.example.ui.screens.SecurityStatusScreen
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PowerGuardViewModel

enum class NavigationTab(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Dashboard", Icons.Default.Shield, "tab_dashboard"),
    STATUS("Status", Icons.Default.Security, "tab_status"),
    SETTINGS("Settings", Icons.Default.Settings, "tab_settings"),
    ACTIVITY("Activity", Icons.Default.History, "tab_activity")
}

class MainActivity : ComponentActivity() {

    private val viewModel: PowerGuardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = true) {
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                if (!state.onboardingCompleted) {
                    OnboardingScreen(
                        audit = state.auditReport,
                        onFinishOnboarding = { viewModel.completeOnboarding() },
                        onRefreshAudit = { viewModel.refreshAudit() },
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                    )
                } else {
                    PowerGuardAppScaffold(
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check security state whenever user returns to the app
        viewModel.refreshAudit()
    }
}

@Composable
fun PowerGuardAppScaffold(
    viewModel: PowerGuardViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = CyberNavySurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .background(CyberNavyDark)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("powerguard_bottom_nav")
            ) {
                NavigationTab.entries.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberNavyDark,
                            selectedTextColor = NeonCyan,
                            indicatorColor = NeonCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CyberNavyDark)
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = selectedTab,
                label = "NavigationCrossfade"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> MainDashboardScreen(
                        state = state,
                        onTogglePowerOff = { viewModel.setPowerOffProtection(it) },
                        onToggleRestart = { viewModel.setRestartProtection(it) },
                        onSimulateAction = { viewModel.launchTestSimulation(it) },
                        onNavigateToStatus = { selectedTab = 1 },
                        onNavigateToLogs = { selectedTab = 3 },
                        onRefreshAudit = { viewModel.refreshAudit() }
                    )
                    1 -> SecurityStatusScreen(
                        state = state
                    )
                    2 -> ProtectionSettingsScreen(
                        state = state,
                        onTogglePowerOff = { viewModel.setPowerOffProtection(it) },
                        onToggleRestart = { viewModel.setRestartProtection(it) },
                        onToggleBoot = { viewModel.setBootProtection(it) },
                        onToggleOnlyWhenLocked = { viewModel.setProtectOnlyWhenLocked(it) },
                        onToggleHapticFeedback = { viewModel.setHapticFeedback(it) }
                    )
                    3 -> SecurityActivityScreen(
                        state = state,
                        onClearLogs = { viewModel.clearAuditLogs() },
                        onDeleteLog = { viewModel.deleteLog(it) }
                    )
                }
            }
        }
    }
}
