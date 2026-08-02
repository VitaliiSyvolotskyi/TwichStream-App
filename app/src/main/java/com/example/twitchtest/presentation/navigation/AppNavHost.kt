package com.example.twitchtest.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.twitchtest.presentation.mainstream.MainStreamScreen
import com.example.twitchtest.presentation.streamconfig.StreamConfigScreen

private const val ANIM_DURATION = 400

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = StreamConfigRoute,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(ANIM_DURATION)
            ) + scaleIn(tween(ANIM_DURATION), initialScale = 0.92f)
        },
        exitTransition = {
            ExitTransition.None
        },
        popEnterTransition = {
            EnterTransition.None
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(ANIM_DURATION)
            ) + scaleOut(tween(ANIM_DURATION), targetScale = 0.92f)
        }
    ) {
        composable<StreamConfigRoute> {
            StreamConfigScreen(
                onNavigateToStream = {
                    navController.navigate(MainStreamRoute) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<MainStreamRoute> {
            MainStreamScreen()
        }
    }
}


