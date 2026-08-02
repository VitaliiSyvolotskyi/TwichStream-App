package com.example.twitchtest.domain.repository

import com.example.twitchtest.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUsers(): Flow<List<User>>
    fun searchUsers(query: String): Flow<List<User>>
    suspend fun refreshUsers()
}

