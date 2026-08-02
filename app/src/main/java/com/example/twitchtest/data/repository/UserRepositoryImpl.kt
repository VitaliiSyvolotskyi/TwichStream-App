package com.example.twitchtest.data.repository

import com.example.twitchtest.data.local.UserDao
import com.example.twitchtest.data.remote.RandomUserApiService
import com.example.twitchtest.data.remote.toDomain
import com.example.twitchtest.data.remote.toEntity
import com.example.twitchtest.domain.model.User
import com.example.twitchtest.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: RandomUserApiService,
    private val userDao: UserDao
) : UserRepository {

    override fun getUsers(): Flow<List<User>> =
        userDao.getAllUsers().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun searchUsers(query: String): Flow<List<User>> =
        userDao.searchUsers(query).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun refreshUsers() {
        val response = apiService.getRandomUsers()
        val entities = response.results.map { it.toEntity() }
        userDao.clearAll()
        userDao.insertAll(entities)
    }
}

