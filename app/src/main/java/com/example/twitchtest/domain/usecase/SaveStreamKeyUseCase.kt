package com.example.twitchtest.domain.usecase

import com.example.twitchtest.domain.repository.StreamKeyRepository
import javax.inject.Inject

class SaveStreamKeyUseCase @Inject constructor(
    private val repository: StreamKeyRepository
) {
    suspend operator fun invoke(key: String) {
        repository.saveStreamKey(key)
    }
}

