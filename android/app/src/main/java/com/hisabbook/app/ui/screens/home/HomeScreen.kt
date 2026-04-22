package com.hisabbook.app.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.hisabbook.app.R
import com.hisabbook.app.ui.components.OfflineBadge
import com.hisabbook.app.ui.theme.HisabBookTheme
import com.hisabbook.app.ui.theme.Secondary
import com.hisabbook.app.ui.theme.StatusNegativeText
import com.hisabbook.app.util.formatShort
import com.hisabbook.app.util.toRupeesString

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onBolo: () -> Unit,
    bottomBar: @Composable () -> Unit,
    vm: HomeViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val bikriPaise = state.totals.bikriPaise
    val kharchPaise = state.totals.kharchPaise
    val bakiUdharPaise = state.bakiUdharPaise
    val munafaPaise = state.totals.munafaPaise
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
                        stringResource(R.string.home_title),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = MaterialTheme.typography.headlineMedium.fontSize),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.weight(1f))
                    OfflineBadge()
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
        bottomBar = bottomBar,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onBolo,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(40.dp),
                icon = { Icon(Icons.Default.Mic, contentDescription = null) },
                text = {
                    Text(
                        stringResource(R.string.fab_bolo),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                modifier = Modifier.height(72.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            StatsGrid(
                bikriPaise = bikriPaise,
                kharchPaise = kharchPaise,
                bakiUdharPaise = bakiUdharPaise,
                munafaPaise = munafaPaise
            )
            Spacer(Modifier.height(32.dp))
            EmptyState(modifier = Modifier.weight(1f))
            Spacer(Modifier.height(16.dp))
            QuickActionChips()
            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun shortHint(paise: Long): String? {
    val rupees = kotlin.math.abs(paise) / 100
    return if (rupees >= 1_000L) formatShort(rupees) else null
}

@Composable
private fun StatsGrid(
    bikriPaise: Long,
    kharchPaise: Long,
    bakiUdharPaise: Long,
    munafaPaise: Long
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label = stringResource(R.string.card_aaj_bikri),
                value = bikriPaise.toRupeesString(),
                shortText = shortHint(bikriPaise),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.card_aaj_kharch),
                value = kharchPaise.toRupeesString(),
                shortText = shortHint(kharchPaise),
                valueColor = StatusNegativeText,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label = stringResource(R.string.card_baki_udhar),
                value = bakiUdharPaise.toRupeesString(),
                shortText = shortHint(bakiUdharPaise),
                valueColor = Secondary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.card_munafa),
                value = munafaPaise.toRupeesString(),
                shortText = shortHint(munafaPaise),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    shortText: String? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Text(
                value,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = MaterialTheme.typography.headlineMedium.fontSize),
                color = valueColor,
                fontWeight = FontWeight.ExtraBold
            )
            if (!shortText.isNullOrBlank()) {
                Text(
                    shortText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.empty_home),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuickActionChips() {
    val chips = listOf(
        R.string.chip_bikri_jodo,
        R.string.chip_kharch_jodo,
        R.string.chip_udhar_diya,
        R.string.chip_udhar_wapas
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.width(0.dp))
        chips.forEach { res ->
            FilterChip(
                selected = false,
                onClick = { },
                label = {
                    Text(
                        stringResource(res),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.height(48.dp)
            )
        }
    }
}

