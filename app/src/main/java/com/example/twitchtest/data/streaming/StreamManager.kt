package com.example.twitchtest.data.streaming

import android.util.Log
import com.example.twitchtest.domain.model.StreamStatus
import com.pedro.common.ConnectChecker
import com.pedro.encoder.input.video.CameraOpenException
import com.pedro.library.rtmp.RtmpCamera2
import com.pedro.library.view.OpenGlView
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.seconds

class StreamManager @Inject constructor() : ConnectChecker {

    private companion object {
        const val TAG = "StreamManager"
    }

    private var rtmpCamera2: RtmpCamera2? = null
    private var currentOpenGlView: OpenGlView? = null

    private val _streamStatus = MutableStateFlow(StreamStatus.OFFLINE)
    val streamStatus: StateFlow<StreamStatus> = _streamStatus.asStateFlow()

    private val _streamDuration = MutableStateFlow(0L)
    val streamDuration: StateFlow<Long> = _streamDuration.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isFrontCamera = MutableStateFlow(true)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private var timerJob: Job? = null
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    /** The last RTMP URL used, so we can auto-reconnect after surface recreation. */
    private var lastRtmpUrl: String? = null

    /** Whether the user intends to be streaming (survives surface destroy/create). */
    private var streamingIntended = false

    fun initialize(openGlView: OpenGlView) {
        currentOpenGlView = openGlView
        val currentCamera = rtmpCamera2
        if (currentCamera == null) {
            rtmpCamera2 = RtmpCamera2(openGlView, this)
        } else {
            currentCamera.replaceView(openGlView)
        }
    }

    fun startPreview() {
        rtmpCamera2?.runCatching { startPreview() }
    }

    fun stopPreview() {
        rtmpCamera2?.runCatching { stopPreview() }
    }

    /**
     * Called when the OpenGlView surface is recreated (e.g. after returning from background).
     * Re-initializes the camera with the view and restarts preview.
     * If a stream was active, re-prepares and resumes streaming.
     */
    fun onSurfaceRecreated(openGlView: OpenGlView) {
        Log.i(TAG, "Surface recreated, streamingIntended=$streamingIntended")
        // Stop old camera cleanly before re-init
        val camera = rtmpCamera2
        if (camera != null) {
            if (camera.isStreaming) {
                camera.stopStream()
            }
            runCatching { camera.stopPreview() }
        }
        // Re-create camera with fresh OpenGL context
        rtmpCamera2 = RtmpCamera2(openGlView, this)
        currentOpenGlView = openGlView

        // Restart preview
        rtmpCamera2?.runCatching { startPreview() }

        // Re-apply mute state
        if (_isMuted.value) {
            rtmpCamera2?.disableAudio()
        }

        // If the user was streaming, reconnect
        if (streamingIntended && lastRtmpUrl != null) {
            Log.i(TAG, "Reconnecting stream after surface recreation")
            val cam = rtmpCamera2 ?: return
            val isPrepared = cam.prepareAudio() && cam.prepareVideo()
            if (isPrepared) {
                _streamStatus.value = StreamStatus.CONNECTING
                cam.startStream(lastRtmpUrl!!)
            } else {
                _streamStatus.value = StreamStatus.OFFLINE
                streamingIntended = false
                scope.launch {
                    _errorMessage.emit(
                        "Failed to re-prepare camera after returning from background."
                    )
                }
            }
        }
    }

    fun startStream(rtmpUrl: String) {
        Log.i(TAG, "startStream called")

        val camera = rtmpCamera2 ?: return
        if (camera.isStreaming) return

        val isPrepared = camera.prepareAudio() && camera.prepareVideo()
        if (!isPrepared) {
            _streamStatus.value = StreamStatus.OFFLINE
            scope.launch {
                _errorMessage.emit("Unable to prepare camera or microphone for streaming.")
            }
            return
        }

        lastRtmpUrl = rtmpUrl
        streamingIntended = true
        _streamStatus.value = StreamStatus.CONNECTING
        camera.startStream(rtmpUrl)
    }

    fun stopStream() {
        streamingIntended = false
        lastRtmpUrl = null
        val camera = rtmpCamera2
        if (camera?.isStreaming == true) {
            camera.stopStream()
        }
        _streamStatus.value = StreamStatus.OFFLINE
        _streamDuration.value = 0L
        stopTimer()
    }

    fun switchCamera() {
        val camera = rtmpCamera2 ?: return
        runCatching {
            camera.switchCamera()
            _isFrontCamera.value = !_isFrontCamera.value
        }.getOrElse { throwable ->
            if (throwable !is CameraOpenException) throw throwable
        }
    }

    fun toggleMicrophone() {
        val camera = rtmpCamera2 ?: return
        if (_isMuted.value) {
            camera.enableAudio()
        } else {
            camera.disableAudio()
        }
        _isMuted.value = !_isMuted.value
    }

    fun isStreaming(): Boolean = rtmpCamera2?.isStreaming == true

    /**
     * Stops the active stream and preview, cancels the internal scope.
     * After calling this the instance must not be reused.
     */
    fun release() {
        streamingIntended = false
        lastRtmpUrl = null
        stopStream()
        stopPreview()
        rtmpCamera2 = null
        currentOpenGlView = null
        scope.cancel()
    }

    override fun onConnectionStarted(url: String) {
        _streamStatus.value = StreamStatus.CONNECTING
    }

    override fun onConnectionSuccess() {
        _streamStatus.value = StreamStatus.ONLINE
        startTimer()
    }

    override fun onConnectionFailed(reason: String) {
        streamingIntended = false
        // Reset library internal state so startStream can be called again
        rtmpCamera2?.runCatching { stopStream() }
        _streamStatus.value = StreamStatus.OFFLINE
        stopTimer()
        scope.launch {
            _errorMessage.emit(reason)
        }
    }

    override fun onDisconnect() {
        streamingIntended = false
        _streamStatus.value = StreamStatus.OFFLINE
        stopTimer()
    }

    override fun onAuthError() {
        _streamStatus.value = StreamStatus.OFFLINE
        stopTimer()
        scope.launch {
            _errorMessage.emit("Authentication failed. Check your stream key.")
        }
    }

    override fun onAuthSuccess() = Unit

    override fun onNewBitrate(bitrate: Long) = Unit

    private fun startTimer() {
        _streamDuration.value = 0L
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && _streamStatus.value == StreamStatus.ONLINE && isStreaming()) {
                delay(1.seconds)
                if (_streamStatus.value == StreamStatus.ONLINE && isStreaming()) {
                    _streamDuration.value += 1
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _streamDuration.value = 0L
    }
}
