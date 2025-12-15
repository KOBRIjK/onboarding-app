package com.example.darkonboarding.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.darkonboarding.ui.components.AppGlowBackground
import com.example.darkonboarding.ui.navigation.AppNavHost
import com.example.darkonboarding.ui.navigation.BottomNavBar

@Composable
fun App() {
    val navController = rememberNavController()

    AppGlowBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            bottomBar = { BottomNavBar(navController) }
        ) { padding ->
            Box(Modifier.fillMaxSize()) {
                AppNavHost(navController = navController, contentPadding = padding)
            }
        }
    }
}
