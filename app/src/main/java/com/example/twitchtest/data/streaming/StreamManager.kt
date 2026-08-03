package com.example.twitchtest.data.streaming

import com.example.twitchtest.domain.model.StreamStatus
import com.pedro.library.view.OpenGlView
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Manages the camera preview and RTMP streaming lifecycle for the main stream screen.
 */
interface StreamManager {

    /** Current connection status of the stream session. */
    val streamStatus: StateFlow<StreamStatus>

    /** Stream duration in seconds while the status is [StreamStatus.ONLINE]. */
    val streamDuration: StateFlow<Long>

    /** Whether the microphone is muted. */
    val isMuted: StateFlow<Boolean>

    /** Whether the front camera is currently active. */
    val isFrontCamera: StateFlow<Boolean>

    /** User-visible error messages emitted by the streaming stack. */
    val errorMessage: SharedFlow<String>

    /**
     * Creates or rebinds the internal camera engine to the provided [OpenGlView].
     */
    fun initialize(openGlView: OpenGlView)

    /** Starts camera preview rendering if the camera engine is initialized. */
    fun startPreview()

    /** Stops camera preview rendering if it is currently running. */
    fun stopPreview()

    /**
     * Reinitializes preview/stream state after the underlying surface is recreated.
     */
    fun onSurfaceRecreated(openGlView: OpenGlView)

    /** Starts streaming to the provided RTMP endpoint URL. */
    fun startStream(rtmpUrl: String)

    /** Stops the active RTMP stream and resets stream state. */
    fun stopStream()

    /** Switches between front and back camera when available. */
    fun switchCamera()

    /** Toggles microphone mute state. */
    fun toggleMicrophone()

    /** Returns whether an RTMP stream is currently active. */
    fun isStreaming(): Boolean

    /** Releases camera/streaming resources permanently for this instance. */
    fun release()
}
