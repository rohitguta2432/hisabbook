package com.hisabbook.app.ui.screens.manual

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hisabbook.app.R
import com.hisabbook.app.data.model.EntryType

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryScreen(
    prefilledPersonName: String? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    vm: ManualEntryViewModel = hiltViewModel()
) {
    var type by remember { mutableStateOf(EntryType.UDHAR_DIYA) }
    var amount by remember { mutableStateOf("") }
    var person by remember { mutableStateOf(prefilledPersonName.orEmpty()) }
    var item by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                },
                title = {
                    Text(
                        stringResource(R.string.manual_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(Modifier.width(0.dp))
                TypeChip(R.string.manual_type_bikri, type == EntryType.BIKRI) { type = EntryType.BIKRI }
                TypeChip(R.string.manual_type_kharch, type == EntryType.KHARCH) { type = EntryType.KHARCH }
                TypeChip(R.string.manual_type_udhar_diya, type == EntryType.UDHAR_DIYA) { type = EntryType.UDHAR_DIYA }
                TypeChip(R.string.manual_type_udhar_wapas, type == EntryType.UDHAR_WAPAS) { type = EntryType.UDHAR_WAPAS }
                TypeChip(R.string.manual_type_udhar_liya, type == EntryType.UDHAR_LIYA) { type = EntryType.UDHAR_LIYA }
                TypeChip(R.string.manual_type_udhar_chukaya, type == EntryType.UDHAR_CHUKAYA) { type = EntryType.UDHAR_CHUKAYA }
            }
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() } },
                label = { Text(stringResource(R.string.manual_amount_hint)) },
                prefix = { Text("₹") },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            val personNeeded = type in setOf(EntryType.UDHAR_DIYA, EntryType.UDHAR_WAPAS, EntryType.UDHAR_LIYA, EntryType.UDHAR_CHUKAYA)
            OutlinedTextField(
                value = person,
                onValueChange = { person = it },
                label = { Text(stringResource(R.string.manual_person_hint)) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = item,
                onValueChange = { item = it },
                label = { Text(stringResource(R.string.manual_item_hint)) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.manual_note_hint)) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    val paise = (amount.toLongOrNull() ?: 0L) * 100L
                    vm.save(
                        type = type,
                        amountPaise = paise,
                        personName = person.takeIf { it.isNotBlank() },
                        item = item.ifBlank { type.name.replace("_", " ").lowercase() },
                        note = note.ifBlank { null }
                    )
                    onSaved()
                },
                enabled = amount.isNotBlank() && (!personNeeded || person.isNotBlank()),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Text(stringResource(R.string.manual_save), style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun TypeChip(labelRes: Int, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(stringResource(labelRes), style = MaterialTheme.typography.labelLarge) },
        shape = RoundedCornerShape(24.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier.height(48.dp)
    )
}
