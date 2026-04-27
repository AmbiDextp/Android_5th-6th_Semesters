package com.example.cinematest.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cinematest.R

data class Icon (
    val iconResId: Int,
    val comment: String,
)

@Composable
fun ResultScreen(correct: Int, size: Int, startTest: () -> Unit) {
    val result: Int = (correct.toDouble() / size * 100).toInt()
    val data = when (result) {
        in 0..50 -> Icon(
            iconResId = R.drawable.ic_3,
            comment = "Мда, маловато...",
        )
        in 51..80 -> Icon(
            iconResId = R.drawable.ic_2,
            comment = "Неплохо, можно лучше.",
        )
        in 81..100 -> Icon(
            iconResId = R.drawable.ic_1,
            comment = "Молодец!",
        )
        else -> Icon(
            iconResId = 1,
            comment = "",
        )
    }


    Column(
        Modifier.width((LocalConfiguration.current.screenWidthDp * 0.7).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Результаты",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Text(
            text ="Вы ответили правильно на ${correct} из ${size}\n(${result}%)",
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Image(
            painter = painterResource(id = data.iconResId),
            contentDescription = "",
            modifier = Modifier
                .size(240.dp)
                .padding(bottom = 10.dp)
        )
//        Text(
//            text = data.comment,
//            fontSize = 20.sp,
//            textAlign = TextAlign.Center,
//            modifier = Modifier.padding(bottom = 10.dp)
//        )


        Button(onClick = startTest) {
            Text(
                text = "Вернуться на главный экран",
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}