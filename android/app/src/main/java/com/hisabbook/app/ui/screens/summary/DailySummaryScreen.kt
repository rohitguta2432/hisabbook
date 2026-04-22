package com.hisabbook.app.ui.screens.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import com.hisabbook.app.R
import com.hisabbook.app.ui.components.IntentHelpers
import com.hisabbook.app.ui.components.OfflineBadge
import com.hisabbook.app.ui.theme.HisabBookTheme
import com.hisabbook.app.ui.theme.PrimaryFixed
import com.hisabbook.app.ui.theme.StatusNegativeBg

@Composable
fun DailySummaryScreen(
    kulBikriPaise: Long = 12_00_000L,
    kulKharchPaise: Long = 2_50_000L,
    udharDiyaPaise: Long = 80_000L,
    jamaPaise: Long = 1_50_000L,
    munafaPaise: Long = 9_50_000L,
    onShare: (() -> Unit)? = null,
    bottomBar: @Composable () -> Unit
) {
    val ctx = LocalContext.current
    val doShare: () -> Unit = onShare ?: {
        val msg = buildString {
            append("Aaj ka Hisab — HisabBook\n\n")
            append("Kul Bikri: ₹${"%,d".format(kulBikriPaise / 100)}\n")
            append("Kul Kharch: ₹${"%,d".format(kulKharchPaise / 100)}\n")
            append("Udhar Diya: ₹${"%,d".format(udharDiyaPaise / 100)}\n")
            append("Jama Hua: ₹${"%,d".format(jamaPaise / 100)}\n")
            append("Aaj ka Munafa: ₹${"%,d".format(munafaPaise / 100)}")
        }
        IntentHelpers.share(ctx, msg)
    }
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
                        stringResource(R.string.summary_title),
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BigCard(
                label = stringResource(R.string.summary_kul_bikri),
                value = formatRupees(kulBikriPaise)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    label = stringResource(R.string.summary_kul_kharch),
                    value = formatRupees(kulKharchPaise),
                    bg = StatusNegativeBg,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    label = stringResource(R.string.summary_udhar_diya),
                    value = formatRupees(udharDiyaPaise),
                    bg = MaterialTheme.colorScheme.surfaceContainerLowest,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    label = stringResource(R.string.summary_jama),
                    value = formatRupees(jamaPaise),
                    bg = MaterialTheme.colorScheme.surfaceContainerLowest,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    label = stringResource(R.string.summary_munafa),
                    value = formatRupees(munafaPaise),
                    bg = PrimaryFixed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = doShare,
                shape = RoundedCornerShape(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.size(12.dp))
                Text(stringResource(R.string.summary_share), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BigCard(label: String, value: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Text(
                value,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, bg: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        modifier = modifier
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        }
    }
}

private fun formatRupees(paise: Long): String = "₹" + "%,d".format(paise / 100)

@Preview(showBackground = true, heightDp = 850)
@Composable
private fun SummaryPreview() {
    HisabBookTheme { DailySummaryScreen(bottomBar = {}) }
}
