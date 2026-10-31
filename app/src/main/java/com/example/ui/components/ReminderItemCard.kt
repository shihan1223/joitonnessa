package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ReminderCategory
import com.example.data.model.ReminderStatus
import com.example.ui.theme.SoftBlueSecondaryLight
import com.example.ui.theme.StatusMissed
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TealPrimaryLight
import com.example.util.DateTimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReminderItemCard(
    category: ReminderCategory,
    title: String,
    dosageOrDetails: String,
    time: String,
    status: ReminderStatus,
    language: String,
    onCardClick: () -> Unit,
    onMarkTaken: () -> Unit,
    onMarkSkipped: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryIcon = when (category) {
        ReminderCategory.MEDICINE -> Icons.Default.Medication
        ReminderCategory.FOOD -> Icons.Default.Restaurant
        ReminderCategory.WATER -> Icons.Default.WaterDrop
    }

    val iconTint = when (category) {
        ReminderCategory.MEDICINE -> TealPrimaryLight
        ReminderCategory.FOOD -> Color(0xFFD97706)
        ReminderCategory.WATER -> SoftBlueSecondaryLight
    }

    val statusColor = when (status) {
        ReminderStatus.TAKEN -> StatusSuccess
        ReminderStatus.SKIPPED -> Color.Gray
        ReminderStatus.MISSED -> StatusMissed
        ReminderStatus.PENDING -> StatusPending
    }

    val statusText = if (language == "bn") status.labelBn else status.labelEn

    val scope = rememberCoroutineScope()
    var isConfirming by remember { mutableStateOf(false) }
    var isBanished by remember { mutableStateOf(false) }

    val checkScale by animateFloatAsState(
        targetValue = if (isConfirming) 1.35f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "check_scale"
    )

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
        modifier = modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onCardClick)
                .testTag("reminder_card_${title.replace(' ', '_')}"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isConfirming) StatusSuccess.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon with circular backdrop
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isConfirming) StatusSuccess.copy(alpha = 0.2f) else iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isConfirming) Icons.Default.Check else categoryIcon,
                        contentDescription = null,
                        tint = if (isConfirming) StatusSuccess else iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Main details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = DateTimeUtils.format12Hour(time, language),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (dosageOrDetails.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dosageOrDetails,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Status pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isConfirming) StatusSuccess.copy(alpha = 0.2f) else statusColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (isConfirming) (if (language == "bn") "সম্পন্ন হচ্ছে..." else "Confirmed!") else statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isConfirming) StatusSuccess else statusColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Action buttons if pending
                if (status == ReminderStatus.PENDING && !isConfirming) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Confirm Right Mark Button
                        IconButton(
                            onClick = {
                                isConfirming = true
                                scope.launch {
                                    delay(300L) // Satisfying confirmation bounce
                                    isBanished = true // Trigger shrink, slide-out, and banish animation
                                    delay(380L) // Allow animation to finish
                                    onMarkTaken() // Update in Room DB
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .scale(checkScale)
                                .background(StatusSuccess.copy(alpha = 0.18f))
                                .testTag("action_taken_$title")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Confirm (Mark Taken)",
                                tint = StatusSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Skip Button
                        IconButton(
                            onClick = {
                                scope.launch {
                                    isBanished = true
                                    delay(350L)
                                    onMarkSkipped()
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("action_skip_$title")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Skip Dose",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
