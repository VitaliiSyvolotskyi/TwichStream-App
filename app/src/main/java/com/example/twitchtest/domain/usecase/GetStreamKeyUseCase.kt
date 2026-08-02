package com.example.twitchtest.domain.usecase

import com.example.twitchtest.domain.repository.StreamKeyRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetStreamKeyUseCase @Inject constructor(
    private val repository: StreamKeyRepository
) {
    operator fun invoke(): Flow<String?> = repository.getStreamKey()
}

