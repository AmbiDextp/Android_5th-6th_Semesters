package com.example.cinematest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.cinematest.data.Questions
import com.example.cinematest.ui.screen.QuestionScreen
import com.example.cinematest.ui.screen.ResultScreen
import com.example.cinematest.ui.screen.StartScreen

enum class State {
    START,
    TEST,
    RESULT
}

@Composable
fun App() {
    var state: State by remember { mutableStateOf(State.START) }
    var correct: Int by remember { mutableStateOf(0) }
    val startTest: () -> Unit = { state = State.TEST; correct = 0 }
    val startScreen: () -> Unit = { state = State.START; correct = 0 }

    Scaffold() { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state) {
                State.START -> StartScreen(startTest)
                State.TEST -> QuestionScreen(Questions, { state = State.RESULT }) {
                    ++correct
                }
                State.RESULT -> ResultScreen(correct, Questions.size, startScreen)
            }
        }
    }
}