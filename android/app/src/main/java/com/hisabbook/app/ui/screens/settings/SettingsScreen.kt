package com.hisabbook.app.ui.screens.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabbook.app.R
import com.hisabbook.app.data.prefs.AppPreferences
import com.hisabbook.app.ui.components.OfflineBadge
import com.hisabbook.app.ui.theme.HisabBookTheme
import com.hisabbook.app.ui.theme.SecondaryContainer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    bottomBar: @Composable () -> Unit,
    onExportBackup: () -> Unit = {},
    onImportBackup: () -> Unit = {},
    onOpenLanguage: () -> Unit = {},
    vm: SettingsViewModel = hiltViewModel()
) {
    val darkModePref by vm.darkMode.collectAsState(initial = null)
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val darkMode = darkModePref ?: systemDark
    val lockEnabled by vm.lockEnabled.collectAsState(initial = true)
    val voiceStatus by vm.voiceStatus.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.weight(1f))
                    OfflineBadge()
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
        bottomBar = bottomBar
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsGroup {
                SettingsItem(
                    icon = Icons.Default.Store,
                    iconBg = MaterialTheme.colorScheme.primary,
                    iconTint = MaterialTheme.colorScheme.onPrimary,
                    title = stringResource(R.string.settings_dukaan_naam),
                    subtitle = stringResource(R.string.settings_dukaan_default),
                    trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
                Row(modifier = Modifier.clickable(onClick = onOpenLanguage)) {
                    SettingsItem(
                        icon = Icons.Default.Language,
                        iconBg = MaterialTheme.colorScheme.primary,
                        iconTint = MaterialTheme.colorScheme.onPrimary,
                        title = stringResource(R.string.settings_bhasha),
                        subtitle = stringResource(R.string.settings_bhasha_sub),
                        trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    )
                }
            }

            SectionLabel(stringResource(R.string.settings_voice_ai))
            SettingsGroup {
                SettingsItem(
                    icon = Icons.Default.Speed,
                    iconBg = SecondaryContainer,
                    iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                    title = stringResource(R.string.settings_speed),
                    subtitle = stringResource(R.string.settings_speed_normal),
                    trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
                val subtitle = buildString {
                    append(voiceStatus.tier.name)
                    append(" · ")
                    append(if (voiceStatus.modelReady) "Taiyaar ✓"
                    else if (voiceStatus.assetPresent) "Install karne ke liye tap karo"
                    else "Model nahi mila")
                }
                Row(modifier = Modifier.clickable(
                    enabled = !voiceStatus.modelReady && voiceStatus.assetPresent,
                    onClick = { vm.installModel() }
                )) {
                    SettingsItem(
                        icon = Icons.Default.Mic,
                        iconBg = SecondaryContainer,
                        iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                        title = stringResource(R.string.settings_voice_ai_label),
                        subtitle = subtitle
                    )
                }
            }

            SectionLabel(stringResource(R.string.settings_system))
            SettingsGroup {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    iconBg = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    title = stringResource(R.string.settings_lock),
                    subtitle = stringResource(R.string.settings_lock_sub),
                    trailing = { Switch(checked = lockEnabled, onCheckedChange = { vm.setLock(it) }) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    iconBg = MaterialTheme.colorScheme.surfaceContainerHigh,
                    iconTint = MaterialTheme.colorScheme.onSurface,
                    title = stringResource(R.string.settings_dark_mode),
                    subtitle = null,
                    trailing = { Switch(checked = darkMode, onCheckedChange = { vm.setDark(it) }) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
                Row(modifier = Modifier.clickable(onClick = onExportBackup)) {
                    SettingsItem(
                        icon = Icons.Default.Save,
                        iconBg = MaterialTheme.colorScheme.surfaceContainerHigh,
                        iconTint = MaterialTheme.colorScheme.onSurface,
                        title = stringResource(R.string.settings_backup),
                        subtitle = stringResource(R.string.settings_backup_sub),
                        trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
                Row(modifier = Modifier.clickable(onClick = onImportBackup)) {
                    SettingsItem(
                        icon = Icons.Default.Save,
                        iconBg = MaterialTheme.colorScheme.surfaceContainerHigh,
                        iconTint = MaterialTheme.colorScheme.onSurface,
                        title = stringResource(R.string.settings_restore),
                        subtitle = stringResource(R.string.settings_restore_sub),
                        trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            Text(
                stringResource(R.string.settings_version),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String?,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) { Icon(icon, contentDescription = null, tint = iconTint) }
        Spacer(Modifier.size(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        trailing?.invoke()
    }
}

@Preview(showBackground = true, heightDp = 850)
@Composable
private fun SettingsPreview() {
    HisabBookTheme { SettingsScreen(bottomBar = {}) }
}
