package com.hisabbook.app.ui.screens.khata

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SwipeLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.hisabbook.app.R
import com.hisabbook.app.data.model.Entry
import com.hisabbook.app.data.model.EntryType
import com.hisabbook.app.data.model.Person
import com.hisabbook.app.data.model.PersonType
import com.hisabbook.app.ui.components.EntryEditSheet
import com.hisabbook.app.ui.components.IntentHelpers
import com.hisabbook.app.ui.components.OfflineBadge
import com.hisabbook.app.ui.theme.StatusNegativeText
import com.hisabbook.app.ui.theme.StatusPositive
import com.hisabbook.app.util.toRupeesString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerKhataScreen(
    personId: String = "p1",
    onBack: () -> Unit,
    onNewEntry: () -> Unit,
    bottomBar: @Composable () -> Unit,
    vm: CustomerKhataViewModel = hiltViewModel()
) {
    LaunchedEffect(personId) { vm.setPersonId(personId) }
    val person by vm.person.collectAsState()
    val entries by vm.entries.collectAsState()
    var editing by remember { mutableStateOf<Entry?>(null) }
    val ctx = LocalContext.current
    val personSafe = person ?: Person(id = personId, name = "—", phone = null, type = PersonType.CUSTOMER, balancePaise = 0L)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    title = {
                        Text(
                            "${personSafe.name} ka Khata",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    },
                    actions = { OfflineBadge(Modifier.padding(end = 16.dp)) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
        bottomBar = bottomBar,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewEntry,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(40.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.khata_new_entry), style = MaterialTheme.typography.titleLarge) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            PersonHeaderCard(
                person = personSafe,
                onCall = { personSafe.phone?.let { IntentHelpers.dial(ctx, it) } },
                onRemind = {
                    personSafe.phone?.let {
                        val msg = "Namaste ${personSafe.name} ji. Aapka ${personSafe.balancePaise.toRupeesString()} baki hai. — HisabBook"
                        IntentHelpers.whatsappText(ctx, it, msg)
                    }
                }
            )
            Spacer(Modifier.height(16.dp))
            EntryTableHeader()
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(entries, key = { it.id }) { entry ->
                    SwipeableEntryRow(entry = entry, onSettle = { editing = it.copy() }, onLongPress = { editing = it })
                }
                item { SwipeHint() }
            }
        }
    }

    editing?.let { e ->
        EntryEditSheet(
            entry = e,
            onDismiss = { editing = null },
            onEdit = { editing = null },
            onDelete = { vm.deleteEntry(it); editing = null },
            onSettle = { vm.settle(it); editing = null }
        )
    }
}

@Composable
private fun PersonHeaderCard(person: Person, onCall: () -> Unit, onRemind: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(person.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    person.phone?.let {
                        Text("Phone: $it", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.khata_kul_baki), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        person.balancePaise.toRupeesString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (person.balancePaise == 0L) StatusPositive else StatusNegativeText,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onCall,
                    enabled = person.phone != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.khata_call), style = MaterialTheme.typography.titleLarge)
                }
                Button(
                    onClick = onRemind,
                    enabled = person.phone != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.khata_remind), style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@Composable
private fun EntryTableHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(stringResource(R.string.khata_col_date), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.2f))
        Text(stringResource(R.string.khata_col_kaam), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.5f))
        Text(stringResource(R.string.khata_col_rakam), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.3f))
        Text(stringResource(R.string.khata_col_baki), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.2f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableEntryRow(
    entry: Entry,
    onSettle: (Entry) -> Unit,
    onLongPress: (Entry) -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { target ->
            if (target == SwipeToDismissBoxValue.EndToStart) {
                onSettle(entry)
                false
            } else false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(StatusPositive, RoundedCornerShape(16.dp))
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Done, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.entry_settle), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    ) {
        EntryRowCard(entry = entry, onLongPress = onLongPress)
    }
}

@Composable
private fun EntryRowCard(entry: Entry, onLongPress: (Entry) -> Unit) {
    val isWapas = entry.type == EntryType.UDHAR_WAPAS || entry.type == EntryType.UDHAR_CHUKAYA
    val signColor = if (isWapas) StatusPositive else StatusNegativeText
    val sign = if (isWapas) "-" else "+"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val date = SimpleDateFormat("d MMM", Locale("en", "IN")).format(Date(entry.createdAt))
            Text(date, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1.2f))
            Text(entry.item, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1.5f))
            Text(
                "$sign ₹${"%,d".format(entry.amountPaise / 100)}",
                style = MaterialTheme.typography.bodyMedium,
                color = signColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1.3f)
            )
            Text(
                entry.amountPaise.toRupeesString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1.2f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
}

@Composable
private fun SwipeHint() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.SwipeLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.size(8.dp))
        Text(stringResource(R.string.khata_swipe_hint), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
