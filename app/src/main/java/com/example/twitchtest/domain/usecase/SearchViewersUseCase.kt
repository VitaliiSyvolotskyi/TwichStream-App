package com.example.twitchtest.domain.usecase

import com.example.twitchtest.domain.model.User
import com.example.twitchtest.domain.repository.UserRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class SearchViewersUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(query: String): Flow<List<User>> =
        repository.searchUsers(query)
}

