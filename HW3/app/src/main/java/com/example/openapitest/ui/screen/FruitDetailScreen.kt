package com.example.openapitest.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.openapitest.model.Fruit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FruitDetailScreen(
    state: Fruit,
    isFavourite: Boolean,
    onBack: () -> Unit,
    onToggleFavourite: () -> Unit,
) {
    Scaffold( topBar = { TopAppBar(title = { Text("Detail") }) })
    { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            TextButton(onClick = onBack) { Text("Назад") }

            Spacer(Modifier.height(8.dp))

            Text("Name: ${state.name}", fontWeight = FontWeight.Bold)
            Image(
                painter = rememberAsyncImagePainter(state.img),
                contentDescription = "Изображение фрукта",
                modifier = Modifier.size(350.dp)
                )
            Text("Type: ${state.type}")
            Text("Description: ${state.description}")

            Spacer(Modifier.height(12.dp))

            Row {
                Text("Favourite: ")
                Text(
                    text = if (isFavourite) "+" else "-",
                    modifier = Modifier.clickable(onClick = onToggleFavourite),
                    fontSize = 32.sp
                )
            }
        }
    }
}
