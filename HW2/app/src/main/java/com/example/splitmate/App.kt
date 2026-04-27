package com.example.splitmate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.splitmate.ui.screen.HistoryScreen
import com.example.splitmate.ui.screen.CalculateScreen
import com.example.splitmate.ui.screen.MainScreen
import com.example.splitmate.ui.screen.ResultScreen
import com.example.splitmate.ui.viewmodel.SplitMateViewModel
import kotlin.collections.listOf


sealed class Router(val route: String) {

    data object Home : Router("home")
    data object Input : Router("input/{changeLast}") {
        const val ARG_CHANGELAST = "changeLast"
        fun createRoute(changeLast: Boolean): String = "input/$changeLast"
    }

    data object Result : Router("result/{id}") {
        const val ARG_ID = "id"
        fun createRoute(id: Int): String = "result/$id"
    }

    data object History : Router("history")
}

@Composable
fun App() {
    val holder: SplitMateViewModel = viewModel()
    val navController: NavHostController = rememberNavController()

    Column(
        Modifier.width((LocalConfiguration.current.screenWidthDp * 0.8).dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NavHost(navController = navController, startDestination = Router.Home.route) {
            composable(route = Router.Home.route) {
                MainScreen(
                    {holder.resetState()
                    navController.navigate(Router.Input.createRoute(false))},
                    { navController.navigate(Router.History.route) }
                )
            }

            composable(
                route = Router.Input.route,
                arguments = listOf(navArgument(Router.Input.ARG_CHANGELAST) {
                    type = NavType.BoolType
                })
            ) { backStackEntry ->
                val changeLast: Boolean? = backStackEntry.arguments?.getBoolean(Router.Input.ARG_CHANGELAST)
                if (changeLast != null) {
                    CalculateScreen(
                        holder.uiState,
                        holder.validSum,
                        holder.validPeople,
                        holder.validTip,
                        holder::changeSum,
                        holder::changePeople,
                        holder::changeTip,
                        { holder.saveCheck(changeLast) }
                    ) {
                        navController.navigate(Router.Result.createRoute(
                            holder.checks.size - 1))
                    }
                }
            }

            composable(
                route = Router.Result.route,
                arguments = listOf(navArgument(Router.Result.ARG_ID) {
                    type = NavType.IntType
                })) { backStackEntry ->
                val id: Int? = backStackEntry.arguments?.getInt(Router.Result.ARG_ID)
                if (id != null) {
                    ResultScreen(
                        holder.checks[id],
                        holder::tipAmount,
                        holder::totalSum,
                        holder::perPerson,
                        navController.previousBackStackEntry?.destination?.route !=
                                Router.History.route,
                        { navController.popBackStack() },
                        {
                            holder.resetState()
                            navController.navigate(Router.Input.createRoute(false))
                        },
                        { navController.navigate(Router.History.route) }
                    )
                }
            }

            composable(route = Router.History.route) {
                HistoryScreen(
                    holder.checks
                ) { id -> navController.navigate(Router.Result.createRoute(id)) }
            }
        }
    }
}
