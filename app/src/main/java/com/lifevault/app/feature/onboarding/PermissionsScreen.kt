package com.lifevault.app.feature.onboarding

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.lifevault.app.core.designsystem.icon.LifeVaultIcons
import com.lifevault.app.core.designsystem.spacing.Spacing
import com.lifevault.app.core.util.isAggressiveBatteryOem

/** S03 (Section 3.3): notification permission + OEM reliability card. Neither is blocking. */
@Composable
fun PermissionsScreen(onContinue: () -> Unit, onNotNow: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(Spacing.lg)) {
        Text(text = "Stay ahead of deadlines", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.size(Spacing.xxl))

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                NotificationPermissionCard()
            }
            if (isAggressiveBatteryOem()) {
                ReliabilityCard()
            }
        }

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Text("Continue")
        }
        TextButton(onClick = onNotNow, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Not now")
        }
    }
}

// Only ever called under the Build.VERSION.SDK_INT >= TIRAMISU guard in PermissionsScreen;
// the POST_NOTIFICATIONS field itself is just a compile-time string constant, safe to
// reference on any API level.
@SuppressLint("InlinedApi")
@Composable
private fun NotificationPermissionCard() {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        granted = isGranted
    }

    PermissionCard(
        icon = LifeVaultIcons.Core.Reminders,
        title = "Notifications",
        reason = "So we can remind you before documents expire",
        granted = granted,
        onAllow = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) },
    )
}

@Composable
private fun ReliabilityCard() {
    var acknowledged by remember { mutableStateOf(false) }
    PermissionCard(
        icon = LifeVaultIcons.Core.Settings,
        title = "Keep reminders reliable",
        reason = "Your phone's battery saver may delay reminders — a quick setting fixes this",
        granted = acknowledged,
        // TODO(step 17 - reminder engine): open the full OEM-specific guidance (Section 7.6)
        // instead of just acknowledging the card.
        onAllow = { acknowledged = true },
    )
}

@Composable
private fun PermissionCard(
    icon: Int,
    title: String,
    reason: String,
    granted: Boolean,
    onAllow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(16.dp))
            .padding(Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.size(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.size(Spacing.md))
        if (granted) {
            Icon(
                painter = painterResource(LifeVaultIcons.Core.Valid),
                contentDescription = "Granted",
                tint = MaterialTheme.colorScheme.tertiary,
            )
        } else {
            TextButton(onClick = onAllow) {
                Text("Allow")
            }
        }
    }
}
