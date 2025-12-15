package com.example.darkonboarding.ui.navigation

import android.net.Uri

sealed class Route(val value: String) {

    data object Home : Route("home")

    data object Answer : Route("answer") {
        const val QUESTION_ARG = "question"
        val routeWithArg = "answer/{$QUESTION_ARG}"

        fun createRoute(question: String): String =
            "answer/${Uri.encode(question)}"
    }

    data object Tasks : Route("tasks")
    data object Docs : Route("docs")
    data object Profile : Route("profile")
}
