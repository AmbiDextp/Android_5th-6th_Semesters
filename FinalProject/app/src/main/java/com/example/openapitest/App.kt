package com.example.openapitest

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.openapitest.ui.screen.catalog.CatalogScreen
import com.example.openapitest.ui.screen.collections.CollectionDetailScreen
import com.example.openapitest.ui.screen.collections.CollectionsScreen
import com.example.openapitest.ui.screen.detail.FruitDetailScreen
import com.example.openapitest.ui.screen.history.HistoryScreen
import com.example.openapitest.ui.screen.settings.SettingsScreen

sealed class Router(val route: String) {
    data object Catalog : Router("catalog")
    data object Collections : Router("collections")
    data object History : Router("history")
    data object Settings : Router("settings")

    data object Detail : Router("detail/{id}") {
        const val ARG_ID = "id"
        fun createRoute(id: Int): String = "detail/$id"
    }

    data object CollectionDetail : Router("collection/{id}") {
        const val ARG_ID = "id"
        fun createRoute(id: Long): String = "collection/$id"
    }
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer is not provided")
}

private data class TopLevelDestination(
    val router: Router,
    val label: String,
    val icon: ImageVector
)

@Composable
fun App(container: AppContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val topLevel = listOf(
        TopLevelDestination(Router.Catalog, "Каталог", Icons.AutoMirrored.Filled.List),
        TopLevelDestination(Router.Collections, "Коллекции", Icons.Filled.CollectionsBookmark),
        TopLevelDestination(Router.History, "История", Icons.Filled.History),
        TopLevelDestination(Router.Settings, "Настройки", Icons.Filled.Settings)
    )
    val showBottomBar = topLevel.any { it.router.route == currentRoute }

    CompositionLocalProvider(LocalAppContainer provides container) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        topLevel.forEach { dest ->
                            NavigationBarItem(
                                selected = currentRoute == dest.router.route,
                                onClick = {
                                    navController.navigate(dest.router.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(dest.icon, contentDescription = dest.label) },
                                label = { Text(dest.label) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Router.Catalog.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Router.Catalog.route) {
                    CatalogScreen(
                        onFruitClick = { id ->
                            navController.navigate(Router.Detail.createRoute(id))
                        }
                    )
                }

                composable(Router.Collections.route) {
                    CollectionsScreen(
                        onCollectionClick = { id ->
                            navController.navigate(Router.CollectionDetail.createRoute(id))
                        }
                    )
                }

                composable(Router.History.route) {
                    HistoryScreen(
                        onFruitClick = { id ->
                            navController.navigate(Router.Detail.createRoute(id))
                        }
                    )
                }

                composable(Router.Settings.route) {
                    SettingsScreen()
                }

                composable(
                    route = Router.Detail.route,
                    arguments = listOf(navArgument(Router.Detail.ARG_ID) { type = NavType.IntType })
                ) { entry ->
                    val id = entry.arguments?.getInt(Router.Detail.ARG_ID)
                        ?: return@composable
                    FruitDetailScreen(
                        fruitId = id,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Router.CollectionDetail.route,
                    arguments = listOf(
                        navArgument(Router.CollectionDetail.ARG_ID) { type = NavType.LongType }
                    )
                ) { entry ->
                    val id = entry.arguments?.getLong(Router.CollectionDetail.ARG_ID)
                        ?: return@composable
                    CollectionDetailScreen(
                        collectionId = id,
                        onBack = { navController.popBackStack() },
                        onFruitClick = { fruitId ->
                            navController.navigate(Router.Detail.createRoute(fruitId))
                        }
                    )
                }
            }
        }
    }
}
