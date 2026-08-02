package com.example.twitchtest.presentation.screens.viewerlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.twitchtest.domain.usecase.GetViewersUseCase
import com.example.twitchtest.domain.usecase.RefreshViewersUseCase
import com.example.twitchtest.domain.usecase.SearchViewersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ViewerListViewModel @Inject constructor(
    private val getViewersUseCase: GetViewersUseCase,
    private val refreshViewersUseCase: RefreshViewersUseCase,
    private val searchViewersUseCase: SearchViewersUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ViewerListState())
    val state: StateFlow<ViewerListState> = _state.asStateFlow()

    private var observeJob: Job? = null

    init {
        loadViewers()
    }

    fun onIntent(intent: ViewerListIntent) {
        when (intent) {
            is ViewerListIntent.Search -> search(intent.query)
        }
    }

    private fun loadViewers() {
        observeViewers(_state.value.searchQuery)
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching {
                refreshViewersUseCase()
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.message ?: "Failed to refresh viewers"
                    )
                }
            }
        }
    }

    private fun search(query: String) {
        _state.update { it.copy(searchQuery = query, error = null) }
        observeViewers(query)
    }

    private fun observeViewers(query: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            val source = if (query.isBlank()) {
                getViewersUseCase()
            } else {
                searchViewersUseCase(query)
            }
            source.collect { viewers ->
                _state.update {
                    it.copy(
                        viewers = viewers,
                        isLoading = false
                    )
                }
            }
        }
    }
}

