package com.example.cinematest.ui.screen

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cinematest.model.Question

@Composable
fun QuestionScreen(questions: List<Question>, end: () -> Unit, cnt: () -> Unit) {
    var number: Int by remember { mutableStateOf(0) }
    var response: Int by remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier
            .padding(64.dp)
            .fillMaxWidth(),
    ) {
        Text(
            modifier = Modifier.padding(bottom = 10.dp),
            text =
                """
                Вопрос ${questions[number].id} из ${questions.size}
                ${questions[number].question}
            """.trimIndent(),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
        )


        for(i in 0..<questions[number].responses.size) {
            Row(
                Modifier
                    .selectable(
                        selected = (i == response),
                        onClick = { response = i },
                        role = Role.RadioButton,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    )
                    .padding(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = (i == response),
                    onClick = null
                )
                Text(
                    text = questions[number].responses[i],
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 5.dp)

                )
            }
        }

        Button(
            enabled = (response != -1),
            onClick = {
                if (response == questions[number].correct) cnt()
                if (number < questions.size - 1) number++
                else end()
                response = -1
            },
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Далее", fontSize = 20.sp)
        }
    }
}