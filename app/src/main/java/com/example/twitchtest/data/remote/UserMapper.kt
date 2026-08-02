package com.example.twitchtest.data.remote

import com.example.twitchtest.data.local.UserEntity
import com.example.twitchtest.domain.model.User

fun UserDto.toEntity(): UserEntity = UserEntity(
    id = login.uuid,
    firstName = name.first,
    lastName = name.last,
    email = email,
    thumbnailUrl = picture.thumbnail,
    largePictureUrl = picture.large
)

fun UserEntity.toDomain(): User = User(
    id = id,
    firstName = firstName,
    lastName = lastName,
    email = email,
    thumbnailUrl = thumbnailUrl,
    largePictureUrl = largePictureUrl
)

