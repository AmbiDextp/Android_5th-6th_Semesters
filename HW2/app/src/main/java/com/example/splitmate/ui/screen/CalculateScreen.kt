package com.example.splitmate.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splitmate.model.SplitMateUIState


@Composable
fun CalculateScreen(
    uiState: SplitMateUIState,
    validSum: Boolean,
    validPeople: Boolean,
    validTip: Boolean,
    changeSum: (String) -> Unit,
    ChangePeople: (String) -> Unit,
    ChangeTip: (String) -> Unit,
    SaveCheck: () -> Unit,
    onLoadResult: () -> Unit
) {

    Column(
        Modifier.width((LocalConfiguration.current.screenWidthDp * 0.8).dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        OutlinedTextField(
            value = uiState.curSum,
            onValueChange = changeSum,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            label = @Composable { Text("Сумма чека") },
            singleLine = true,
        )

        OutlinedTextField(
            value = uiState.curPeople,
            onValueChange = ChangePeople,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            label = @Composable { Text("Количество персон") },
            singleLine = true,
        )

        OutlinedTextField(
            value = uiState.curTip,
            onValueChange = ChangeTip,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            label = @Composable { Text("Чаевые (% от чека)") },
            singleLine = true,
        )

        Button(
            enabled = (validSum && validPeople && validTip),
            onClick = { SaveCheck();onLoadResult() },
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
        ) { Text(text = "Рассчитать", fontSize = 20.sp, color = Color.Black) }
    }
}

