package com.example.twitchtest.presentation.screens.mainstream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.twitchtest.data.streaming.StreamManager
import com.example.twitchtest.domain.model.StreamStatus
import com.example.twitchtest.domain.usecase.GetStreamKeyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MainStreamViewModel @Inject constructor(
    private val streamManager: StreamManager,
    getStreamKeyUseCase: GetStreamKeyUseCase
) : ViewModel() {

    val state: StateFlow<MainStreamState> = combine(
        getStreamKeyUseCase(),
        streamManager.streamStatus,
        streamManager.streamDuration,
        streamManager.isMuted,
        streamManager.isFrontCamera
    ) { key, status, duration, muted, front ->
        MainStreamState(
            streamKey = key.orEmpty(),
            streamStatus = status,
            duration = duration,
            isMuted = muted,
            isFrontCamera = front
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MainStreamState()
    )

    private val _effect = MutableSharedFlow<MainStreamEffect>()
    val effect: SharedFlow<MainStreamEffect> = _effect.asSharedFlow()

    init {
        streamManager.errorMessage
            .onEach { message -> _effect.emit(MainStreamEffect.ShowError(message)) }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: MainStreamIntent) {
        when (intent) {
            is MainStreamIntent.InitializeStream -> streamManager.initialize(intent.openGlView)
            MainStreamIntent.StartPreview -> streamManager.startPreview()
            MainStreamIntent.StopPreview -> streamManager.stopPreview()
            is MainStreamIntent.SurfaceRecreated ->
                streamManager.onSurfaceRecreated(intent.openGlView)
            MainStreamIntent.StartStream -> startStream()
            MainStreamIntent.StopStream -> streamManager.stopStream()
            MainStreamIntent.ToggleMicrophone -> streamManager.toggleMicrophone()
            MainStreamIntent.SwitchCamera -> streamManager.switchCamera()
            MainStreamIntent.OpenViewerSheet -> {
                if (state.value.streamStatus == StreamStatus.ONLINE) {
                    viewModelScope.launch { _effect.emit(MainStreamEffect.ShowViewerSheet) }
                }
            }
            MainStreamIntent.CloseViewerSheet -> {
                viewModelScope.launch { _effect.emit(MainStreamEffect.HideViewerSheet) }
            }
        }
    }

    private fun startStream() {
        val key = state.value.streamKey.trim()
        if (key.isBlank()) return
        val rtmpUrl = "rtmp://live.twitch.tv/app/$key"
        streamManager.startStream(rtmpUrl)
    }

    override fun onCleared() {
        super.onCleared()
        streamManager.release()
    }
}
