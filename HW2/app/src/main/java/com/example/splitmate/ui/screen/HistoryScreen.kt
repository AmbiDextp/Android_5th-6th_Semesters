package com.example.splitmate.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.splitmate.model.Check

@Composable
fun HistoryScreen(
    checks: List<Check>,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .width((LocalConfiguration.current.screenWidthDp * 0.8).dp)
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (checks.isEmpty()) {
            Text(
                text = "Нет операций",
                modifier = Modifier.padding(16.dp)
            )
        } else {
            checks.forEach { check ->
                HistoryItem(
                    check = check,
                    onClick = { onSelect(check.id) }
                )
            }
        }
    }
}


@Composable
fun HistoryItem(
    check: Check,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.width((LocalConfiguration.current.screenWidthDp * 0.8).dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = check.date.toString(), fontWeight = FontWeight.Bold)
                Text(text = "Сумма: ${check.sum}")
                Text(text = "Персон: ${check.people}")
                Text(text = "Чаевые: ${check.tip}")
            }
        }
    }
}