package com.example.ui.alarm

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.JoitonnessaApplication
import com.example.data.model.ReminderCategory
import com.example.data.model.ReminderStatus
import com.example.reminder.NotificationHelper
import com.example.ui.theme.JoitonnessaTheme
import com.example.ui.theme.StatusSuccess
import com.example.util.DateTimeUtils
import com.example.util.SoundPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmRingingActivity : ComponentActivity() {

    private lateinit var soundPlayer: SoundPlayer
    private var notificationId: Int = 0
    private var category: ReminderCategory = ReminderCategory.MEDICINE
    private var refId: Long = 0L
    private var title: String = ""
    private var subtitle: String = ""
    private var time: String = ""
    private var dateIso: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Turn on screen and show when locked
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, 0)
        val categoryStr = intent.getStringExtra(NotificationHelper.EXTRA_CATEGORY) ?: ReminderCategory.MEDICINE.name
        category = try { ReminderCategory.valueOf(categoryStr) } catch (_: Exception) { ReminderCategory.MEDICINE }
        refId = intent.getLongExtra(NotificationHelper.EXTRA_REF_ID, 0L)
        title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "Reminder"
        subtitle = intent.getStringExtra(NotificationHelper.EXTRA_SUBTITLE) ?: ""
        time = intent.getStringExtra(NotificationHelper.EXTRA_TIME) ?: "08:00"
        dateIso = intent.getStringExtra(NotificationHelper.EXTRA_DATE_ISO) ?: DateTimeUtils.todayIso()

        soundPlayer = SoundPlayer(this)
        soundPlayer.startAlarmRinging(vibrate = true)

        setContent {
            JoitonnessaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AlarmRingingContent(
                        title = title,
                        subtitle = subtitle,
                        time = time,
                        category = category,
                        onTaken = { handleAction(ReminderStatus.TAKEN) },
                        onSnooze = { handleSnooze() },
                        onSkip = { handleAction(ReminderStatus.SKIPPED) }
                    )
                }
            }
        }
    }

    private fun handleAction(status: ReminderStatus) {
        soundPlayer.stop()
        val app = application as JoitonnessaApplication
        app.notificationHelper.cancelNotification(notificationId)

        CoroutineScope(Dispatchers.IO).launch {
            app.reminderLogRepository.recordOrUpdateLog(
                category = category,
                referenceId = refId,
                title = title,
                subtitle = subtitle,
                scheduledDateIso = dateIso,
                scheduledTime = time,
                scheduledTimestamp = System.currentTimeMillis(),
                status = status
            )
        }
        finish()
    }

    private fun handleSnooze() {
        soundPlayer.stop()
        val app = application as JoitonnessaApplication
        app.notificationHelper.cancelNotification(notificationId)
        app.alarmScheduler.scheduleSnooze(
            category = category,
            refId = refId,
            title = title,
            subtitle = subtitle,
            minutes = 10
        )
        finish()
    }

    override fun onDestroy() {
        soundPlayer.stop()
        super.onDestroy()
    }
}

@Composable
fun AlarmRingingContent(
    title: String,
    subtitle: String,
    time: String,
    category: ReminderCategory,
    onTaken: () -> Unit,
    onSnooze: () -> Unit,
    onSkip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Pulsing Alarm Icon Badge
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = "Alarm Ringing",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "joitonnessa Reminder",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = DateTimeUtils.format12Hour(time),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val categoryLabel = when (category) {
                        ReminderCategory.MEDICINE -> "💊 Medicine (ঔষধ)"
                        ReminderCategory.FOOD -> "🍽️ Food (খাদ্য)"
                        ReminderCategory.WATER -> "💧 Hydration (পানি)"
                    }
                    Text(
                        text = categoryLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (subtitle.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Action: Taken (Primary Green/Teal)
            Button(
                onClick = onTaken,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("alarm_taken_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Taken / Completed (খেয়েছি)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action: Snooze (Soft Blue)
            Button(
                onClick = onSnooze,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("alarm_snooze_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.Snooze, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Snooze 10 Minutes (১০ মি. পরে)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action: Skip (Outlined)
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("alarm_skip_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Skip Dose (বাদ দিন)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
