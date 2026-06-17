package pe.edu.upc.rent2go_kotlin.common.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pe.edu.upc.rent2go_kotlin.iam.presentation.LoginScreen
import pe.edu.upc.rent2go_kotlin.iam.presentation.SignUpScreen
import pe.edu.upc.rent2go_kotlin.iam.presentation.AccountTypeScreen
import pe.edu.upc.rent2go_kotlin.iam.presentation.ValidationScreen
import pe.edu.upc.rent2go_kotlin.iam.presentation.ForgotPasswordScreen
import pe.edu.upc.rent2go_kotlin.iam.presentation.AuthViewModel
import pe.edu.upc.rent2go_kotlin.catalog.presentation.MainDashboard
import pe.edu.upc.rent2go_kotlin.catalog.presentation.CarDetailScreen
import pe.edu.upc.rent2go_kotlin.booking.presentation.ChatDetailScreen
import pe.edu.upc.rent2go_kotlin.booking.presentation.BookingConfirmationScreen

@Composable
fun SetupNavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()

    // Verificar si hay token guardado (sincrónico) para decidir la ruta inicial.
    // El AuthViewModel.init() llamará a /api/v1/auth/me para hidratar los datos frescos.
    val hasSession = pe.edu.upc.rent2go_kotlin.common.SessionManager.isUserLoggedIn()
    val startDestination = if (authViewModel.currentUser != null || hasSession) "car_list" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = "login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginClick = {
                    navController.navigate("car_list") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSignUpClick = {
                    authViewModel.clearError()
                    navController.navigate("sign_up_data")
                },
                onForgotPasswordClick = {
                    authViewModel.clearError()
                    navController.navigate("forgot_password")
                }
            )
        }
        composable(route = "sign_up_data") {
            SignUpScreen(
                viewModel = authViewModel,
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
                viewModel = authViewModel,
                onContinueClick = {
                    navController.navigate("sign_up_validation")
                }
            )
        }
        composable(route = "sign_up_validation") {
            ValidationScreen(
                viewModel = authViewModel,
                onFinishClick = {
                    navController.navigate("car_list") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable(route = "forgot_password") {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onResetSuccess = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }
        composable(route = "car_list") {
            MainDashboard(
                authViewModel = authViewModel,
                onCarClick = { carId ->
                    navController.navigate("car_detail/$carId")
                },
                onChatClick = { userName ->
                    navController.navigate("chat_detail/$userName")
                },
                onLogoutClick = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
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
