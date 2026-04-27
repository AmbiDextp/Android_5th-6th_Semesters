package com.example.openapitest.ui.widget

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.openapitest.ui.viewmodel.FruitFilter

@Composable
fun FilterRow(
    filter: FruitFilter,
    onFilterChange: (FruitFilter) -> Unit
) {
    Row {
        TextButton(
            onClick = { onFilterChange(FruitFilter.ALL) },
            enabled = filter != FruitFilter.ALL
        ) {
            Text(if (filter == FruitFilter.ALL) "• All" else "All")
        }
        TextButton(
            onClick = { onFilterChange(FruitFilter.FAVOURITES) },
            enabled = filter != FruitFilter.FAVOURITES
        ) {
            Text(if (filter == FruitFilter.FAVOURITES) "• Favourites" else "Favourites")
        }
    }
}