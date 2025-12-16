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
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.darkonboarding.ui.theme.NavBarBg

data class BottomTab(
    val route: Route,
    val label: String,
    val icon: @Composable () -> Unit
)


fun NavController.navigateToTab(
    tab: Route,
    selected: Boolean
) {
    when {
        tab == Route.Answer && selected -> {
            navigate(Route.Answer.value) {
                popUpTo(Route.Answer.routeWithOptionalArg) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }

        tab == Route.Home -> {
            navigate(Route.Home.value) {
                popUpTo(graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        else -> {
            navigate(tab.value) {
                popUpTo(Route.Home.value) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController) {

    val tabs = remember {
        listOf(
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
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar(containerColor = NavBarBg) {

        tabs.forEach { tab ->
            val selected = remember(currentRoute) {
                currentRoute?.startsWith(tab.route.value) == true
            }

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigateToTab(tab.route, selected)
                },
                icon = tab.icon,
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedIconColor = Color(0xFF8B93A7),
                    unselectedTextColor = Color(0xFF8B93A7),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
