package com.example.darkonboarding.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.darkonboarding.data.auth.AuthEnvironment
import com.example.darkonboarding.ui.auth.AuthScreen
import com.example.darkonboarding.ui.auth.AuthUiState
import com.example.darkonboarding.ui.auth.AuthViewModel
import com.example.darkonboarding.ui.components.AppGlowBackground
import com.example.darkonboarding.ui.navigation.AppNavHost
import com.example.darkonboarding.ui.navigation.BottomNavBar

@Composable
fun App() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authEnvironment = remember { AuthEnvironment(context.applicationContext) }
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(authEnvironment.authRepository))
    val authState by authViewModel.state.collectAsStateWithLifecycle()

    AppGlowBackground {
        when (authState) {
            AuthUiState.Loading, AuthUiState.Submitting -> {
                Box(Modifier.fillMaxSize()) {
                    com.example.darkonboarding.ui.components.LoadingDots()
                }
            }

            AuthUiState.Authenticated -> {
                Scaffold(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    bottomBar = { BottomNavBar(navController) }
                ) { padding ->
                    Box(Modifier.fillMaxSize()) {
                        AppNavHost(
                            navController = navController,
                            contentPadding = padding,
                            onLogout = { authViewModel.logout() }
                        )
                    }
                }
            }

            else -> {
                AuthScreen(viewModel = authViewModel)
            }
        }
    }
}
