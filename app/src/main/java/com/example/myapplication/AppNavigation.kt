package com.example.myapplication

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.detail.DetailScreen
import com.example.myapplication.feed.FeedScreen
import com.example.myapplication.landing.LandingPage
import com.example.myapplication.post.PostScreen
import com.example.myapplication.profile.ProfileScreen
import com.example.myapplication.settings.SettingsScreen



sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Feed : BottomNavItem(Routes.FEED, "Home", Icons.Default.Home)
    object Post : BottomNavItem(Routes.POST, "Report", Icons.Default.Add)
    object Profile : BottomNavItem(Routes.PROFILE, "Profile", Icons.Default.Person)
}

object Routes {
    const val LANDING = "landing"
    const val FEED = "feed"
    const val POST = "post"
    const val PROFILE = "profile"
    const val DETAIL = "detail/{complaintId}"

    const val SETTINGS = "settings"

}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val currentRoute = navController
        .currentBackStackEntryAsState().value?.destination?.route

    val bottomNavScreens = listOf(Routes.FEED, Routes.POST, Routes.PROFILE)
    val showBottomNav = currentRoute in bottomNavScreens

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar {
                    listOf(
                        BottomNavItem.Feed,
                        BottomNavItem.Post,
                        BottomNavItem.Profile
                    ).forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(Routes.FEED) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.LANDING
        ) {
            composable(Routes.LANDING) {
                LandingPage(
                    onChooseLanguage = { },
                    onGetStarted = { navController.navigate(Routes.FEED) }
                )
            }

            composable(Routes.FEED) {
                FeedScreen(
                    onComplaintClick = { id ->
                        navController.navigate("detail/$id")
                    },
                    onPostClick = {
                        navController.navigate(Routes.POST)
                    }
                )
            }

            composable(Routes.DETAIL) { backStackEntry ->
                val complaintId = backStackEntry.arguments?.getString("complaintId") ?: "1"
                DetailScreen(
                    complaintId = complaintId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Routes.POST) {
                PostScreen(
                    onBackClick = { navController.popBackStack() },
                    onSubmitSuccess = { navController.navigate(Routes.FEED) }
                )

            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    onSettingsClick = {navController.navigate(Routes.SETTINGS)}
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}