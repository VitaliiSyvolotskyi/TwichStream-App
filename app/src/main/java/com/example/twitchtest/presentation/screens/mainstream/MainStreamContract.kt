package com.example.twitchtest.presentation.screens.mainstream

import com.example.twitchtest.domain.model.StreamStatus
import com.pedro.library.view.OpenGlView

data class MainStreamState(
    val streamStatus: StreamStatus = StreamStatus.OFFLINE,
    val duration: Long = 0L,
    val isMuted: Boolean = false,
    val isFrontCamera: Boolean = true,
    val streamKey: String = ""
) {
    val isStreaming =
        streamStatus == StreamStatus.ONLINE || streamStatus == StreamStatus.CONNECTING
}

sealed interface MainStreamIntent {
    data class InitializeStream(val openGlView: OpenGlView) : MainStreamIntent
    data object StartPreview : MainStreamIntent
    data object StopPreview : MainStreamIntent
    data class SurfaceRecreated(val openGlView: OpenGlView) : MainStreamIntent
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
