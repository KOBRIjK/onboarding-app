package com.example.darkonboarding.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.darkonboarding.ui.screens.answer.AnswerScreen
import com.example.darkonboarding.ui.screens.docs.DocsScreen
import com.example.darkonboarding.ui.screens.home.HomeScreen
import com.example.darkonboarding.ui.screens.profile.ProfileScreen
import com.example.darkonboarding.ui.screens.tasks.TasksScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    contentPadding: PaddingValues,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home.value,
        modifier = Modifier.padding(contentPadding)
    ) {

        // ───────── HOME ─────────
        composable(Route.Home.value) {
            HomeScreen(
                onQuestionClick = { question ->
                    navController.navigate(
                        Route.Answer.createRoute(question)
                    )
                },
                onSearchClick = {
                    navController.navigate(
                        Route.Answer.value
                    )
                }
            )
        }

        // ───────── ANSWER (из BottomBar) ─────────
        composable(Route.Answer.value) {
            AnswerScreen(
                question = ""
            )
        }

        // ───────── ANSWER (из Home с вопросом) ─────────
        composable(
            route = Route.Answer.routeWithArg,
            arguments = listOf(
                navArgument(Route.Answer.QUESTION_ARG) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val question = backStackEntry.arguments
                ?.getString(Route.Answer.QUESTION_ARG)
                .orEmpty()

            AnswerScreen(question = question)
        }

        composable(Route.Tasks.value) { TasksScreen() }
        composable(Route.Docs.value) { DocsScreen() }
        composable(Route.Profile.value) { ProfileScreen(onLogout = onLogout) }
    }
}
