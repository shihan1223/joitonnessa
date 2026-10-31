package com.example.ui.settings

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyAboutScreen(
    language: String,
    onNavigateBack: () -> Unit
) {
    val isBangla = language == "bn"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isBangla) "গোপনীয়তা ও পরিচিতি" else "Privacy & About") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_about_back")) {
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "joitonnessa – ঔষধ ও খাদ্য",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Version 1.0 (Production Ready)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Text(
                text = if (isBangla) "আমাদের গোপনীয়তা নীতি" else "Privacy Commitments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            PrivacyBullet(
                icon = Icons.Default.OfflinePin,
                title = if (isBangla) "সম্পূর্ণ অফলাইন ডাটা স্টোরেজ" else "Local Offline-First Storage",
                description = if (isBangla) {
                    "আপনার ঔষধের তালিকা, খাবারের শিডিউল ও পানি পানের রেকর্ড সম্পূর্ণ আপনার ডিভাইসে সংরক্ষিত থাকে। কোনো রিমোট সার্ভারে পাঠানো হয় না।"
                } else {
                    "All your medicine schedules, meal times, and hydration logs are stored securely on your local device."
                }
            )

            PrivacyBullet(
                icon = Icons.Default.Lock,
                title = if (isBangla) "ডাক্তারের পরামর্শের সুরক্ষা" else "Doctor's Prescription Safety",
                description = if (isBangla) {
                    "এই অ্যাপ কোনো ঔষধ সুপারিশ করে না অথবা স্বয়ংক্রিয়ভাবে ডোজ পরিবর্তন করে না। শুধুমাত্র আপনার নির্ধারিত সময় অনুযায়ী রিমাইন্ডার দেয়।"
                } else {
                    "This app never recommends or alters medical dosages. It functions strictly as your scheduled reminder assistant."
                }
            )

            PrivacyBullet(
                icon = Icons.Default.VerifiedUser,
                title = if (isBangla) "লকস্ক্রিন নিরাপত্তা" else "Lock Screen Privacy",
                description = if (isBangla) {
                    "ব্যবহারকারীর পূর্ণ নিয়ন্ত্রণ রয়েছে লকস্ক্রিনে ঔষধের বিস্তারিত প্রদর্শনের ক্ষেত্রে।"
                } else {
                    "Medicine names and instructions are masked or displayed based on your privacy preference."
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PrivacyBullet(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
