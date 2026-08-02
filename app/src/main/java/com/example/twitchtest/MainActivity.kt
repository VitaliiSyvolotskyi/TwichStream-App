package com.example.twitchtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.twitchtest.presentation.navigation.AppNavHost
import com.example.twitchtest.ui.theme.TwitchTestTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TwitchTestTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
}
