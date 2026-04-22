package com.hisabbook.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hisabbook.app.data.prefs.AppPreferences
import com.hisabbook.app.data.stt.ModelInstaller
import com.hisabbook.app.domain.device.DeviceClassifier
import com.hisabbook.app.domain.device.DeviceProfile
import com.hisabbook.app.domain.device.VoiceTier
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VoiceAiStatus(
    val tier: VoiceTier,
    val modelReady: Boolean,
    val assetPresent: Boolean,
    val totalRamGb: Int,
    val soc: String
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferences,
    private val classifier: DeviceClassifier,
    private val installer: ModelInstaller
) : ViewModel() {

    val lockEnabled = prefs.lockEnabled

    private val _voiceStatus = MutableStateFlow(initialStatus())
    val voiceStatus: StateFlow<VoiceAiStatus> = _voiceStatus.asStateFlow()

    fun setLock(enabled: Boolean) {
        viewModelScope.launch { prefs.setLockEnabled(enabled) }
    }

    fun installModel() {
        viewModelScope.launch {
            installer.install()
            _voiceStatus.value = snapshot()
        }
    }

    private fun initialStatus(): VoiceAiStatus = snapshot()

    private fun snapshot(): VoiceAiStatus {
        val profile: DeviceProfile = classifier.classify()
        return VoiceAiStatus(
            tier = profile.tier,
            modelReady = installer.isInstalled(),
            assetPresent = installer.assetAvailable(),
            totalRamGb = profile.totalRamGb,
            soc = profile.soc
        )
    }
}
