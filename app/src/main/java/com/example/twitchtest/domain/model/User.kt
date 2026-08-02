package com.example.twitchtest.domain.model

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val thumbnailUrl: String,
    val largePictureUrl: String
) {
    val fullName: String
        get() = "$firstName $lastName"
}

