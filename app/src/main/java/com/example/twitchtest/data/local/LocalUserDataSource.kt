package com.example.twitchtest.data.local

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface LocalUserDataSource {
    fun getUsers(): Flow<List<UserEntity>>
    fun searchUsers(query: String): Flow<List<UserEntity>>
    suspend fun replaceAll(users: List<UserEntity>)
}

class LocalUserDataSourceImpl @Inject constructor(
    private val userDao: UserDao
) : LocalUserDataSource {

    override fun getUsers(): Flow<List<UserEntity>> =
        userDao.getAllUsers()

    override fun searchUsers(query: String): Flow<List<UserEntity>> =
        userDao.searchUsers(query)

    override suspend fun replaceAll(users: List<UserEntity>) {
        userDao.replaceAll(users)
    }
}

