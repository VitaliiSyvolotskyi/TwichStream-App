package com.example.twitchtest.data.repository

import com.example.twitchtest.data.local.LocalUserDataSource
import com.example.twitchtest.data.remote.RemoteUserDataSource
import com.example.twitchtest.data.remote.toDomain
import com.example.twitchtest.domain.model.User
import com.example.twitchtest.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteUserDataSource,
    private val localDataSource: LocalUserDataSource
) : UserRepository {

    override fun getUsers(): Flow<List<User>> =
        localDataSource.getUsers().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun searchUsers(query: String): Flow<List<User>> =
        localDataSource.searchUsers(query).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun refreshUsers() {
        val users = remoteDataSource.fetchUsers()
        localDataSource.replaceAll(users)
    }
}
