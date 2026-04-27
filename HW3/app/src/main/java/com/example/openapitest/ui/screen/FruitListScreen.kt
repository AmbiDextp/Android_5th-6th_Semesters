package com.example.openapitest.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.openapitest.model.Fruit
import com.example.openapitest.ui.viewmodel.FruitFilter
import com.example.openapitest.ui.widget.FilterRow
import com.example.openapitest.ui.widget.FruitCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FruitListScreen(
    query: String,
    filter: FruitFilter,
    isLoading: Boolean,
    errorMessage: String?,
    fruits: List<Fruit>,
    favourites: Set<Int>,
    onSearchChange: (String) -> Unit,
    onFilterChange: (FruitFilter) -> Unit,
    onToggleFavourite: (Int) -> Unit,
    onRefresh: () -> Unit,
    onItemClick: (Int) -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Devil Fruits") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search fruits...") },
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(8.dp))

            FilterRow(
                filter = filter,
                onFilterChange = onFilterChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading fruits...")
                        }
                    }
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRetry) {
                            Text("Retry")
                        }
                    }
                }

                fruits.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No fruits found")
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items = fruits, key = { it.id }) { fruit ->
                            FruitCard(
                                fruit = fruit,
                                isFavourite = fruit.id in favourites,
                                onToggleFavourite = { onToggleFavourite(fruit.id) },
                                onClick = { onItemClick(fruit.id) }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }
}