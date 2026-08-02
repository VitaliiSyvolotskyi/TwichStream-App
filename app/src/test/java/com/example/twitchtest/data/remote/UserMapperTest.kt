package com.example.twitchtest.data.remote

import com.example.twitchtest.data.local.UserEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class UserMapperTest {

    @Test
    fun `toEntity maps dto fields correctly`() {
        val dto = UserDto(
            login = LoginDto(uuid = "user-123"),
            name = NameDto(first = "Ada", last = "Lovelace"),
            email = "ada@example.com",
            picture = PictureDto(
                thumbnail = "https://example.com/thumb.jpg",
                large = "https://example.com/large.jpg"
            )
        )

        val entity = dto.toEntity()

        assertEquals(
            UserEntity(
                id = "user-123",
                firstName = "Ada",
                lastName = "Lovelace",
                email = "ada@example.com",
                thumbnailUrl = "https://example.com/thumb.jpg",
                largePictureUrl = "https://example.com/large.jpg"
            ),
            entity
        )
    }

    @Test
    fun `toDomain maps entity fields correctly`() {
        val entity = UserEntity(
            id = "user-456",
            firstName = "Grace",
            lastName = "Hopper",
            email = "grace@example.com",
            thumbnailUrl = "https://example.com/thumb2.jpg",
            largePictureUrl = "https://example.com/large2.jpg"
        )

        val user = entity.toDomain()

        assertEquals("user-456", user.id)
        assertEquals("Grace", user.firstName)
        assertEquals("Hopper", user.lastName)
        assertEquals("grace@example.com", user.email)
        assertEquals("https://example.com/thumb2.jpg", user.thumbnailUrl)
        assertEquals("https://example.com/large2.jpg", user.largePictureUrl)
        assertEquals("Grace Hopper", user.fullName)
    }
}

