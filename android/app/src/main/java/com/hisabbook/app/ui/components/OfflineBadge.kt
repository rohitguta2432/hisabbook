package com.hisabbook.app.ui.components

import android.content.Context
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hisabbook.app.R
import com.hisabbook.app.ui.theme.OfflineBadgeBg
import com.hisabbook.app.ui.theme.StorageFullBadgeBg
import com.hisabbook.app.ui.theme.StorageFullBadgeText

private const val LOW_STORAGE_THRESHOLD = 100L * 1024 * 1024 // 100 MB

@Composable
fun OfflineBadge(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val lowStorage = remember(ctx) { isLowStorage(ctx) }

    if (lowStorage) StorageFullBadge(modifier) else OfflineOkBadge(modifier)
}

@Composable
private fun OfflineOkBadge(modifier: Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(OfflineBadgeBg)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = androidx.compose.ui.res.stringResource(R.string.home_offline),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun StorageFullBadge(modifier: Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(StorageFullBadgeBg)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = StorageFullBadgeText
        )
        Text(
            text = "Phone full",
            style = MaterialTheme.typography.labelLarge,
            color = StorageFullBadgeText
        )
    }
}

private fun isLowStorage(ctx: Context): Boolean = runCatching {
    Environment.getDataDirectory().freeSpace < LOW_STORAGE_THRESHOLD
}.getOrDefault(false)
