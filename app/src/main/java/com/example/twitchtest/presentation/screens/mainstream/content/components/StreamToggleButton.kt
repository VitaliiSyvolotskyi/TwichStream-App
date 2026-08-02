package com.example.twitchtest.presentation.screens.mainstream.content.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.twitchtest.R

@Composable
fun StreamToggleButton(
    isStreaming: Boolean,
    containerColor: Color,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = containerColor
    ) {
        Crossfade(
            targetState = isStreaming,
            animationSpec = tween(200),
            label = "stream"
        ) { live ->
            Icon(
                imageVector = if (live) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                contentDescription = if (live) stringResource(R.string.stop_stream)
                else stringResource(R.string.start_stream)
            )
        }
    }
}

