package com.hisabbook.app.ui.screens.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.hisabbook.app.security.AppLockManager
import com.hisabbook.app.security.AuthCapability

@Composable
fun AppLockScreen(
    lockManager: AppLockManager,
    onUnlocked: () -> Unit
) {
    val ctx = LocalContext.current
    val activity = ctx as? FragmentActivity
    var error by remember { mutableStateOf<String?>(null) }
    var triedOnce by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val cap = lockManager.canAuthenticate()
        when (cap) {
            AuthCapability.Available -> if (activity != null && !triedOnce) {
                triedOnce = true
                lockManager.prompt(
                    activity = activity,
                    onSuccess = onUnlocked,
                    onError = { error = it },
                    onCancel = { error = "Phone lock kholne ke liye tap karein" }
                )
            }
            AuthCapability.NotEnrolled,
            AuthCapability.NoHardware,
            AuthCapability.Unavailable -> {
                // No lock configured — skip gate. Privacy best-effort only.
                onUnlocked()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(56.dp)
            )
        }
        Spacer(Modifier.height(32.dp))
        Text(
            "HisabBook band hai",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Phone ka lock kholkar aage badhein",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (error != null) {
            Spacer(Modifier.height(16.dp))
            Text(
                error!!,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                activity?.let {
                    lockManager.prompt(
                        activity = it,
                        onSuccess = onUnlocked,
                        onError = { msg -> error = msg },
                        onCancel = { error = "Phir se koshish karein" }
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Icon(Icons.Default.Fingerprint, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Kholein", style = MaterialTheme.typography.titleLarge)
        }
    }
}
