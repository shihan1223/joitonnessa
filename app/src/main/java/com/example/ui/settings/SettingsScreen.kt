package com.example.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.UserPreferences
import com.example.data.repository.UserPreferencesRepository
import com.example.reminder.NotificationHelper
import com.example.ui.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userPreferencesRepository: UserPreferencesRepository,
    notificationHelper: NotificationHelper,
    onNavigate: (String) -> Unit
) {
    val prefs by userPreferencesRepository.userPreferencesFlow.collectAsStateWithLifecycle(initialValue = UserPreferences())
    val isBangla = prefs.language == "bn"
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isBangla) "সেটিংস" else "Settings", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .testTag("settings_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Profile Row
            SettingsCard(
                icon = Icons.Default.Person,
                title = if (isBangla) "ইউজার প্রোফাইল (${prefs.userName})" else "User Profile (${prefs.userName})",
                subtitle = if (isBangla) "নাম ও ব্যক্তিগত তথ্য পরিবর্তন করুন" else "Manage your name and health goals",
                onClick = { onNavigate(Screen.Profile.route) }
            )

            // Language Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isBangla) "ভাষা নির্বাচন (Language)" else "App Language",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = prefs.language == "bn",
                            onClick = { scope.launch { userPreferencesRepository.setLanguage("bn") } },
                            label = { Text("বাংলা (Bangla)") },
                            modifier = Modifier.testTag("chip_lang_bn")
                        )
                        FilterChip(
                            selected = prefs.language == "en",
                            onClick = { scope.launch { userPreferencesRepository.setLanguage("en") } },
                            label = { Text("English") },
                            modifier = Modifier.testTag("chip_lang_en")
                        )
                    }
                }
            }

            // Theme Selection (Light, Dark, System)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isBangla) "থিম মোড" else "Theme Display",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = prefs.themeMode == "SYSTEM",
                            onClick = { scope.launch { userPreferencesRepository.setThemeMode("SYSTEM") } },
                            label = { Text(if (isBangla) "সিস্টেম" else "System") }
                        )
                        FilterChip(
                            selected = prefs.themeMode == "LIGHT",
                            onClick = { scope.launch { userPreferencesRepository.setThemeMode("LIGHT") } },
                            label = { Text(if (isBangla) "লাইট" else "Light") }
                        )
                        FilterChip(
                            selected = prefs.themeMode == "DARK",
                            onClick = { scope.launch { userPreferencesRepository.setThemeMode("DARK") } },
                            label = { Text(if (isBangla) "ডার্ক" else "Dark") }
                        )
                    }
                }
            }

            // Sound and Vibration toggles
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isBangla) "অ্যালার্ম সাউন্ড" else "Alarm Sound",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Switch(
                            checked = prefs.soundEnabled,
                            onCheckedChange = { scope.launch { userPreferencesRepository.setSoundEnabled(it) } },
                            modifier = Modifier.testTag("switch_alarm_sound")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isBangla) "ভাইব্রেশন" else "Vibration",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Switch(
                            checked = prefs.vibrationEnabled,
                            onCheckedChange = { scope.launch { userPreferencesRepository.setVibrationEnabled(it) } },
                            modifier = Modifier.testTag("switch_vibration")
                        )
                    }
                }
            }

            // Snooze Duration (5, 10, 15 mins)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBangla) "স্নুজ করার সময়সীমা" else "Snooze Duration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5, 10, 15, 20).forEach { mins ->
                            FilterChip(
                                selected = prefs.snoozeMinutes == mins,
                                onClick = { scope.launch { userPreferencesRepository.setSnoozeMinutes(mins) } },
                                label = { Text(if (isBangla) "$mins মিনিট" else "$mins mins") }
                            )
                        }
                    }
                }
            }

            // Battery Optimization Guidance
            SettingsCard(
                icon = Icons.Default.BatteryAlert,
                title = if (isBangla) "ব্যাটারি অপটিমাইজেশন সেটিং" else "Battery Optimization & Reliability",
                subtitle = if (isBangla) "সময়মতো অ্যালার্ম বাজতে 'Unrestricted' নির্ধারণ করুন" else "Allow background activity so alarms trigger accurately",
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                }
            )

            // Privacy & About
            SettingsCard(
                icon = Icons.Default.PrivacyTip,
                title = if (isBangla) "গোপনীয়তা নীতি ও অ্যাপ পরিচিতি" else "Privacy Policy & About joitonnessa",
                subtitle = if (isBangla) "আপনার তথ্য কেবল এই ডিভাইসেই সুরক্ষিত" else "Local data storage & offline-first promise",
                onClick = { onNavigate(Screen.PrivacyAbout.route) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
