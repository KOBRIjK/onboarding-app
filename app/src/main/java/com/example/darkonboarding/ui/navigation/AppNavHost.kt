package com.example.darkonboarding.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.darkonboarding.ui.screens.answer.AnswerScreen
import com.example.darkonboarding.ui.screens.answer.AnswerViewModel
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

        // ───────── HOME ─────────
        composable(Route.Home.value) {
            HomeScreen(
                onQuestionClick = { question ->
                    navController.navigate(
                        Route.AnswerDetails.createRoute(question)
                    )
                },
                onSearchClick = {
                    navController.navigate(Route.AnswerTab.value) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // ───────── ANSWER GRAPH ─────────
        navigation(
            route = Route.AnswerRoot.value,
            startDestination = Route.AnswerTab.value
        ) {
            composable(Route.AnswerTab.value) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Route.AnswerRoot.value)
                }
                val answerViewModel: AnswerViewModel = viewModel(parentEntry)
                val question by answerViewModel.question.collectAsStateWithLifecycle()

                AnswerScreen(
                    question = question,
                    autoFocus = true,
                    onQuestionChange = answerViewModel::onQuestionChange
                )
            }

            composable(
                route = Route.AnswerDetails.routeWithArg,
                arguments = listOf(
                    navArgument(Route.AnswerDetails.QUESTION_ARG) {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Route.AnswerRoot.value)
                }
                val answerViewModel: AnswerViewModel = viewModel(parentEntry)

                val questionArg = backStackEntry.arguments
                    ?.getString(Route.AnswerDetails.QUESTION_ARG)
                    .orEmpty()

                LaunchedEffect(questionArg) {
                    answerViewModel.initializeQuestion(questionArg)
                }

                val question by answerViewModel.question.collectAsStateWithLifecycle()

                AnswerScreen(
                    question = question,
                    autoFocus = false,
                    onQuestionChange = answerViewModel::onQuestionChange,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Route.Tasks.value) { TasksScreen() }
        composable(Route.Docs.value) { DocsScreen() }
        composable(Route.Profile.value) { ProfileScreen() }
    }
}
