package com.example.lastfm.ui.theme.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.example.lastfm.R

@Composable
fun LoadScreen(navController: NavController) {
    LaunchedEffect(key1 = true) {
        delay(750)
        navController.navigate("login") {
            popUpTo("load") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_login),
            contentDescription = "Logo",
            modifier = Modifier.size(300.dp)
        )
    }
}