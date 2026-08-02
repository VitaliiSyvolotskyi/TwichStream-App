package com.example.twitchtest.presentation.mainstream

import com.example.twitchtest.domain.model.StreamStatus

data class MainStreamState(
    val streamStatus: StreamStatus = StreamStatus.OFFLINE,
    val duration: Long = 0L,
    val isMuted: Boolean = false,
    val isFrontCamera: Boolean = true,
    val streamKey: String = ""
)

sealed interface MainStreamIntent {
    data object StartStream : MainStreamIntent
    data object StopStream : MainStreamIntent
    data object ToggleMicrophone : MainStreamIntent
    data object SwitchCamera : MainStreamIntent
    data object OpenViewerSheet : MainStreamIntent
    data object CloseViewerSheet : MainStreamIntent
}

sealed interface MainStreamEffect {
    data object ShowViewerSheet : MainStreamEffect
    data object HideViewerSheet : MainStreamEffect
    data class ShowError(val message: String) : MainStreamEffect
}

