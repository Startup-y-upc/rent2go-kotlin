package pe.edu.upc.rent2go_kotlin.common.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pe.edu.upc.rent2go_kotlin.iam.presentation.LoginScreen
import pe.edu.upc.rent2go_kotlin.iam.presentation.SignUpScreen
import pe.edu.upc.rent2go_kotlin.iam.presentation.AccountTypeScreen
import pe.edu.upc.rent2go_kotlin.iam.presentation.ValidationScreen
import pe.edu.upc.rent2go_kotlin.iam.presentation.ForgotPasswordScreen
import pe.edu.upc.rent2go_kotlin.iam.presentation.AuthViewModel

@Composable
fun SetupNavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()

    val startDestination = if (authViewModel.currentUser != null) "car_list" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = "login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginClick = {
                    // El éxito se puede manejar dentro de la pantalla o redirigiendo
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
                    navController.navigate("login") {
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
    }
}
