package com.example.twitchtest.presentation.screens.streamconfig

data class StreamConfigState(
    val streamKey: String = "",
    val isSaved: Boolean = false,
    val error: String? = null
)

sealed interface StreamConfigIntent {
    data class UpdateKey(val key: String) : StreamConfigIntent
    data object SaveKey : StreamConfigIntent
    data object NavigateToStream : StreamConfigIntent
}

sealed interface StreamConfigEffect {
    data object NavigateToMainStream : StreamConfigEffect
}

