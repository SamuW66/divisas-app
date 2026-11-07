package com.example.divisas.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.divisas.ServiceLocator
import com.example.divisas.data.model.Role
import com.example.divisas.ui.admin.AdminScreen
import com.example.divisas.ui.home.HomeScreen
import com.example.divisas.ui.login.LoginScreen
import com.example.divisas.ui.profile.ProfileScreen
import com.example.divisas.ui.register.RegisterScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val ADMIN = "admin"
}

private fun NavHostController.logout() {
    ServiceLocator.authRepo.logout()
    navigate(Routes.LOGIN) { popUpTo(graph.id) { inclusive = true } }
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = { role ->
                    val dest = if (role == Role.ADMIN) Routes.ADMIN else Routes.HOME
                    nav.navigate(dest) { popUpTo(Routes.LOGIN) { inclusive = true } }
                },
                onRegister = { nav.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegistered = { nav.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } } },
                onBack = { nav.popBackStack() }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(onProfile = { nav.navigate(Routes.PROFILE) })
        }
        composable(Routes.PROFILE) {
            ProfileScreen(onBack = { nav.popBackStack() }, onLogout = { nav.logout() })
        }
        composable(Routes.ADMIN) {
            AdminScreen(onLogout = { nav.logout() })
        }
    }
}
