package com.example.twitchtest.data.remote

import com.example.twitchtest.data.local.UserEntity
import javax.inject.Inject

interface RemoteUserDataSource {
    suspend fun fetchUsers(): List<UserEntity>
}

class RemoteUserDataSourceImpl @Inject constructor(
    private val apiService: RandomUserApiService
) : RemoteUserDataSource {

    override suspend fun fetchUsers(): List<UserEntity> {
        val response = apiService.getRandomUsers()
        return response.results.map { it.toEntity() }
    }
}

