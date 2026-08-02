package com.example.twitchtest.presentation.screens.mainstream.content.utils

import androidx.compose.ui.graphics.Color
import com.example.twitchtest.domain.model.StreamStatus

internal fun StreamStatus.toColor(): Color = when (this) {
    StreamStatus.OFFLINE -> Color.Gray
    StreamStatus.CONNECTING -> Color(0xFFFFA000)
    StreamStatus.ONLINE -> Color(0xFF2E7D32)
    StreamStatus.RECONNECTING -> Color(0xFFFF6F00)
}

internal fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, remainingSeconds)
}

