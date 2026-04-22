package com.hisabbook.app.ui.components

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/** Silently requests POST_NOTIFICATIONS once on Android 13+. Best-effort. */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestNotificationPermissionOnce() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val state = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    LaunchedEffect(state) {
        if (!state.status.isGranted) state.launchPermissionRequest()
    }
}
