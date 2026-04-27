package com.example.splitmate.ui.screen

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.example.splitmate.model.Check

@Composable
fun ResultScreen(
    check: Check,
    tipAmount: (Int, Int) -> Int,
    totalWithTip: (Int, Int) -> Int,
    perPerson: (Int, Int, Int) -> Int,
    showBackToEdit: Boolean,
    onBackToEdit: () -> Unit,
    onNewCalculation: () -> Unit,
    onLoadHistory: () -> Unit
) {

    Column(
        Modifier.width((LocalConfiguration.current.screenWidthDp * 0.8).dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = check.date.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Text(
            text ="Чаевые: ${tipAmount(check.sum, check.tip)} (${check.tip}%)",
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 5.dp)
        )

        Text(
            text ="На оплату: ${totalWithTip(check.sum, check.tip)}",
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 5.dp)
        )

        Text(
            text ="С человека: ${perPerson(check.sum, check.tip, check.people)}",
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 5.dp)
        )

        if (showBackToEdit) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onBackToEdit

            ) {
                Text(
                    text = "Изменить последнюю операцию",
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    color = Color.Black
                )
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNewCalculation
        ) {
            Text(
                text = "Новая операция",
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                color = Color.Black
            )
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onLoadHistory
        ) {
            Text(
                text = "История",
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                color = Color.Black
            )
        }
    }
}
