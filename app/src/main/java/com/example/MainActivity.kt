package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.repository.UserPreferences
import com.example.ui.components.AppBottomBar
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.food.AddEditFoodScreen
import com.example.ui.food.FoodViewModel
import com.example.ui.history.HistoryScreen
import com.example.ui.history.HistoryViewModel
import com.example.ui.history.ReportsScreen
import com.example.ui.medicine.AddEditMedicineScreen
import com.example.ui.medicine.MedicineDetailScreen
import com.example.ui.medicine.MedicineViewModel
import com.example.ui.navigation.Screen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.onboarding.PermissionSetupScreen
import com.example.ui.onboarding.SplashScreen
import com.example.ui.schedule.DailyScheduleScreen
import com.example.ui.settings.PrivacyAboutScreen
import com.example.ui.settings.ProfileScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.JoitonnessaTheme
import com.example.ui.water.WaterTrackerScreen
import com.example.ui.water.WaterViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as JoitonnessaApplication

        val dashboardViewModel: DashboardViewModel by viewModels {
            DashboardViewModel.Factory(
                app.medicineRepository,
                app.foodReminderRepository,
                app.waterRepository,
                app.reminderLogRepository,
                app.userPreferencesRepository,
                app.alarmScheduler
            )
        }

        val medicineViewModel: MedicineViewModel by viewModels {
            MedicineViewModel.Factory(
                app.medicineRepository,
                app.reminderLogRepository,
                app.alarmScheduler
            )
        }

        val foodViewModel: FoodViewModel by viewModels {
            FoodViewModel.Factory(
                app.foodReminderRepository,
                app.reminderLogRepository,
                app.alarmScheduler
            )
        }

        val waterViewModel: WaterViewModel by viewModels {
            WaterViewModel.Factory(
                app.waterRepository,
                app.alarmScheduler
            )
        }

        val historyViewModel: HistoryViewModel by viewModels {
            HistoryViewModel.Factory(
                app.reminderLogRepository
            )
        }

        setContent {
            val userPrefs by app.userPreferencesRepository.userPreferencesFlow.collectAsStateWithLifecycle(
                initialValue = UserPreferences()
            )

            val isDarkTheme = when (userPrefs.themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            JoitonnessaTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val isBottomBarVisible = currentRoute in listOf(
                    Screen.Dashboard.route,
                    Screen.Schedule.route,
                    Screen.History.route,
                    Screen.Settings.route
                )

                val scope = rememberCoroutineScope()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (isBottomBarVisible) {
                            AppBottomBar(
                                currentRoute = currentRoute,
                                language = userPrefs.language,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(Screen.Dashboard.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Splash.route) {
                            SplashScreen(
                                userPreferences = userPrefs,
                                onNavigateToOnboarding = {
                                    navController.navigate(Screen.Onboarding.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                },
                                onNavigateToDashboard = {
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.Onboarding.route) {
                            OnboardingScreen(
                                language = userPrefs.language,
                                onFinishOnboarding = {
                                    navController.navigate(Screen.Permissions.route)
                                }
                            )
                        }

                        composable(Screen.Permissions.route) {
                            PermissionSetupScreen(
                                language = userPrefs.language,
                                onContinue = {
                                    scope.launch {
                                        app.userPreferencesRepository.setOnboardingCompleted(true)
                                    }
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.Dashboard.route) {
                            DashboardScreen(
                                viewModel = dashboardViewModel,
                                onNavigate = { route -> navController.navigate(route) }
                            )
                        }

                        composable(Screen.Schedule.route) {
                            DailyScheduleScreen(
                                dashboardViewModel = dashboardViewModel,
                                language = userPrefs.language,
                                onNavigate = { route -> navController.navigate(route) }
                            )
                        }

                        composable(Screen.History.route) {
                            HistoryScreen(
                                viewModel = historyViewModel,
                                language = userPrefs.language,
                                onNavigate = { route -> navController.navigate(route) }
                            )
                        }

                        composable(Screen.Reports.route) {
                            ReportsScreen(
                                viewModel = historyViewModel,
                                language = userPrefs.language,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Water.route) {
                            WaterTrackerScreen(
                                viewModel = waterViewModel,
                                language = userPrefs.language,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                userPreferencesRepository = app.userPreferencesRepository,
                                notificationHelper = app.notificationHelper,
                                onNavigate = { route -> navController.navigate(route) }
                            )
                        }

                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                userPreferencesRepository = app.userPreferencesRepository,
                                language = userPrefs.language,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.PrivacyAbout.route) {
                            PrivacyAboutScreen(
                                language = userPrefs.language,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.AddMedicine.route) {
                            AddEditMedicineScreen(
                                medicineId = 0L,
                                viewModel = medicineViewModel,
                                language = userPrefs.language,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Screen.EditMedicine.route,
                            arguments = listOf(navArgument("medicineId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val medId = backStackEntry.arguments?.getLong("medicineId") ?: 0L
                            AddEditMedicineScreen(
                                medicineId = medId,
                                viewModel = medicineViewModel,
                                language = userPrefs.language,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Screen.MedicineDetail.route,
                            arguments = listOf(navArgument("medicineId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val medId = backStackEntry.arguments?.getLong("medicineId") ?: 0L
                            MedicineDetailScreen(
                                medicineId = medId,
                                viewModel = medicineViewModel,
                                language = userPrefs.language,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToEdit = { id ->
                                    navController.navigate(Screen.EditMedicine.createRoute(id))
                                }
                            )
                        }

                        composable(Screen.FoodSchedule.route) {
                            com.example.ui.food.FoodScheduleScreen(
                                viewModel = foodViewModel,
                                language = userPrefs.language,
                                onNavigateBack = { navController.popBackStack() },
                                onEditReminder = { id ->
                                    navController.navigate(Screen.EditFood.createRoute(id))
                                }
                            )
                        }

                        composable(Screen.AddFood.route) {
                            AddEditFoodScreen(
                                foodId = 0L,
                                viewModel = foodViewModel,
                                language = userPrefs.language,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Screen.EditFood.route,
                            arguments = listOf(navArgument("foodId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val foodId = backStackEntry.arguments?.getLong("foodId") ?: 0L
                            AddEditFoodScreen(
                                foodId = foodId,
                                viewModel = foodViewModel,
                                language = userPrefs.language,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
