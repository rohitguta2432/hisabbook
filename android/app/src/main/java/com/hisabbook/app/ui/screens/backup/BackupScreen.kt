package com.hisabbook.app.ui.screens.backup

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hisabbook.app.R
import com.hisabbook.app.ui.theme.StatusNegativeText
import com.hisabbook.app.ui.theme.StatusPositive

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    isExport: Boolean,
    onBack: () -> Unit,
    vm: BackupViewModel = hiltViewModel()
) {
    var passphrase by remember { mutableStateOf("") }
    val state by vm.state.collectAsState()

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == android.app.Activity.RESULT_OK) {
            res.data?.data?.let { uri -> vm.export(uri, passphrase) }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == android.app.Activity.RESULT_OK) {
            res.data?.data?.let { uri -> vm.import(uri, passphrase) }
        }
    }

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
                        stringResource(if (isExport) R.string.backup_export_title else R.string.backup_import_title),
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = passphrase,
                onValueChange = { passphrase = it },
                label = { Text(stringResource(R.string.backup_passphrase_hint)) },
                shape = RoundedCornerShape(16.dp),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            when (val s = state) {
                is BackupState.Working -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                is BackupState.Success -> Text(s.message, color = StatusPositive, style = MaterialTheme.typography.bodyLarge)
                is BackupState.Error -> Text(s.message, color = StatusNegativeText, style = MaterialTheme.typography.bodyLarge)
                BackupState.Idle -> {}
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (isExport) {
                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "application/octet-stream"
                            putExtra(Intent.EXTRA_TITLE, "hisabbook-backup-${System.currentTimeMillis()}.hbbk")
                        }
                        exportLauncher.launch(intent)
                    } else {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "*/*"
                        }
                        importLauncher.launch(intent)
                    }
                },
                enabled = passphrase.length >= 4 && state !is BackupState.Working,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth().height(64.dp)
            ) {
                Text(
                    stringResource(if (isExport) R.string.backup_export_cta else R.string.backup_import_cta),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}
