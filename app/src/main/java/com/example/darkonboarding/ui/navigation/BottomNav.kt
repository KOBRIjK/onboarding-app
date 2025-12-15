package com.example.darkonboarding.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.darkonboarding.ui.theme.AccentCyan
import com.example.darkonboarding.ui.theme.NavBarBg

data class BottomTab(
    val route: Route,
    val label: String,
    val icon: @Composable () -> Unit
)

@Composable
fun BottomNavBar(navController: NavController) {

    val tabs = listOf(
        BottomTab(Route.Home, "Главная") {
            Icon(Icons.Default.Home, contentDescription = null)
        },
        BottomTab(Route.Answer, "Ответ") {
            Icon(Icons.Default.ChatBubbleOutline, contentDescription = null)
        },
        BottomTab(Route.Tasks, "Задачи") {
            Icon(Icons.Default.Checklist, contentDescription = null)
        },
        BottomTab(Route.Docs, "Документы") {
            Icon(Icons.Default.Description, contentDescription = null)
        },
        BottomTab(Route.Profile, "Профиль") {
            Icon(Icons.Default.Person, contentDescription = null)
        }
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar(containerColor = NavBarBg) {

        tabs.forEach { tab ->

            // 🔥 ВАЖНО: startsWith
            val selected = currentRoute
                ?.startsWith(tab.route.value)
                ?: false

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(tab.route.value) {
                        popUpTo(Route.Home.value) {
                            inclusive = false
                            saveState = false
                        }

                        launchSingleTop = true
                        restoreState = false
                    }
                },
                icon = tab.icon,
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = AccentCyan,
                    unselectedIconColor = Color(0xFF8B93A7),
                    unselectedTextColor = Color(0xFF8B93A7),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
