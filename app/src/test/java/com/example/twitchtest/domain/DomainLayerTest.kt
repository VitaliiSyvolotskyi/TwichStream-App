package com.example.twitchtest.domain

import com.example.twitchtest.domain.model.StreamStatus
import com.example.twitchtest.domain.model.User
import com.example.twitchtest.domain.repository.StreamKeyRepository
import com.example.twitchtest.domain.repository.UserRepository
import com.example.twitchtest.domain.usecase.GetStreamKeyUseCase
import com.example.twitchtest.domain.usecase.GetViewersUseCase
import com.example.twitchtest.domain.usecase.RefreshViewersUseCase
import com.example.twitchtest.domain.usecase.SaveStreamKeyUseCase
import com.example.twitchtest.domain.usecase.SearchViewersUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainLayerTest {

    @Test
    fun `user fullName combines first and last name`() {
        val user = sampleUser(firstName = "Ada", lastName = "Lovelace")

        assertEquals("Ada Lovelace", user.fullName)
    }

    @Test
    fun `stream status contains all expected states in order`() {
        assertEquals(
            listOf(
                StreamStatus.OFFLINE,
                StreamStatus.CONNECTING,
                StreamStatus.ONLINE,
                StreamStatus.RECONNECTING
            ),
            StreamStatus.entries
        )
    }

    @Test
    fun `save stream key use case delegates to repository`() = runBlocking {
        val repository = FakeStreamKeyRepository(initialKey = null)
        val useCase = SaveStreamKeyUseCase(repository)

        useCase("stream-key-123")

        assertEquals("stream-key-123", repository.savedKey)
    }

    @Test
    fun `get stream key use case returns repository flow unchanged`() = runBlocking {
        val repository = FakeStreamKeyRepository(initialKey = "abc123")
        val useCase = GetStreamKeyUseCase(repository)

        val key = useCase().first()

        assertEquals("abc123", key)
    }

    @Test
    fun `get viewers use case returns repository viewers`() = runBlocking {
        val expectedUsers = listOf(sampleUser(id = "viewer-1"), sampleUser(id = "viewer-2"))
        val repository = FakeUserRepository(users = expectedUsers)
        val useCase = GetViewersUseCase(repository)

        val users = useCase().first()

        assertEquals(expectedUsers, users)
    }

    @Test
    fun `refresh viewers use case calls repository refresh exactly once`() = runBlocking {
        val repository = FakeUserRepository(users = emptyList())
        val useCase = RefreshViewersUseCase(repository)

        useCase()

        assertEquals(1, repository.refreshCallCount)
    }

    @Test
    fun `search viewers use case forwards exact query and returns repository results`() = runBlocking {
        val expectedUsers = listOf(sampleUser(id = "search-1", firstName = "Grace", lastName = "Hopper"))
        val repository = FakeUserRepository(users = emptyList(), searchResults = expectedUsers)
        val useCase = SearchViewersUseCase(repository)

        val users = useCase("Grace").first()

        assertEquals("Grace", repository.lastSearchQuery)
        assertEquals(expectedUsers, users)
    }

    @Test
    fun `save stream key use case supports empty key values without transformation`() = runBlocking {
        val repository = FakeStreamKeyRepository(initialKey = "initial")
        val useCase = SaveStreamKeyUseCase(repository)

        useCase("")

        assertTrue(repository.savedKey != null)
        assertEquals("", repository.savedKey)
    }

    private fun sampleUser(
        id: String = "user-1",
        firstName: String = "Test",
        lastName: String = "User"
    ): User = User(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = "$id@example.com",
        thumbnailUrl = "https://example.com/$id-thumb.jpg",
        largePictureUrl = "https://example.com/$id-large.jpg"
    )

    private class FakeStreamKeyRepository(initialKey: String?) : StreamKeyRepository {
        var savedKey: String? = initialKey
            private set

        override fun getStreamKey(): Flow<String?> = flowOf(savedKey)

        override suspend fun saveStreamKey(key: String) {
            savedKey = key
        }
    }

    private class FakeUserRepository(
        private val users: List<User>,
        private val searchResults: List<User> = users
    ) : UserRepository {
        var refreshCallCount: Int = 0
            private set
        var lastSearchQuery: String? = null
            private set

        override fun getUsers(): Flow<List<User>> = flowOf(users)

        override fun searchUsers(query: String): Flow<List<User>> {
            lastSearchQuery = query
            return flowOf(searchResults)
        }

        override suspend fun refreshUsers() {
            refreshCallCount++
        }
    }
}

