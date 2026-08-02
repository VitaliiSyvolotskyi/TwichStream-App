package com.example.twitchtest.presentation.streamconfig

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
                        isLoading = false,
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
            val key = _state.value.streamKey.trim()
            if (key.isBlank()) {
                _state.update { it.copy(error = "Stream key cannot be empty") }
                return@launch
            }

            saveStreamKeyUseCase(key)
            _state.update {
                it.copy(
                    streamKey = key,
                    isSaved = true,
                    error = null
                )
            }
        }
    }

    private fun navigateToStream() {
        viewModelScope.launch {
            if (!_state.value.isSaved) {
                _state.update { it.copy(error = "Please save a stream key first") }
                return@launch
            }
            _effect.emit(StreamConfigEffect.NavigateToMainStream)
        }
    }
}


