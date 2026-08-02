package com.example.twitchtest.domain.usecase

import com.example.twitchtest.domain.model.User
import com.example.twitchtest.domain.repository.UserRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetViewersUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<List<User>> = repository.getUsers()
}

