package com.hisabbook.app.ui.screens.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabbook.app.data.backup.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BackupState {
    data object Idle : BackupState()
    data object Working : BackupState()
    data class Success(val message: String) : BackupState()
    data class Error(val message: String) : BackupState()
}

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupManager: BackupManager
) : ViewModel() {

    private val _state = MutableStateFlow<BackupState>(BackupState.Idle)
    val state = _state.asStateFlow()

    fun export(uri: Uri, passphrase: String) {
        if (passphrase.length < 4) {
            _state.value = BackupState.Error("Passphrase chhota hai")
            return
        }
        _state.value = BackupState.Working
        viewModelScope.launch {
            runCatching { backupManager.exportTo(uri, passphrase.toCharArray()) }
                .onSuccess { _state.value = BackupState.Success("Backup save ho gaya") }
                .onFailure { _state.value = BackupState.Error(it.message ?: "Backup fail") }
        }
    }

    fun import(uri: Uri, passphrase: String) {
        if (passphrase.length < 4) {
            _state.value = BackupState.Error("Passphrase chhota hai")
            return
        }
        _state.value = BackupState.Working
        viewModelScope.launch {
            runCatching { backupManager.importFrom(uri, passphrase.toCharArray()) }
                .onSuccess { count -> _state.value = BackupState.Success("$count entries wapas aayi") }
                .onFailure { _state.value = BackupState.Error(it.message ?: "Restore fail") }
        }
    }

    fun reset() { _state.value = BackupState.Idle }
}
