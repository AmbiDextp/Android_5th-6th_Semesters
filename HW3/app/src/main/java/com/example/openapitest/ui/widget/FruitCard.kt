package com.example.openapitest.ui.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openapitest.model.Fruit

@Composable
fun FruitCard(
    fruit: Fruit,
    isFavourite: Boolean,
    onToggleFavourite: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = fruit.name, fontWeight = FontWeight.Bold)
            Text("Type: ${fruit.type}")
            Text(fruit.description.take(50) + "...")
        }
        Text(
            text = if (isFavourite) "+" else "-",
            modifier = Modifier.clickable(onClick = onToggleFavourite),
            fontSize = 32.sp
        )
    }
}