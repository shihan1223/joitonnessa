package com.example.ui.food

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FoodReminder
import com.example.ui.navigation.Screen
import com.example.ui.theme.StatusSuccess
import com.example.util.DateTimeUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FoodScheduleScreen(
    viewModel: FoodViewModel,
    language: String,
    onNavigateBack: () -> Unit,
    onEditReminder: (Long) -> Unit
) {
    val reminders by viewModel.allFoodReminders.collectAsStateWithLifecycle()
    val config by viewModel.scheduleConfig.collectAsStateWithLifecycle()
    val isBangla = language == "bn"
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedStartTime by remember(config.startTime) { mutableStateOf(config.startTime) }
    var selectedIntervalHours by remember(config.intervalHours) { mutableIntStateOf(config.intervalHours) }
    val totalReminders = 10

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBangla) "১০ বার খাবারের সময়সূচি" else "10-Meal Eating Schedule",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_food_schedule_back")) {
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
                .padding(horizontal = 16.dp)
                .testTag("food_schedule_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Hero Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = if (isBangla) "ব্যক্তিগত খাবারের দৈনিক রুটিন" else "Personalized Food Schedule",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = if (isBangla) "প্রতিদিন ১০টি নিয়মিত খাবারের নোটিফিকেশন" else "10 scheduled eating reminders every day",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = if (isBangla) "শুরু: ${DateTimeUtils.format12Hour(selectedStartTime, "bn")}" else "Start: ${DateTimeUtils.format12Hour(selectedStartTime, "en")}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = if (isBangla) "বিরতি: প্রতি $selectedIntervalHours ঘণ্টা পর পর" else "Interval: Every $selectedIntervalHours hours",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = if (isBangla) "মোট: ১০টি" else "Total: 10",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Schedule Generator Controls Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isBangla) "রুটিন পরিবর্তন ও পুনর্গঠন" else "Customize Schedule Parameters",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Start Time Selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val parts = selectedStartTime.split(":")
                                    val h = parts.getOrNull(0)?.toIntOrNull() ?: 7
                                    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            selectedStartTime = String.format("%02d:%02d", hour, minute)
                                        },
                                        h,
                                        m,
                                        false
                                    ).show()
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (isBangla) "শুরুর সময়" else "Starting Time",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = DateTimeUtils.format12Hour(selectedStartTime, language),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Interval Selector Chips
                        Text(
                            text = if (isBangla) "খাবারের মধ্যবর্তী বিরতি" else "Interval Between Meals",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val intervals = listOf(1, 2, 3)
                            intervals.forEach { hours ->
                                val isSelected = selectedIntervalHours == hours
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedIntervalHours = hours },
                                    label = {
                                        Text(if (isBangla) "$hours ঘণ্টা পর পর" else "Every $hours hours")
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Apply / Regenerate Button
                        Button(
                            onClick = {
                                viewModel.regenerateSchedule(
                                    startTime = selectedStartTime,
                                    intervalHours = selectedIntervalHours,
                                    totalReminders = totalReminders
                                ) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (isBangla) "১০টি খাবারের রিমাইন্ডার সফলভাবে নির্ধারিত হয়েছে!" else "10-meal schedule generated and alarms updated!"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_regenerate_10_meals"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBangla) "১০টি রিমাইন্ডার স্বয়ংক্রিয় তৈরি করুন" else "Generate 10-Meal Reminders",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Section Header
            item {
                Text(
                    text = if (isBangla) "আজকের ১০টি খাবারের তালিকা" else "Daily 10 Eating Reminders",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Reminders List (All 10 items)
            items(reminders, key = { it.id }) { reminder ->
                FoodReminderScheduleCard(
                    reminder = reminder,
                    language = language,
                    isBangla = isBangla,
                    onToggleActive = { isActive -> viewModel.toggleReminderActive(reminder, isActive) },
                    onMarkCompleted = {
                        viewModel.markCompleted(reminder)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (isBangla) "${reminder.displayName} সম্পন্ন হিসেবে সংরক্ষিত হয়েছে" else "${reminder.displayName} marked as completed"
                            )
                        }
                    },
                    onSnooze = {
                        viewModel.snooze(reminder, 10)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (isBangla) "১০ মিনিটের জন্য স্নুজ করা হয়েছে" else "Snoozed for 10 minutes"
                            )
                        }
                    },
                    onEdit = { onEditReminder(reminder.id) }
                )
            }

            item { Spacer(modifier = Modifier.height(48.dp)) }
        }
    }
}

@Composable
fun FoodReminderScheduleCard(
    reminder: FoodReminder,
    language: String,
    isBangla: Boolean,
    onToggleActive: (Boolean) -> Unit,
    onMarkCompleted: () -> Unit,
    onSnooze: () -> Unit,
    onEdit: () -> Unit
) {
    val cardScope = rememberCoroutineScope()
    var isConfirming by remember { mutableStateOf(false) }
    var isBanished by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = !isBanished,
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = 400),
            shrinkTowards = Alignment.Top
        ) + fadeOut(
            animationSpec = tween(durationMillis = 350)
        ) + slideOutHorizontally(
            animationSpec = tween(durationMillis = 350),
            targetOffsetX = { it }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("food_reminder_card_${reminder.orderIndex}"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isConfirming) StatusSuccess.copy(alpha = 0.15f)
                else if (reminder.isActive) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (reminder.isActive) 2.dp else 0.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sequential Index Badge (1..10)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isConfirming) StatusSuccess.copy(alpha = 0.25f)
                                else if (reminder.isActive) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBangla) DateTimeUtils.toBengaliDigits(reminder.orderIndex.toString()) else reminder.orderIndex.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isConfirming) StatusSuccess else if (reminder.isActive) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title and Time
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = DateTimeUtils.format12Hour(reminder.time, language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isConfirming) StatusSuccess else if (reminder.isActive) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                            if (reminder.isNextDay) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Nightlight,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = if (isBangla) "পরবর্তী দিন" else "Next Day",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = reminder.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (reminder.isActive) MaterialTheme.colorScheme.onSurface else Color.Gray
                        )
                    }

                    // Individual Enable / Disable Toggle
                    Switch(
                        checked = reminder.isActive,
                        onCheckedChange = onToggleActive,
                        modifier = Modifier.testTag("switch_meal_${reminder.orderIndex}")
                    )
                }

                if (reminder.isActive && !isConfirming) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Actions row: Mark Completed, Snooze, Edit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sound & Vibration status indicator icons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (reminder.soundEnabled) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Sound On", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                            if (reminder.vibrationEnabled) {
                                Icon(Icons.Default.Vibration, contentDescription = "Vibration On", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Mark Completed Button (Animate off and banish for this day)
                            OutlinedButton(
                                onClick = {
                                    isConfirming = true
                                    cardScope.launch {
                                        kotlinx.coroutines.delay(280L)
                                        isBanished = true
                                        kotlinx.coroutines.delay(380L)
                                        onMarkCompleted()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_completed_${reminder.orderIndex}")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBangla) "খেয়েছি" else "Completed", fontSize = 12.sp, color = StatusSuccess)
                            }

                            // Snooze Button
                            OutlinedButton(
                                onClick = onSnooze,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_snooze_${reminder.orderIndex}")
                            ) {
                                Icon(Icons.Default.Snooze, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isBangla) "১০ মি." else "Snooze", fontSize = 12.sp)
                            }

                            // Edit Button
                            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Meal", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
