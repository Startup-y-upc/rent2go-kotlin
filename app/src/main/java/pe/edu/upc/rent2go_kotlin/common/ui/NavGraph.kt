package pe.edu.upc.rent2go_kotlin.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import pe.edu.upc.rent2go_kotlin.booking.presentation.BookingDetailScreen
import pe.edu.upc.rent2go_kotlin.community.presentation.TermsScreen
import pe.edu.upc.rent2go_kotlin.notifications.presentation.NotificationsScreen

@Composable
fun SetupNavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()

    // Verificar si hay token guardado
    val startDestination = remember {
        val hasSession = pe.edu.upc.rent2go_kotlin.common.SessionManager.isUserLoggedIn()
        if (authViewModel.currentUser != null || hasSession) "car_list" else "login"
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        pe.edu.upc.rent2go_kotlin.common.SessionEventBus.events.collect { event ->
            if (event == pe.edu.upc.rent2go_kotlin.common.SessionEvent.SESSION_EXPIRED) {
                authViewModel.logout {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

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
                    authViewModel.clearRegistrationFields()
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
        composable(
            route = "sign_up_validation?fromProfile={fromProfile}",
            arguments = listOf(navArgument("fromProfile") { type = NavType.BoolType; defaultValue = false })
        ) { backStackEntry ->
            val fromProfile = backStackEntry.arguments?.getBoolean("fromProfile") ?: false
            ValidationScreen(
                viewModel = authViewModel,
                onFinishClick = {
                    if (fromProfile) {
                        navController.popBackStack()
                    } else {
                        navController.navigate("car_list") {
                            popUpTo("login") { inclusive = true }
                        }
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
                onChatClick = { conversationId ->
                    navController.navigate("chat_detail/$conversationId")
                },
                onLogoutClick = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBookingClick = { bookingId ->
                    navController.navigate("booking_detail/$bookingId")
                },
                onKycClick = {
                    navController.navigate("sign_up_validation?fromProfile=true")
                },
                onTermsClick = {
                    navController.navigate("terms")
                },
                onNotificationsClick = {
                    navController.navigate("notifications")
                }
            )
        }
        composable(route = "terms") {
            TermsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(route = "notifications") {
            NotificationsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = "chat_detail/{conversationId}",
            arguments = listOf(navArgument("conversationId") { type = NavType.IntType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getInt("conversationId") ?: 0
            ChatDetailScreen(
                conversationId = conversationId,
                reservationId = null,
                userName = "Conversación",
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
        composable(
            route = "booking_detail/{bookingId}",
            arguments = listOf(navArgument("bookingId") { type = NavType.IntType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getInt("bookingId") ?: 0
            BookingDetailScreen(
                bookingId = bookingId,
                onBackClick = {
                    navController.popBackStack()
                },
                onChatClick = { conversationId ->
                    navController.navigate("chat_detail/$conversationId")
                }
            )
        }
    }
}
