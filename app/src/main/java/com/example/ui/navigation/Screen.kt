package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Permissions : Screen("permissions")
    object Dashboard : Screen("dashboard")
    object Schedule : Screen("schedule")
    object History : Screen("history")
    object Reports : Screen("reports")
    object Water : Screen("water")
    object Settings : Screen("settings")
    object Profile : Screen("profile")
    object PrivacyAbout : Screen("privacy_about")
    object AddMedicine : Screen("add_medicine")
    object EditMedicine : Screen("edit_medicine/{medicineId}") {
        fun createRoute(medicineId: Long) = "edit_medicine/$medicineId"
    }
    object MedicineDetail : Screen("medicine_detail/{medicineId}") {
        fun createRoute(medicineId: Long) = "medicine_detail/$medicineId"
    }
    object FoodSchedule : Screen("food_schedule")
    object AddFood : Screen("add_food")
    object EditFood : Screen("edit_food/{foodId}") {
        fun createRoute(foodId: Long) = "edit_food/$foodId"
    }
}
