package com.example.darkonboarding.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.darkonboarding.ui.theme.AccentCyan
import com.example.darkonboarding.ui.theme.AccentPurple
import com.example.darkonboarding.ui.theme.NavBarBg

data class BottomTab(
    val route: Route,
    val label: String,
    val icon: @Composable () -> Unit
)

@Composable
fun BottomNavBar(navController: NavController) {
    val tabs = listOf(
        BottomTab(Route.Home, "Главная") { Icon(Icons.Default.Home, contentDescription = null) },
        BottomTab(Route.Answer, "Ответ") { Icon(Icons.Default.ChatBubbleOutline, contentDescription = null) },
        BottomTab(Route.Tasks, "Задачи") { Icon(Icons.Default.Checklist, contentDescription = null) },
        BottomTab(Route.Docs, "Документы") { Icon(Icons.Default.Description, contentDescription = null) },
        BottomTab(Route.Profile, "Профиль") { Icon(Icons.Default.Person, contentDescription = null) },
    )

    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    NavigationBar(
        containerColor = NavBarBg,
        tonalElevation = 0.dp
    ) {
        tabs.forEach { tab ->
            val selected = current == tab.route.value
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(tab.route.value) {
                        popUpTo(Route.Home.value) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    // небольшая “пилюля” вокруг иконки как в макете
                    val tint = if (selected) Color.White else Color(0xFF8B93A7)
                    Box(Modifier.padding(vertical = 4.dp)) {
                        androidx.compose.material3.Surface(
                            color = if (selected) AccentPurple.copy(alpha = 0.22f) else Color.Transparent,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                        ) {
                            Box(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                androidx.compose.runtime.CompositionLocalProvider(
                                    androidx.compose.material3.LocalContentColor provides tint
                                ) { tab.icon() }
                            }
                        }
                    }
                },
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
