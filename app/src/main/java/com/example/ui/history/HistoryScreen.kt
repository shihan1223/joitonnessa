package com.example.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ReminderCategory
import com.example.data.model.ReminderLog
import com.example.data.model.ReminderStatus
import com.example.ui.navigation.Screen
import com.example.ui.theme.StatusMissed
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusSuccess
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    language: String,
    onNavigate: (String) -> Unit
) {
    val isBangla = language == "bn"
    val selectedDate by viewModel.selectedDateIso.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val logs by viewModel.logsForSelectedDate.collectAsStateWithLifecycle()

    val pastDays = remember { DateTimeUtils.getPastDaysIso(14) }

    val filteredLogs = remember(logs, statusFilter) {
        if (statusFilter == null) logs else logs.filter { it.status == statusFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isBangla) "রুটিন ও ঔষধের ইতিহাস" else "Reminder History") },
                actions = {
                    IconButton(
                        onClick = { onNavigate(Screen.Reports.route) },
                        modifier = Modifier.testTag("btn_reports")
                    ) {
                        Icon(Icons.Default.BarChart, contentDescription = "Reports", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("history_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Horizontal Calendar Selector (Last 14 days)
            item {
                Text(
                    text = if (isBangla) "তারিখ নির্বাচন করুন" else "Select Date",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(pastDays) { dateIso ->
                        val isSelected = dateIso == selectedDate
                        val isToday = dateIso == DateTimeUtils.todayIso()

                        val dayDisplay = try {
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                            val d = sdf.parse(dateIso) ?: java.util.Date()
                            val dayOfWeek = java.text.SimpleDateFormat("EEE", java.util.Locale.US).format(d)
                            val dayNum = java.text.SimpleDateFormat("dd", java.util.Locale.US).format(d)
                            "$dayOfWeek\n$dayNum"
                        } catch (_: Exception) {
                            dateIso.takeLast(5)
                        }

                        Card(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { viewModel.selectDate(dateIso) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isToday) (if (isBangla) "আজ" else "Today") else dayDisplay,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Filter status chips
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { viewModel.setStatusFilter(null) },
                        label = { Text(if (isBangla) "সকল" else "All") }
                    )
                    FilterChip(
                        selected = statusFilter == ReminderStatus.TAKEN,
                        onClick = { viewModel.setStatusFilter(ReminderStatus.TAKEN) },
                        label = { Text(if (isBangla) "✓ সম্পন্ন" else "Taken") }
                    )
                    FilterChip(
                        selected = statusFilter == ReminderStatus.SKIPPED,
                        onClick = { viewModel.setStatusFilter(ReminderStatus.SKIPPED) },
                        label = { Text(if (isBangla) "✗ বাদ দেওয়া" else "Skipped") }
                    )
                    FilterChip(
                        selected = statusFilter == ReminderStatus.MISSED,
                        onClick = { viewModel.setStatusFilter(ReminderStatus.MISSED) },
                        label = { Text(if (isBangla) "! মিস" else "Missed") }
                    )
                }
            }

            // History Logs List or Empty State
            if (filteredLogs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isBangla) "এই তারিখে কোনো হিস্ট্রি পাওয়া যায়নি" else "No reminder logs for this date",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isBangla) "নির্ধারিত সময়ে অ্যালার্ম বাজার পর স্বয়ংক্রিয়ভাবে হিস্ট্রি যুক্ত হবে।" else "Logs are recorded automatically when alarms trigger.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredLogs, key = { it.id }) { log ->
                    HistoryLogCard(
                        log = log,
                        isBangla = isBangla,
                        language = language,
                        onMarkTaken = { viewModel.updateLogStatus(log, ReminderStatus.TAKEN) },
                        onMarkSkipped = { viewModel.updateLogStatus(log, ReminderStatus.SKIPPED) },
                        onDelete = { viewModel.deleteLog(log) }
                    )
                }
            }

            // Bottom space
            item { Spacer(modifier = Modifier.height(36.dp)) }
        }
    }
}

@Composable
fun HistoryLogCard(
    log: ReminderLog,
    isBangla: Boolean,
    language: String,
    onMarkTaken: () -> Unit,
    onMarkSkipped: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (log.status) {
        ReminderStatus.TAKEN -> StatusSuccess
        ReminderStatus.SKIPPED -> Color.Gray
        ReminderStatus.MISSED -> StatusMissed
        ReminderStatus.PENDING -> StatusPending
    }

    val statusLabel = if (isBangla) log.status.labelBn else log.status.labelEn

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (log.category == ReminderCategory.MEDICINE) Icons.Default.Medication else Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (log.subtitle.isNotBlank()) {
                    Text(
                        text = log.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = DateTimeUtils.format12Hour(log.scheduledTime, language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Status Modifier and Delete Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (log.status != ReminderStatus.TAKEN) {
                    IconButton(onClick = onMarkTaken, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Check, contentDescription = "Change to Taken", tint = StatusSuccess, modifier = Modifier.size(18.dp))
                    }
                }
                if (log.status != ReminderStatus.SKIPPED) {
                    IconButton(onClick = onMarkSkipped, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Change to Skipped", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Log", tint = StatusMissed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
