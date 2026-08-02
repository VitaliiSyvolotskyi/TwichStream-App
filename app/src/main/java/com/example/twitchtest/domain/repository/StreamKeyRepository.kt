package com.example.twitchtest.domain.repository

import kotlinx.coroutines.flow.Flow

interface StreamKeyRepository {
    fun getStreamKey(): Flow<String?>
    suspend fun saveStreamKey(key: String)
}

