package com.example.cinematest.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StartScreen(startTest: () -> Unit){
    Column(
        Modifier.width((LocalConfiguration.current.screenWidthDp * 0.7).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Квиз по Ван-Пису",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Text(
            text = "Проверьте свои знания",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Button(onClick = startTest) {
            Text(text = "Начать", fontSize = 20.sp)
        }
    }
}