package com.qracesso.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.qracesso.app.presentation.main.MainScreen
import com.qracesso.app.presentation.main.MainViewModel
import com.qracesso.app.presentation.settings.SettingsScreen

private const val ROUTE_MAIN = "main"
private const val ROUTE_SETTINGS = "settings"

@Composable
fun QRAcessoNavHost(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_MAIN) {
        composable(ROUTE_MAIN) {
            MainScreen(
                viewModel = viewModel,
                onOpenSettings = { navController.navigate(ROUTE_SETTINGS) }
            )
        }
        composable(ROUTE_SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
