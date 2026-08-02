package com.example.twitchtest.presentation.screens.mainstream.content.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.example.twitchtest.R

@Composable
fun CameraSwitchButton(
    isFrontCamera: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
    ) {
        Crossfade(
            targetState = isFrontCamera,
            animationSpec = tween(200),
            label = "camera"
        ) { front ->
            Icon(
                imageVector = if (front) Icons.Filled.CameraRear else Icons.Filled.CameraFront,
                contentDescription = if (front) stringResource(R.string.switch_to_back_camera)
                else stringResource(R.string.switch_to_front_camera)
            )
        }
    }
}

