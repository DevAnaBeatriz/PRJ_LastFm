package com.example.lastfm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lastfm.ui.theme.view.AddScrobble
import com.example.lastfm.ui.theme.view.EditProfile
import com.example.lastfm.ui.theme.view.EditScrobble
import com.example.lastfm.ui.theme.view.Home
import com.example.lastfm.ui.theme.view.LoadScreen
import com.example.lastfm.ui.theme.view.Login
import com.example.lastfm.ui.theme.view.ManageScrobble
import com.example.lastfm.ui.theme.view.Scrobbles
import com.example.lastfm.ui.theme.view.SignUp
import com.example.lastfm.ui.theme.view.ViewProfile
import com.example.lastfm.ui.theme.view.ViewScrobble


@Composable
fun SetupNavigation(navController: NavHostController) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "load") {
        composable("load"){
            LoadScreen(navController = navController)
        }
        composable("login") {
            Login(navController = navController)
        }
        composable("sign_up") {
            SignUp(navController = navController)
        }
        composable("home") {
            Home(navController = navController)
        }
        composable("view_profile") {
            ViewProfile(navController = navController)
        }
        composable("edit_profile") {
            EditProfile(navController = navController)
        }
        composable("scrobbles"){
            Scrobbles(navController)
        }
        composable("add_scrobble") {
            AddScrobble(navController = navController)
        }
        composable("scrobble_detail/{scrobbleId}") { backStackEntry ->
            val scrobbleId = backStackEntry.arguments?.getString("scrobbleId")
            ViewScrobble(navController = navController, scrobbleId = scrobbleId.toString())
        }
        composable("manage_scrobble/{scrobbleId}") { backStackEntry ->
            val scrobbleId = backStackEntry.arguments?.getString("scrobbleId")
            ManageScrobble(navController = navController, scrobbleId = scrobbleId.toString())
        }
        composable("edit_scrobble/{scrobbleId}"){backStackEntry ->
            val scrobbleId = backStackEntry.arguments?.getString("scrobbleId")
            EditScrobble(navController = navController, scrobbleId = scrobbleId.toString())
        }
    }
}
