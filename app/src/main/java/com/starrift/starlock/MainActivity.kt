package com.starrift.starlock

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.starrift.starlock.data.AppDatabase
import com.starrift.starlock.data.AppRepository
import com.starrift.starlock.navigation.Routes
import com.starrift.starlock.ui.screens.AccountDetailScreen
import com.starrift.starlock.ui.screens.AccountDetailViewModel
import com.starrift.starlock.ui.screens.AccountDetailViewModelFactory
import com.starrift.starlock.ui.screens.AccountListScreen
import com.starrift.starlock.ui.screens.AccountListViewModel
import com.starrift.starlock.ui.screens.AccountListViewModelFactory
import com.starrift.starlock.ui.screens.HomeScreen
import com.starrift.starlock.ui.screens.HomeViewModel
import com.starrift.starlock.ui.screens.HomeViewModelFactory
import com.starrift.starlock.ui.screens.SettingsScreen
import com.starrift.starlock.ui.screens.SettingsViewModel
import com.starrift.starlock.ui.screens.SettingsViewModelFactory
import com.starrift.starlock.ui.screens.TrashScreen
import com.starrift.starlock.ui.screens.TrashViewModel
import com.starrift.starlock.ui.screens.TrashViewModelFactory
import com.starrift.starlock.ui.screens.ArchivedScreen
import com.starrift.starlock.ui.screens.ArchivedViewModel
import com.starrift.starlock.ui.screens.ArchivedViewModelFactory
import com.starrift.starlock.ui.theme.HesapYoneticisiTheme
import com.starrift.starlock.util.PinManager
import com.starrift.starlock.ui.screens.LockScreen
import java.util.Locale

class MainActivity : ComponentActivity() {
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Dil ayarını yükle
        val prefs = applicationContext.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val savedLang = prefs.getString("app_lang", "system") ?: "system"
        val savedTheme = prefs.getString("app_theme", "system") ?: "system"
        
        if (savedLang != "system") {
            val locale = Locale(savedLang)
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }

        val database = AppDatabase.getInstance(applicationContext)
        val repository = AppRepository(database)
        val sortPreferenceManager = com.starrift.starlock.util.SortPreferenceManager(applicationContext)
        
        val pinManager = PinManager(applicationContext)

        setContent {
            var themeMode by remember { mutableStateOf(savedTheme) }
            val useDarkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }
            HesapYoneticisiTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isUnlocked by remember { mutableStateOf(!pinManager.isPinSet()) }

                    if (isUnlocked) {
                        AppRoot(
                    repository = repository,
                    pinManager = pinManager,
                    themeMode = themeMode,
                    onThemeChange = { newMode ->
                        themeMode = newMode
                        prefs.edit().putString("app_theme", newMode).apply()
                    }
                )
                    } else {
                        LockScreen(
                            pinManager = pinManager,
                            onUnlocked = { isUnlocked = true }
                        )
                    }
                }
            }
        }
    }
}

private const val TRANSITION_DURATION = 320

@Composable
private fun AppRoot(repository: AppRepository, pinManager: PinManager, themeMode: String, onThemeChange: (String) -> Unit) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(
            route = Routes.HOME,
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(TRANSITION_DURATION)) +
                fadeOut(tween(TRANSITION_DURATION))
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(TRANSITION_DURATION)) +
                fadeIn(tween(TRANSITION_DURATION))
            }
        ) {
            val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository, sortPreferenceManager))
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(repository))
            HomeScreen(
                viewModel = homeViewModel,
                onAppClick = { appId -> navController.navigate(Routes.accountList(appId)) },
                settingsContent = { SettingsScreen(viewModel = settingsViewModel, onTrashClick = { navController.navigate(Routes.TRASH) }, onArchivedClick = { navController.navigate(Routes.ARCHIVED) }, themeMode = themeMode, onThemeChange = onThemeChange) }
            )
        }

        composable(
            route = Routes.ACCOUNT_LIST,
            arguments = listOf(navArgument("appId") { type = NavType.LongType }),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(TRANSITION_DURATION)) + fadeIn(tween(TRANSITION_DURATION))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(TRANSITION_DURATION)) + fadeOut(tween(TRANSITION_DURATION))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(TRANSITION_DURATION)) + fadeOut(tween(TRANSITION_DURATION))
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(TRANSITION_DURATION)) + fadeIn(tween(TRANSITION_DURATION))
            }
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getLong("appId") ?: 0L
            val accountListViewModel: AccountListViewModel = viewModel(
                factory = AccountListViewModelFactory(repository, appId, sortPreferenceManager)
            )
            AccountListScreen(
                viewModel = accountListViewModel,
                onBack = { navController.popBackStack() },
                onAccountClick = { accountId -> navController.navigate(Routes.accountDetail(accountId)) }
            )
        }

        composable(
            route = Routes.ACCOUNT_DETAIL,
            arguments = listOf(navArgument("accountId") { type = NavType.LongType }),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(TRANSITION_DURATION)) + fadeIn(tween(TRANSITION_DURATION))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(TRANSITION_DURATION)) + fadeOut(tween(TRANSITION_DURATION))
            }
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getLong("accountId") ?: 0L
            val accountDetailViewModel: AccountDetailViewModel = viewModel(
                factory = AccountDetailViewModelFactory(repository, accountId)
            )
            AccountDetailScreen(
                viewModel = accountDetailViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(route = Routes.TRASH) {
            var trashUnlocked by remember { mutableStateOf(!pinManager.isPinSet()) }
            if (trashUnlocked) {
                val trashViewModel: TrashViewModel = viewModel(factory = TrashViewModelFactory(repository))
                TrashScreen(
                    viewModel = trashViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            } else {
                LockScreen(
                    pinManager = pinManager,
                    onUnlocked = { trashUnlocked = true }
                )
            }
        }

    composable(route = Routes.ARCHIVED) {
        var archivedUnlocked by remember { mutableStateOf(!pinManager.isPinSet()) }
        if (archivedUnlocked) {
            val archivedViewModel: ArchivedViewModel = viewModel(factory = ArchivedViewModelFactory(repository))
            ArchivedScreen(
                viewModel = archivedViewModel,
                onBackClick = { navController.popBackStack() }
            )
        } else {
            LockScreen(
                pinManager = pinManager,
                onUnlocked = { archivedUnlocked = true }
            )
        }
    }
        }
}
