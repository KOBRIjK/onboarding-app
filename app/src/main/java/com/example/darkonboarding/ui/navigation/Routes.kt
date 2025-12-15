package com.example.darkonboarding.ui.navigation

sealed class Route(val value: String) {
    data object Home : Route("home")
    data object Answer : Route("answer")
    data object Tasks : Route("tasks")
    data object Docs : Route("docs")
    data object Profile : Route("profile")
}
