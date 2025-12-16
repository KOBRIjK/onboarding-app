package com.example.darkonboarding.ui.screens.answer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AnswerViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _question = MutableStateFlow(savedStateHandle[QUESTION_KEY].orEmpty())
    val question: StateFlow<String> = _question.asStateFlow()

    fun onQuestionChange(newQuestion: String) {
        _question.value = newQuestion
        savedStateHandle[QUESTION_KEY] = newQuestion
    }

    fun initializeQuestion(initialQuestion: String) {
        if (initialQuestion.isEmpty()) return

        _question.value = initialQuestion
        savedStateHandle[QUESTION_KEY] = initialQuestion
    }

    private companion object {
        const val QUESTION_KEY = "answer_question_state"
    }
}
