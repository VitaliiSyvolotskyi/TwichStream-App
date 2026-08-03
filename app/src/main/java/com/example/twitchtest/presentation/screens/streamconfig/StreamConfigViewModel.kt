package com.example.twitchtest.presentation.screens.streamconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.twitchtest.domain.usecase.GetStreamKeyUseCase
import com.example.twitchtest.domain.usecase.SaveStreamKeyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URI

@HiltViewModel
class StreamConfigViewModel @Inject constructor(
    private val saveStreamKeyUseCase: SaveStreamKeyUseCase,
    private val getStreamKeyUseCase: GetStreamKeyUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(StreamConfigState())
    val state: StateFlow<StreamConfigState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<StreamConfigEffect>()
    val effect: Flow<StreamConfigEffect> = _effect.asSharedFlow()

    init {
        viewModelScope.launch {
            getStreamKeyUseCase().collect { key ->
                _state.update {
                    it.copy(
                        streamKey = key.orEmpty(),
                        isSaved = !key.isNullOrBlank(),
                        error = null
                    )
                }
            }
        }
    }

    fun onIntent(intent: StreamConfigIntent) {
        when (intent) {
            is StreamConfigIntent.UpdateKey -> {
                _state.update {
                    it.copy(
                        streamKey = intent.key,
                        isSaved = false,
                        error = null
                    )
                }
            }

            StreamConfigIntent.SaveKey -> saveKey()
            StreamConfigIntent.NavigateToStream -> navigateToStream()
        }
    }

    private fun saveKey() {
        viewModelScope.launch {
            val streamUrl = _state.value.streamKey.trim()
            if (streamUrl.isBlank()) {
                _state.update { it.copy(error = "Stream URL cannot be empty") }
                return@launch
            }

            if (!isValidStreamUrl(streamUrl)) {
                _state.update { it.copy(error = "Please enter a valid stream URL") }
                return@launch
            }

            saveStreamKeyUseCase(streamUrl)
            _state.update {
                it.copy(
                    streamKey = streamUrl,
                    isSaved = true,
                    error = null
                )
            }
        }
    }

    private fun navigateToStream() {
        viewModelScope.launch {
            if (!_state.value.isSaved) {
                _state.update { it.copy(error = "Please save a stream URL first") }
                return@launch
            }
            _effect.emit(StreamConfigEffect.NavigateToMainStream)
        }
    }

    private fun isValidStreamUrl(value: String): Boolean {
        val uri = runCatching { URI(value) }.getOrNull() ?: return false
        return !uri.scheme.isNullOrBlank() && !uri.schemeSpecificPart.isNullOrBlank()
    }
}


