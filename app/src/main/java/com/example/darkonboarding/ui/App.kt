package com.example.darkonboarding.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.darkonboarding.data.auth.AuthSession
import com.example.darkonboarding.data.auth.AuthStore
import com.example.darkonboarding.ui.components.AppGlowBackground
import com.example.darkonboarding.ui.navigation.AppNavHost
import com.example.darkonboarding.ui.navigation.BottomNavBar
import com.example.darkonboarding.ui.screens.auth.LoginScreen

@Composable
fun App() {
    val context = LocalContext.current
    val navController = rememberNavController()
    var session by remember {
        mutableStateOf(AuthStore.load(context.applicationContext))
    }

    AppGlowBackground {
        if (session == null) {
            LoginScreen(
                onAuthenticated = { newSession: AuthSession ->
                    AuthStore.save(context.applicationContext, newSession)
                    session = newSession
                }
            )
        } else {
            Scaffold(
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                bottomBar = { BottomNavBar(navController) }
            ) { padding ->
                Box(Modifier.fillMaxSize()) {
                    AppNavHost(
                        navController = navController,
                        contentPadding = padding,
                        accessToken = session!!.accessToken,
                        onLogout = {
                            AuthStore.clear(context.applicationContext)
                            session = null
                        }
                    )
                }
            }
        }
    }
}
