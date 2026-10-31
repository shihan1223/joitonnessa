package com.example.ui.water

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.SoftBlueSecondaryLight
import com.example.ui.theme.StatusMissed
import com.example.util.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WaterTrackerScreen(
    viewModel: WaterViewModel,
    language: String,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isBangla = language == "bn"
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isBangla) "পানি পান ট্র্যাকার" else "Water Hydration Tracker") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_water_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .testTag("water_tracker_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Enable / Disable Feature Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isBangla) "পানি পানের রিমাইন্ডার" else "Water Reminders",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isBangla) "নির্দিষ্ট বিরতিতে পানি পানের সতর্কবার্তা" else "Periodic hydration prompts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.isEnabled,
                            onCheckedChange = { viewModel.toggleEnabled(it) },
                            modifier = Modifier.testTag("switch_water_enable")
                        )
                    }
                }
            }

            // Big Progress Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftBlueSecondaryLight.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(SoftBlueSecondaryLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val percentage = if (state.config.dailyGoalMl > 0) {
                            ((state.todayWaterMl.toFloat() / state.config.dailyGoalMl.toFloat()) * 100).toInt()
                        } else 0

                        Text(
                            text = if (isBangla) {
                                "${DateTimeUtils.toBengaliDigits(state.todayWaterMl.toString())} / ${DateTimeUtils.toBengaliDigits(state.config.dailyGoalMl.toString())} মিলি"
                            } else {
                                "${state.todayWaterMl} / ${state.config.dailyGoalMl} ml"
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftBlueSecondaryLight
                        )

                        Text(
                            text = if (isBangla) "দৈনিক লক্ষ্যমাত্রার $percentage% পূরণ হয়েছে" else "$percentage% of daily goal reached",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val progressFraction = if (state.config.dailyGoalMl > 0) {
                            (state.todayWaterMl.toFloat() / state.config.dailyGoalMl.toFloat()).coerceIn(0f, 1f)
                        } else 0f

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = SoftBlueSecondaryLight,
                            trackColor = Color.White
                        )
                    }
                }
            }

            // Quick Add Buttons
            item {
                Text(
                    text = if (isBangla) "দ্রুত পানি পান যোগ করুন" else "Quick Log Water",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.logWater(200) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_log_200"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftBlueSecondaryLight)
                    ) {
                        Text(if (isBangla) "+ ২০০ মিলি" else "+ 200 ml", color = Color.White)
                    }
                    Button(
                        onClick = { viewModel.logWater(250) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_log_250"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftBlueSecondaryLight)
                    ) {
                        Text(if (isBangla) "+ ২৫০ মিলি" else "+ 250 ml", color = Color.White)
                    }
                    Button(
                        onClick = { viewModel.logWater(500) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_log_500"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftBlueSecondaryLight)
                    ) {
                        Text(if (isBangla) "+ ৫০০ মিলি" else "+ 500 ml", color = Color.White)
                    }
                }
            }

            // Daily Goal Presets
            item {
                Text(
                    text = if (isBangla) "দৈনিক লক্ষ্যমাত্রা নির্ধারণ" else "Set Daily Goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val goals = listOf(1500, 2000, 2500, 3000, 3500)
                    goals.forEach { goal ->
                        val isSelected = state.config.dailyGoalMl == goal
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateGoal(goal) },
                            label = { Text(if (isBangla) "${DateTimeUtils.toBengaliDigits(goal.toString())} মিলি" else "$goal ml") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SoftBlueSecondaryLight,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("chip_goal_$goal")
                        )
                    }
                }
            }

            // Interval Presets
            item {
                Text(
                    text = if (isBangla) "রিমাইন্ডার বিরতি" else "Reminder Interval",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val intervals = listOf(45, 60, 90, 120)
                    intervals.forEach { interval ->
                        val isSelected = state.config.intervalMinutes == interval
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateInterval(interval) },
                            label = {
                                Text(
                                    if (isBangla) "প্রতি ${DateTimeUtils.toBengaliDigits(interval.toString())} মিনিট"
                                    else "Every $interval mins"
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SoftBlueSecondaryLight,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("chip_interval_$interval")
                        )
                    }
                }
            }

            // Today's Logs List
            item {
                Text(
                    text = if (isBangla) "আজকের পানি পানের তালিকা" else "Today's Consumption Logs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (state.logs.isEmpty()) {
                item {
                    Text(
                        text = if (isBangla) "আজ এখনও কোনো পানি পানের হিসাব যোগ করা হয়নি।" else "No water consumption logged yet today.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(state.logs, key = { it.id }) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = SoftBlueSecondaryLight,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBangla) "${DateTimeUtils.toBengaliDigits(log.amountMl.toString())} মিলি" else "${log.amountMl} ml",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = timeFormat.format(Date(log.timestamp)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.deleteLog(log.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusMissed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
