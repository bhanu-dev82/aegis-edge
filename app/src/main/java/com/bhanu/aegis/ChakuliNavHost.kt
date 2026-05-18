package com.bhanu.aegis

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bhanu.aegis.core.data.repository.ThemeMode
import com.bhanu.aegis.core.theme.ChakuliTheme
import com.bhanu.aegis.feature.settings.SettingsScreen
import com.bhanu.aegis.feature.settings.SettingsViewModel
import com.bhanu.aegis.feature.triage.TriageDashboardScreen
import com.bhanu.aegis.feature.triage.TriageViewModel

object AegisRoutes {
    const val TRIAGE   = "triage"
    const val SETTINGS = "settings"
}

@Composable
fun ChakuliRoot(modifier: Modifier = Modifier) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val themeMode by settingsViewModel.themeMode.collectAsState()

    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT  -> false
        ThemeMode.DARK   -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    // TriageViewModel hoisted to activity scope — engine + queue survive tab switches.
    val triageViewModel: TriageViewModel = hiltViewModel()

    ChakuliTheme(darkTheme = darkTheme) {
        AegisNavHost(modifier = modifier, triageViewModel = triageViewModel)
    }
}

@Composable
fun AegisNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    triageViewModel: TriageViewModel,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        modifier  = modifier,
        bottomBar = {
            if (currentRoute == AegisRoutes.TRIAGE || currentRoute == AegisRoutes.SETTINGS) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == AegisRoutes.TRIAGE,
                        onClick  = {
                            if (currentRoute != AegisRoutes.TRIAGE) {
                                navController.navigate(AegisRoutes.TRIAGE) {
                                    popUpTo(AegisRoutes.TRIAGE) { inclusive = true }
                                }
                            }
                        },
                        icon  = { Icon(Icons.Outlined.LocalHospital, "Triage") },
                        label = { Text("Triage") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == AegisRoutes.SETTINGS,
                        onClick  = {
                            if (currentRoute != AegisRoutes.SETTINGS) {
                                navController.navigate(AegisRoutes.SETTINGS) {
                                    popUpTo(AegisRoutes.TRIAGE)
                                }
                            }
                        },
                        icon  = { Icon(Icons.Outlined.Settings, null) },
                        label = { Text("Settings") },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = AegisRoutes.TRIAGE,
            modifier         = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            composable(AegisRoutes.TRIAGE) {
                TriageDashboardScreen(
                    viewModel = triageViewModel,
                    onNavigateToModels = {
                        navController.navigate(AegisRoutes.SETTINGS)
                    },
                )
            }

            composable(AegisRoutes.SETTINGS) {
                SettingsScreen(
                    onNavigateUp = { navController.popBackStack() },
                )
            }
        }
    }
}
