package com.example.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.MetricCard
import com.example.ui.theme.StatusMissed
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TealPrimaryLight
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: HistoryViewModel,
    language: String,
    onNavigateBack: () -> Unit
) {
    val isBangla = language == "bn"
    val reportsFlow = remember(language) { viewModel.getWeeklyReports(language) }
    val reportsData by reportsFlow.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isBangla) "রিপোর্ট ও বিশ্লেষণ" else "Reports & Analytics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_reports_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .testTag("reports_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Adherence Ring Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(100.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            strokeWidth = 10.dp
                        )
                        CircularProgressIndicator(
                            progress = { (reportsData.overallAdherence / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 10.dp
                        )
                        Text(
                            text = "${reportsData.overallAdherence}%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isBangla) "সাপ্তাহিক ঔষধ ও খাবার খাওয়ার নিয়মবদ্ধতা" else "Weekly Routine Adherence",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (reportsData.overallAdherence >= 80) {
                            if (isBangla) "দারুণ নিয়ম মেনে চলছেন! সুস্থতার জন্য এটি বজায় রাখুন।" else "Excellent consistency! Keep up your healthy habit."
                        } else {
                            if (isBangla) "নিয়মিত নোটিফিকেশন শুনে ঔষধ গ্রহণ করার চেষ্টা করুন।" else "Aim to take doses on time when notifications ring."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Weekly Bar Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isBangla) "গত ৭ দিনের পারফরম্যান্স" else "Past 7 Days Performance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val primaryColor = MaterialTheme.colorScheme.primary
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant

                    // Weekly Bar Chart using Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val count = reportsData.weeklyReports.size.coerceAtLeast(1)
                            val barWidth = size.width / (count * 2f)
                            val step = size.width / count

                            reportsData.weeklyReports.forEachIndexed { index, day ->
                                val x = step * index + (step - barWidth) / 2f
                                val percent = (day.adherencePercent / 100f).coerceIn(0f, 1f)
                                val barHeight = size.height * percent

                                // Background track bar
                                drawRoundRect(
                                    color = trackColor,
                                    topLeft = Offset(x, 0f),
                                    size = Size(barWidth, size.height),
                                    cornerRadius = CornerRadius(8f, 8f)
                                )

                                // Filled active bar
                                if (barHeight > 0f) {
                                    drawRoundRect(
                                        color = primaryColor,
                                        topLeft = Offset(x, size.height - barHeight),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = CornerRadius(8f, 8f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Labels under bars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        reportsData.weeklyReports.forEach { day ->
                            Text(
                                text = day.dayLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Summary Totals
            Text(
                text = if (isBangla) "মোট পরিসংখ্যান" else "Total Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = if (isBangla) "গ্রহণ করা হয়েছে" else "Total Taken",
                    value = if (isBangla) DateTimeUtils.toBengaliDigits(reportsData.totalTaken.toString()) else reportsData.totalTaken.toString(),
                    subtitle = if (isBangla) "সফল ডোজ" else "Completed",
                    icon = Icons.Default.CheckCircle,
                    iconTint = StatusSuccess,
                    containerColor = StatusSuccess.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = if (isBangla) "মিস বা বাদ" else "Skipped/Missed",
                    value = if (isBangla) DateTimeUtils.toBengaliDigits((reportsData.totalSkipped + reportsData.totalMissed).toString()) else (reportsData.totalSkipped + reportsData.totalMissed).toString(),
                    subtitle = if (isBangla) "অনুপস্থিত" else "Unrecorded",
                    icon = Icons.Default.Warning,
                    iconTint = StatusMissed,
                    containerColor = StatusMissed.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
