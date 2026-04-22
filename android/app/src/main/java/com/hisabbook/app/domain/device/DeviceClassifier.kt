package com.hisabbook.app.domain.device

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class VoiceTier { T0, T1, T2, T3 }

data class DeviceProfile(
    val totalRamGb: Int,
    val freeStorageGb: Int,
    val hasAICore: Boolean,
    val soc: String,
    val tier: VoiceTier
)

@Singleton
class DeviceClassifier @Inject constructor(@ApplicationContext private val ctx: Context) {

    fun classify(): DeviceProfile {
        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        val totalRamGb = (mi.totalMem / 1_073_741_824L).toInt().coerceAtLeast(1)
        val freeStorageGb = (Environment.getDataDirectory().freeSpace / 1_073_741_824L).toInt().coerceAtLeast(0)
        val hasAICore = ctx.packageManager.hasSystemFeature("android.software.ai_core")
        val soc = Build.SOC_MODEL.takeIf { it.isNotBlank() } ?: Build.HARDWARE

        val tier = when {
            hasAICore && totalRamGb >= 8 -> VoiceTier.T3
            totalRamGb >= 8 && freeStorageGb >= 8 -> VoiceTier.T3
            totalRamGb >= 6 && freeStorageGb >= 8 -> VoiceTier.T2
            totalRamGb >= 4 -> VoiceTier.T1
            else -> VoiceTier.T0
        }

        return DeviceProfile(totalRamGb, freeStorageGb, hasAICore, soc, tier)
    }
}
