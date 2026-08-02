package com.example.twitchtest.presentation.mainstream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.twitchtest.data.streaming.StreamManager
import com.example.twitchtest.domain.model.StreamStatus
import com.example.twitchtest.domain.usecase.GetStreamKeyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MainStreamViewModel @Inject constructor(
    private val streamManager: StreamManager,
    private val getStreamKeyUseCase: GetStreamKeyUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MainStreamState())
    val state: StateFlow<MainStreamState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<MainStreamEffect>()
    val effect: SharedFlow<MainStreamEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            getStreamKeyUseCase().collect { key ->
                _state.update { it.copy(streamKey = key.orEmpty()) }
            }
        }
        viewModelScope.launch {
            streamManager.streamStatus.collect { status ->
                _state.update { it.copy(streamStatus = status) }
            }
        }
        viewModelScope.launch {
            streamManager.streamDuration.collect { duration ->
                _state.update { it.copy(duration = duration) }
            }
        }
        viewModelScope.launch {
            streamManager.isMuted.collect { muted ->
                _state.update { it.copy(isMuted = muted) }
            }
        }
        viewModelScope.launch {
            streamManager.isFrontCamera.collect { front ->
                _state.update { it.copy(isFrontCamera = front) }
            }
        }
        viewModelScope.launch {
            streamManager.errorMessage.collect { message ->
                _effect.emit(MainStreamEffect.ShowError(message))
            }
        }
    }

    fun getStreamManager(): StreamManager = streamManager

    fun onIntent(intent: MainStreamIntent) {
        when (intent) {
            MainStreamIntent.StartStream -> startStream()
            MainStreamIntent.StopStream -> streamManager.stopStream()
            MainStreamIntent.ToggleMicrophone -> streamManager.toggleMicrophone()
            MainStreamIntent.SwitchCamera -> streamManager.switchCamera()
            MainStreamIntent.OpenViewerSheet -> {
                if (_state.value.streamStatus == StreamStatus.ONLINE) {
                    viewModelScope.launch { _effect.emit(MainStreamEffect.ShowViewerSheet) }
                }
            }
            MainStreamIntent.CloseViewerSheet -> {
                viewModelScope.launch { _effect.emit(MainStreamEffect.HideViewerSheet) }
            }
        }
    }

    private fun startStream() {
        val key = _state.value.streamKey.trim()
        if (key.isBlank()) return
        val rtmpUrl = "rtmp://live.twitch.tv/app/$key"
        streamManager.startStream(rtmpUrl)
    }

    override fun onCleared() {
        super.onCleared()
        streamManager.release()
    }
}

