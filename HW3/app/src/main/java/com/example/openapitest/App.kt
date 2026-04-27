package com.example.openapitest

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.openapitest.ui.screen.FruitListScreen
import com.example.openapitest.ui.screen.FruitDetailScreen
import com.example.openapitest.ui.viewmodel.FruitListViewModel

sealed class Router(val route: String) {
    data object List : Router("list")

    data object Detail : Router("detail/{id}") {
        const val ARG_ID = "id"
        fun createRoute(id: Int): String = "detail/$id"
    }
}

@Composable
fun App() {
    val viewModel: FruitListViewModel = viewModel()
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Router.List.route) {
        composable(route = Router.List.route) {
            FruitListScreen(
                query = viewModel.uiState.query,
                filter = viewModel.uiState.filter,
                isLoading = viewModel.uiState.isLoading,
                errorMessage = viewModel.uiState.errorMessage,
                fruits = viewModel.visibleFruits,
                favourites = viewModel.uiState.favourites,
                onSearchChange = viewModel::onQueryChange,
                onFilterChange = viewModel::onFilterChange,
                onToggleFavourite = viewModel::onToggleFavourite,
                onRefresh = viewModel::loadFruits,
                onItemClick = { navController.navigate(Router.Detail.createRoute(it)) },
                onRetry = viewModel::loadFruits
            )
        }

        composable(
            route = Router.Detail.route,
            arguments = listOf(navArgument(Router.Detail.ARG_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt(Router.Detail.ARG_ID)
            val fruit = id?.let(viewModel::fruitById)

            if (fruit == null) {
                Text("Fruit not found")
            } else {
                FruitDetailScreen(
                    state = fruit,
                    isFavourite = fruit.id in viewModel.uiState.favourites,
                    onBack = { navController.popBackStack() },
                    onToggleFavourite = { viewModel.onToggleFavourite(fruit.id) })
            }
        }
    }
}