package com.example.ui.schedule

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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ReminderCategory
import com.example.data.model.ReminderStatus
import com.example.ui.components.ReminderItemCard
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.dashboard.TodayScheduleItem
import com.example.ui.navigation.Screen
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyScheduleScreen(
    dashboardViewModel: DashboardViewModel,
    language: String,
    onNavigate: (String) -> Unit
) {
    val state by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val isBangla = language == "bn"

    var selectedCategoryFilter by remember { mutableStateOf<ReminderCategory?>(null) }
    var selectedDayOfWeek by remember { mutableIntStateOf(DateTimeUtils.currentDayOfWeek()) }

    val dayNamesEn = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val dayNamesBn = listOf("সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র", "শনি", "রবি")

    // Filter items based on selected day of week and category
    val filteredItems = remember(state.medicines, state.foodReminders, selectedDayOfWeek, selectedCategoryFilter) {
        val items = mutableListOf<TodayScheduleItem>()

        if (selectedCategoryFilter == null || selectedCategoryFilter == ReminderCategory.MEDICINE) {
            state.medicines.forEach { med ->
                if (med.daysOfWeek.contains(selectedDayOfWeek)) {
                    med.reminderTimes.forEach { time ->
                        items.add(
                            TodayScheduleItem(
                                id = med.id * 1000 + time.hashCode(),
                                category = ReminderCategory.MEDICINE,
                                title = med.name,
                                subtitle = "${med.dosage} • ${if (isBangla) med.foodRelation.labelBn else med.foodRelation.labelEn}",
                                time = time,
                                status = ReminderStatus.PENDING,
                                daysOfWeek = med.daysOfWeek,
                                referenceId = med.id
                            )
                        )
                    }
                }
            }
        }

        if (selectedCategoryFilter == null || selectedCategoryFilter == ReminderCategory.FOOD) {
            state.foodReminders.forEach { meal ->
                if (meal.daysOfWeek.contains(selectedDayOfWeek)) {
                    items.add(
                        TodayScheduleItem(
                            id = 50000 + meal.id,
                            category = ReminderCategory.FOOD,
                            title = meal.displayName,
                            subtitle = if (meal.notes.isNotBlank()) meal.notes else "Scheduled meal",
                            time = meal.time,
                            status = ReminderStatus.PENDING,
                            daysOfWeek = meal.daysOfWeek,
                            referenceId = meal.id
                        )
                    )
                }
            }
        }

        items.sortBy { it.time }
        items
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBangla) "দৈনিক পূর্ণাঙ্গ রুটিন" else "Daily Schedule & Routines",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate(Screen.AddMedicine.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Routine")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("schedule_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Day Selector Horizontal Bar (1..7)
            item {
                Text(
                    text = if (isBangla) "বার নির্বাচন করুন" else "Select Day of Week",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(7) { index ->
                        val dayNum = index + 1
                        val isSelected = selectedDayOfWeek == dayNum
                        val isToday = DateTimeUtils.currentDayOfWeek() == dayNum

                        Card(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedDayOfWeek = dayNum },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isBangla) dayNamesBn[index] else dayNamesEn[index],
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                if (isToday) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color.White else MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Category Filter Chips (All, Medicine, Food)
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text(if (isBangla) "সব রুটিন" else "All Routines") }
                    )
                    FilterChip(
                        selected = selectedCategoryFilter == ReminderCategory.MEDICINE,
                        onClick = { selectedCategoryFilter = ReminderCategory.MEDICINE },
                        label = { Text(if (isBangla) "💊 ঔষধ" else "💊 Medicines") }
                    )
                    FilterChip(
                        selected = selectedCategoryFilter == ReminderCategory.FOOD,
                        onClick = { selectedCategoryFilter = ReminderCategory.FOOD },
                        label = { Text(if (isBangla) "🍽️ ১০ বার খাবার" else "🍽️ 10 Meals") }
                    )
                }
            }

            // Food Schedule Quick Access Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(Screen.FoodSchedule.route) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBangla) "১০ বার খাবারের সময়সূচি পরিচালনা" else "Manage 10-Meal Schedule",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isBangla) "শুরুর সময় (০৭:০০) ও ২ ঘণ্টার ব্যবধান পরিবর্তন করুন" else "07:00 AM start, every 2h, 10 reminders crossing midnight",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Items list or empty state
            if (filteredItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isBangla) "এই দিনের জন্য কোনো রুটিন নির্ধারিত নেই" else "No routines scheduled for this day",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isBangla) "নতুন ঔষধ অথবা খাবারের সময় যুক্ত করুন।" else "Add a medicine or meal reminder for this day.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredItems, key = { it.id }) { item ->
                    ReminderItemCard(
                        category = item.category,
                        title = item.title,
                        dosageOrDetails = item.subtitle,
                        time = item.time,
                        status = item.status,
                        language = language,
                        onCardClick = {
                            if (item.category == ReminderCategory.MEDICINE) {
                                onNavigate(Screen.MedicineDetail.createRoute(item.referenceId))
                            } else {
                                onNavigate(Screen.EditFood.createRoute(item.referenceId))
                            }
                        },
                        onMarkTaken = { dashboardViewModel.markItemTaken(item) },
                        onMarkSkipped = { dashboardViewModel.markItemSkipped(item) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(36.dp)) }
        }
    }
}
