package com.example.lastfm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lastfm.ui.theme.LASTFMTheme
import androidx.navigation.compose.rememberNavController
import com.example.lastfm.navigation.SetupNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LASTFMTheme {

                val navController = rememberNavController()

                Surface(color = MaterialTheme.colorScheme.background) {
                    SetupNavigation(navController)
                }
            }
        }
    }
}
