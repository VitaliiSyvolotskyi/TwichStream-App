package com.example.twitchtest.di

import com.example.twitchtest.data.repository.StreamKeyRepositoryImpl
import com.example.twitchtest.data.repository.UserRepositoryImpl
import com.example.twitchtest.domain.repository.StreamKeyRepository
import com.example.twitchtest.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindStreamKeyRepository(
        impl: StreamKeyRepositoryImpl
    ): StreamKeyRepository
}

