package com.example.darkonboarding.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.darkonboarding.ui.screens.answer.AnswerScreen
import com.example.darkonboarding.ui.screens.docs.DocsScreen
import com.example.darkonboarding.ui.screens.home.HomeScreen
import com.example.darkonboarding.ui.screens.profile.ProfileScreen
import com.example.darkonboarding.ui.screens.tasks.TasksScreen


@Composable
fun AppNavHost(
    navController: NavHostController,
    contentPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home.value,
        modifier = Modifier.padding(contentPadding)
    ) {
        composable(Route.Home.value) { HomeScreen() }
        composable(Route.Answer.value) { AnswerScreen() }
        composable(Route.Tasks.value) { TasksScreen() }
        composable(Route.Docs.value) { DocsScreen() }
        composable(Route.Profile.value) { ProfileScreen() }
    }
}
