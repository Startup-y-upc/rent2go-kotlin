package pe.edu.upc.rent2go_kotlin.common.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pe.edu.upc.rent2go_kotlin.iam.presentation.LoginScreen
import pe.edu.upc.rent2go_kotlin.iam.presentation.SignUpScreen
import pe.edu.upc.rent2go_kotlin.iam.presentation.AccountTypeScreen
import pe.edu.upc.rent2go_kotlin.iam.presentation.ValidationScreen
import pe.edu.upc.rent2go_kotlin.catalog.presentation.MainDashboard
import pe.edu.upc.rent2go_kotlin.catalog.presentation.CarDetailScreen
import pe.edu.upc.rent2go_kotlin.booking.presentation.ChatDetailScreen
import pe.edu.upc.rent2go_kotlin.booking.presentation.BookingConfirmationScreen

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable(route = "login") {
            LoginScreen(
                onLoginClick = {
                    navController.navigate("car_list") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSignUpClick = {
                    navController.navigate("sign_up_data")
                }
            )
        }
        composable(route = "sign_up_data") {
            SignUpScreen(
                onContinueClick = {
                    navController.navigate("sign_up_type")
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(route = "sign_up_type") {
            AccountTypeScreen(
                onContinueClick = {
                    navController.navigate("sign_up_validation")
                }
            )
        }
        composable(route = "sign_up_validation") {
            ValidationScreen(
                onFinishClick = {
                    navController.navigate("car_list") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable(route = "car_list") {
            MainDashboard(
                onCarClick = { carId ->
                    navController.navigate("car_detail/$carId")
                },
                onChatClick = { userName ->
                    navController.navigate("chat_detail/$userName")
                }
            )
        }
        composable(
            route = "chat_detail/{userName}",
            arguments = listOf(navArgument("userName") { type = NavType.StringType })
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            ChatDetailScreen(
                userName = userName,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = "car_detail/{carId}",
            arguments = listOf(navArgument("carId") { type = NavType.IntType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getInt("carId") ?: 0
            CarDetailScreen(
                carId = carId,
                onBackClick = {
                    navController.popBackStack()
                },
                onReserveClick = { id ->
                    navController.navigate("booking_confirmation/$id")
                }
            )
        }
        composable(
            route = "booking_confirmation/{carId}",
            arguments = listOf(navArgument("carId") { type = NavType.IntType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getInt("carId") ?: 0
            BookingConfirmationScreen(
                carId = carId,
                onBackClick = {
                    navController.popBackStack()
                },
                onPaymentClick = {
                    navController.navigate("car_list") {
                        popUpTo("car_list") { inclusive = true }
                    }
                }
            )
        }
    }
}
