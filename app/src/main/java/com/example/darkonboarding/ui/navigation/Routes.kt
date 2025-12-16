package com.example.darkonboarding.ui.navigation

import android.net.Uri

sealed class Route(val value: String) {

    data object Home : Route("home")

    /**
     * Корневой граф для экрана ответа, позволяющий делиться состоянием между табом и деталями.
     */
    data object AnswerRoot : Route("answer_root")

    /**
     * Вкладка в нижней навигации для поиска и генерации ответов без обязательных аргументов.
     */
    data object AnswerTab : Route("answer_tab")

    /**
     * Экран деталей ответа, на который переходим из Home с конкретным вопросом.
     */
    data object AnswerDetails : Route("answer/{question}") {
        const val QUESTION_ARG = "question"
        val routeWithArg = value

        fun createRoute(question: String): String =
            "answer/${Uri.encode(question)}"
    }

    data object Tasks : Route("tasks")
    data object Docs : Route("docs")
    data object Profile : Route("profile")
}
