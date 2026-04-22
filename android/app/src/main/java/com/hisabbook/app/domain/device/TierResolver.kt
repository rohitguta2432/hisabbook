package com.hisabbook.app.domain.device

import com.hisabbook.app.data.prefs.AppPreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Resolves device Voice Tier with a safe-demote guard.
 *  First call classifies device + runs lightweight RAM allocation probe for T2/T3.
 *  If probe fails, tier is demoted to T1 and persisted so subsequent runs skip the probe. */
@Singleton
class TierResolver @Inject constructor(
    private val classifier: DeviceClassifier,
    private val prefs: AppPreferences
) {
    private val mutex = Mutex()

    suspend fun resolve(): VoiceTier = mutex.withLock {
        prefs.cachedTier.first()?.let { return@withLock it }

        val profile = classifier.classify()
        val candidate = profile.tier
        val final = if (candidate == VoiceTier.T2 || candidate == VoiceTier.T3) {
            if (probeHeadroom()) candidate else VoiceTier.T1
        } else candidate
        prefs.setCachedTier(final)
        final
    }

    /** Allocate ~256 MB in small chunks and release; cheap OOM canary for Gemma-class loads. */
    private fun probeHeadroom(): Boolean = try {
        val chunks = ArrayList<ByteArray>(32)
        repeat(32) { chunks.add(ByteArray(8 * 1024 * 1024)) } // 8MB × 32 = 256MB
        chunks.clear()
        System.gc()
        true
    } catch (oom: OutOfMemoryError) {
        System.gc()
        false
    }
}
