package com.example.twitchtest.presentation.viewerlist

import com.example.twitchtest.domain.model.User

data class ViewerListState(
    val viewers: List<User> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface ViewerListIntent {
    data class Search(val query: String) : ViewerListIntent
}

