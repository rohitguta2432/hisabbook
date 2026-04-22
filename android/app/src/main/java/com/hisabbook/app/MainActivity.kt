package com.hisabbook.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import com.hisabbook.app.data.prefs.AppPreferences
import com.hisabbook.app.security.AppLockManager
import com.hisabbook.app.ui.navigation.HisabBookNavHost
import com.hisabbook.app.ui.screens.lock.AppLockScreen
import com.hisabbook.app.ui.theme.HisabBookTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var lockManager: AppLockManager
    @Inject lateinit var prefs: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HisabBookTheme {
                val lockEnabled by prefs.lockEnabled.collectAsState(initial = true)
                var unlocked by remember { mutableStateOf(false) }

                if (lockEnabled && !unlocked) {
                    AppLockScreen(
                        lockManager = lockManager,
                        onUnlocked = { unlocked = true }
                    )
                } else {
                    HisabBookNavHost()
                }
            }
        }
    }
}
