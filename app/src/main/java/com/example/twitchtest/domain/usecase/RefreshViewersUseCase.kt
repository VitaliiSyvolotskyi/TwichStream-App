package com.example.twitchtest.domain.usecase

import com.example.twitchtest.domain.repository.UserRepository
import javax.inject.Inject

class RefreshViewersUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke() {
        repository.refreshUsers()
    }
}

