package com.hisabbook.app.ui.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hisabbook.app.R
import com.hisabbook.app.data.prefs.AppPreferences
import com.hisabbook.app.ui.screens.backup.BackupScreen
import com.hisabbook.app.ui.screens.home.HomeScreen
import com.hisabbook.app.ui.screens.settings.LanguageScreen
import com.hisabbook.app.ui.screens.khata.AddPersonScreen
import com.hisabbook.app.ui.screens.khata.CustomerKhataScreen
import com.hisabbook.app.ui.screens.khata.KhataListScreen
import com.hisabbook.app.ui.screens.manual.ManualEntryScreen
import com.hisabbook.app.ui.screens.onboarding.OnboardingScreen
import com.hisabbook.app.ui.screens.settings.SettingsScreen
import com.hisabbook.app.ui.screens.summary.DailySummaryScreen
import com.hisabbook.app.ui.screens.voice.VoiceEntryScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val KHATA_LIST = "khata_list"
    const val KHATA_PERSON = "khata_person"
    const val KHATA_ADD_PERSON = "khata_add_person"
    const val SUMMARY = "summary"
    const val SETTINGS = "settings"
    const val VOICE = "voice"
    const val MANUAL = "manual"
    const val BACKUP_EXPORT = "backup_export"
    const val BACKUP_IMPORT = "backup_import"
    const val LANGUAGE = "language"

    fun personRoute(id: String) = "$KHATA_PERSON/$id"
}

@HiltViewModel
class StartViewModel @Inject constructor(
    private val prefs: AppPreferences
) : ViewModel() {
    val onboardingDone = prefs.onboardingDone

    fun markOnboardingDone() {
        viewModelScope.launch { prefs.setOnboardingDone(true) }
    }
}

private data class NavTab(val route: String, val labelRes: Int, val icon: ImageVector)

private val tabs = listOf(
    NavTab(Routes.HOME, R.string.nav_ghar, Icons.Default.Home),
    NavTab(Routes.KHATA_LIST, R.string.nav_khata, Icons.AutoMirrored.Filled.MenuBook),
    NavTab(Routes.SUMMARY, R.string.nav_hisab, Icons.Default.Receipt),
    NavTab(Routes.SETTINGS, R.string.nav_setting, Icons.Default.Settings)
)

@Composable
fun HisabBookNavHost(
    nav: NavHostController = rememberNavController(),
    vm: StartViewModel = hiltViewModel()
) {
    val onboardingDone by vm.onboardingDone.collectAsState(initial = false)
    val start = if (onboardingDone) Routes.HOME else Routes.ONBOARDING

    NavHost(navController = nav, startDestination = start) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onStart = {
                vm.markOnboardingDone()
                nav.navigate(Routes.HOME) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }
        mainTabRoutes(nav)
        composable(Routes.VOICE) {
            VoiceEntryScreen(
                onClose = { nav.popBackStack() },
                onConfirm = { nav.popBackStack() },
                onRetry = {},
                onManualFallback = {
                    nav.navigate(Routes.MANUAL) { popUpTo(Routes.VOICE) { inclusive = true } }
                }
            )
        }
        composable(Routes.MANUAL) {
            ManualEntryScreen(
                onBack = { nav.popBackStack() },
                onSaved = { nav.popBackStack() }
            )
        }
        composable(Routes.KHATA_ADD_PERSON) {
            AddPersonScreen(
                onBack = { nav.popBackStack() },
                onSaved = { nav.popBackStack() }
            )
        }
        composable(Routes.BACKUP_EXPORT) {
            BackupScreen(isExport = true, onBack = { nav.popBackStack() })
        }
        composable(Routes.BACKUP_IMPORT) {
            BackupScreen(isExport = false, onBack = { nav.popBackStack() })
        }
        composable(Routes.LANGUAGE) {
            LanguageScreen(onBack = { nav.popBackStack() })
        }
        composable(
            route = "${Routes.KHATA_PERSON}/{personId}",
            arguments = listOf(navArgument("personId") { type = NavType.StringType })
        ) { backStack ->
            val personId = backStack.arguments?.getString("personId") ?: "p1"
            CustomerKhataScreen(
                personId = personId,
                onBack = { nav.popBackStack() },
                onNewEntry = { nav.navigate(Routes.VOICE) },
                bottomBar = { HisabBottomBar(nav) }
            )
        }
    }
}

private fun NavGraphBuilder.mainTabRoutes(nav: NavHostController) {
    composable(Routes.HOME) {
        HomeScreen(
            onBolo = { nav.navigate(Routes.VOICE) },
            bottomBar = { HisabBottomBar(nav) }
        )
    }
    composable(Routes.KHATA_LIST) {
        KhataListScreen(
            onOpenPerson = { id -> nav.navigate(Routes.personRoute(id)) },
            onAddPerson = { nav.navigate(Routes.KHATA_ADD_PERSON) },
            bottomBar = { HisabBottomBar(nav) }
        )
    }
    composable(Routes.SUMMARY) {
        DailySummaryScreen(
            bottomBar = { HisabBottomBar(nav) }
        )
    }
    composable(Routes.SETTINGS) {
        SettingsScreen(
            bottomBar = { HisabBottomBar(nav) },
            onExportBackup = { nav.navigate(Routes.BACKUP_EXPORT) },
            onImportBackup = { nav.navigate(Routes.BACKUP_IMPORT) },
            onOpenLanguage = { nav.navigate(Routes.LANGUAGE) }
        )
    }
}

@Composable
private fun HisabBottomBar(nav: NavHostController) {
    val entry by nav.currentBackStackEntryAsState()
    val current = entry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.height(80.dp)
    ) {
        tabs.forEach { tab ->
            val selected = current == tab.route ||
                (tab.route == Routes.KHATA_LIST && current?.startsWith(Routes.KHATA_PERSON) == true)
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        nav.navigate(tab.route) {
                            popUpTo(Routes.HOME)
                            launchSingleTop = true
                        }
                    }
                },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = {
                    Text(
                        stringResource(tab.labelRes),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
