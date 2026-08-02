package com.example.twitchtest.presentation.screens.mainstream.content.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.twitchtest.presentation.screens.mainstream.MainStreamIntent

@Composable
internal fun ControlBar(
    isStreaming: Boolean,
    isMuted: Boolean,
    isFrontCamera: Boolean,
    onIntent: (MainStreamIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val fabColor by animateColorAsState(
        targetValue = if (isStreaming) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(300),
        label = "fabColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MicButton(
            isMuted = isMuted,
            onClick = { onIntent(MainStreamIntent.ToggleMicrophone) }
        )

        StreamToggleButton(
            isStreaming = isStreaming,
            containerColor = fabColor,
            onClick = {
                if (isStreaming) onIntent(MainStreamIntent.StopStream)
                else onIntent(MainStreamIntent.StartStream)
            }
        )

        CameraSwitchButton(
            isFrontCamera = isFrontCamera,
            onClick = { onIntent(MainStreamIntent.SwitchCamera) }
        )
    }
}

