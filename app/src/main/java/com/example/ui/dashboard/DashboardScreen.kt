package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.model.ReminderCategory
import com.example.ui.components.MetricCard
import com.example.ui.components.ReminderItemCard
import com.example.ui.navigation.Screen
import com.example.ui.theme.SoftBlueSecondaryLight
import com.example.ui.theme.StatusMissed
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TealPrimaryLight
import com.example.util.DateTimeUtils

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isBangla = state.preferences.language == "bn"

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate(Screen.AddMedicine.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_medicine")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Medicine")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("dashboard_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header / Greeting Card
            item {
                HeaderGreetingCard(
                    userName = state.preferences.userName,
                    todayIso = state.todayIso,
                    progress = state.progressFraction,
                    isBangla = isBangla,
                    onReportsClick = { onNavigate(Screen.Reports.route) }
                )
            }

            // Battery Reliability Notice (Dismissible)
            if (!state.preferences.batteryNoticeDismissed) {
                item {
                    BatteryReliabilityNotice(
                        isBangla = isBangla,
                        onDismiss = { viewModel.dismissBatteryNotice() }
                    )
                }
            }

            // Metrics row: Completed, Pending, Missed
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = if (isBangla) "সম্পন্ন" else "Taken",
                        value = if (isBangla) DateTimeUtils.toBengaliDigits(state.completedCount.toString()) else state.completedCount.toString(),
                        subtitle = if (isBangla) "আজকের ডোজ" else "Today's doses",
                        icon = Icons.Default.CheckCircle,
                        iconTint = StatusSuccess,
                        containerColor = StatusSuccess.copy(alpha = 0.1f),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = if (isBangla) "অপেক্ষমান" else "Pending",
                        value = if (isBangla) DateTimeUtils.toBengaliDigits(state.pendingCount.toString()) else state.pendingCount.toString(),
                        subtitle = if (isBangla) "বাকি রয়েছে" else "Upcoming",
                        icon = Icons.Default.PendingActions,
                        iconTint = StatusPending,
                        containerColor = StatusPending.copy(alpha = 0.1f),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = if (isBangla) "মিস" else "Missed",
                        value = if (isBangla) DateTimeUtils.toBengaliDigits(state.missedCount.toString()) else state.missedCount.toString(),
                        subtitle = if (isBangla) "বাদ গেছে" else "Unrecorded",
                        icon = Icons.Default.Warning,
                        iconTint = StatusMissed,
                        containerColor = StatusMissed.copy(alpha = 0.1f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Action Buttons (Add Medicine, Add Meal, Water)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onNavigate(Screen.AddMedicine.route) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_quick_add_med"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBangla) "+ ঔষধ" else "+ Medicine",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { onNavigate(Screen.FoodSchedule.route) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_quick_add_food"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBangla) "১০ বার খাবার রুটিন" else "10-Meal Routine",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Water Tracker Card
            if (state.waterConfig.isEnabled) {
                item {
                    WaterTrackerCard(
                        currentMl = state.todayWaterIntakeMl,
                        goalMl = state.waterConfig.dailyGoalMl,
                        isBangla = isBangla,
                        onAddWater = { viewModel.quickAddWater(it) },
                        onOpenWater = { onNavigate(Screen.Water.route) }
                    )
                }
            }

            // Section Header: Today's Routine
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "আজকের অপেক্ষমান রুটিন" else "Today's Pending Routines",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isBangla) "সব রুটিন দেখুন" else "View all",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigate(Screen.Schedule.route) }
                    )
                }
            }

            val pendingItems = state.todayItems.filter { it.status == com.example.data.model.ReminderStatus.PENDING }
            val completedItems = state.todayItems.filter { it.status != com.example.data.model.ReminderStatus.PENDING }

            // Pending today's items or All Completed celebration
            if (pendingItems.isEmpty() && state.todayItems.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = StatusSuccess.copy(alpha = 0.12f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(StatusSuccess),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isBangla) "আজকের সকল রুটিন সম্পন্ন হয়েছে!" else "All Routines Completed For Today!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusSuccess
                                )
                                Text(
                                    text = if (isBangla) "দারুণ নিয়ম মেনে চলার জন্য ধন্যবাদ।" else "Great consistency with your medicines and meals.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else if (state.todayItems.isEmpty()) {
                item {
                    EmptyTodayRoutineCard(
                        isBangla = isBangla,
                        onAddMedicine = { onNavigate(Screen.AddMedicine.route) },
                        onAddFood = { onNavigate(Screen.FoodSchedule.route) }
                    )
                }
            } else {
                items(pendingItems, key = { it.id }) { item ->
                    ReminderItemCard(
                        category = item.category,
                        title = item.title,
                        dosageOrDetails = item.subtitle,
                        time = item.time,
                        status = item.status,
                        language = state.preferences.language,
                        onCardClick = {
                            if (item.category == ReminderCategory.MEDICINE) {
                                onNavigate(Screen.MedicineDetail.createRoute(item.referenceId))
                            } else {
                                onNavigate(Screen.EditFood.createRoute(item.referenceId))
                            }
                        },
                        onMarkTaken = { viewModel.markItemTaken(item) },
                        onMarkSkipped = { viewModel.markItemSkipped(item) }
                    )
                }
            }

            // Completed Items Section (Collapsible/Banish destination)
            if (completedItems.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBangla) "আজকের সম্পন্ন হওয়া রুটিন (${DateTimeUtils.toBengaliDigits(completedItems.size.toString())})"
                        else "Completed Today (${completedItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(completedItems, key = { it.id }) { item ->
                    ReminderItemCard(
                        category = item.category,
                        title = item.title,
                        dosageOrDetails = item.subtitle,
                        time = item.time,
                        status = item.status,
                        language = state.preferences.language,
                        onCardClick = {
                            if (item.category == ReminderCategory.MEDICINE) {
                                onNavigate(Screen.MedicineDetail.createRoute(item.referenceId))
                            } else {
                                onNavigate(Screen.EditFood.createRoute(item.referenceId))
                            }
                        },
                        onMarkTaken = { viewModel.markItemTaken(item) },
                        onMarkSkipped = { viewModel.markItemSkipped(item) }
                    )
                }
            }

            // Bottom breathing space
            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun HeaderGreetingCard(
    userName: String,
    todayIso: String,
    progress: Float,
    isBangla: Boolean,
    onReportsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBangla) "শুভ সকাল, $userName" else "Hello, $userName",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = DateTimeUtils.formatReadableDate(todayIso, if (isBangla) "bn" else "en"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isBangla) "আজকের অগ্রগতি: ${(progress * 100).toInt()}%" else "Daily progress: ${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.95f),
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Circular progress ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clickable(onClick = onReportsClick)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White.copy(alpha = 0.25f),
                    strokeWidth = 6.dp
                )
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White,
                    strokeWidth = 6.dp
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WaterTrackerCard(
    currentMl: Int,
    goalMl: Int,
    isBangla: Boolean,
    onAddWater: (Int) -> Unit,
    onOpenWater: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenWater),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = SoftBlueSecondaryLight.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SoftBlueSecondaryLight.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = SoftBlueSecondaryLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBangla) "পানি পানের ট্র্যাকার" else "Water Hydration Tracker",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = if (isBangla) {
                        "${DateTimeUtils.toBengaliDigits(currentMl.toString())} / ${DateTimeUtils.toBengaliDigits(goalMl.toString())} মিলি"
                    } else {
                        "$currentMl / $goalMl ml"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = SoftBlueSecondaryLight
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val fraction = if (goalMl > 0) (currentMl.toFloat() / goalMl.toFloat()).coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = SoftBlueSecondaryLight,
                trackColor = SoftBlueSecondaryLight.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onAddWater(250) },
                    modifier = Modifier.testTag("btn_water_250"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isBangla) "+ ২৫০ মিলি" else "+ 250 ml", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onAddWater(500) },
                    modifier = Modifier.testTag("btn_water_500"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftBlueSecondaryLight)
                ) {
                    Text(if (isBangla) "+ ৫০০ মিলি" else "+ 500 ml", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun BatteryReliabilityNotice(
    isBangla: Boolean,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBangla) "অ্যালার্ম নির্ভরযোগ্যতা নির্দেশনা" else "Alarm Reliability Notice",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isBangla) {
                        "ডিভাইসের ব্যাটারি সেভার যাতে অ্যালার্ম বন্ধ না করে, সেজন্য অ্যাপের ব্যাটারি অপটিমাইজেশন 'Unrestricted' রাখুন। প্রস্তুতকারক বিধিনিষেধের কারণে শতভাগ নিশ্চয়তা দেওয়া অসম্ভব।"
                    } else {
                        "To ensure alarms ring on time, please disable battery optimization for this app in Settings. Delivery cannot be 100% guaranteed when Android battery saver is aggressive."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Text(text = "✕", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun EmptyTodayRoutineCard(
    isBangla: Boolean,
    onAddMedicine: () -> Unit,
    onAddFood: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (isBangla) "আজকের কোনো শিডিউল নেই" else "No routines scheduled for today",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isBangla) {
                    "আপনার ঔষধের সময় ও খাদ্যের তালিকা যুক্ত করুন যাতে নিয়মিত নোটিফিকেশন পান।"
                } else {
                    "Add your prescribed medicines and daily meal schedule to receive timely notifications."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onAddMedicine,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isBangla) "ঔষধ যোগ করুন" else "Add Medicine")
                }
                OutlinedButton(
                    onClick = onAddFood,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isBangla) "খাবার যোগ করুন" else "Add Meal")
                }
            }
        }
    }
}
