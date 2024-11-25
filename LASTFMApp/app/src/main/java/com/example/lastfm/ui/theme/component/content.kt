package com.example.tudohorrorosoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun content(navController: NavController, isDrawerOpen: Boolean) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF9E0505))
            .padding(all = 20.dp)

    ) {
        Text(
            text = "Opções",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color.White),
            modifier = Modifier.padding(top = 20.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        item(text = "Início", onClick = { navController.navigate("home") })
        item(text = "Músicas", onClick = { navController.navigate("scrobbles") })
        item(text = "Meu Perfil", onClick = { navController.navigate("view_profile") })
    }
}
