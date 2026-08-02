package com.example.twitchtest.data.remote

import com.google.gson.annotations.SerializedName

data class RandomUserResponse(
    @SerializedName("results")
    val results: List<UserDto>
)

data class UserDto(
    @SerializedName("login")
    val login: LoginDto,
    @SerializedName("name")
    val name: NameDto,
    @SerializedName("email")
    val email: String,
    @SerializedName("picture")
    val picture: PictureDto
)

data class LoginDto(
    @SerializedName("uuid")
    val uuid: String
)

data class NameDto(
    @SerializedName("first")
    val first: String,
    @SerializedName("last")
    val last: String
)

data class PictureDto(
    @SerializedName("thumbnail")
    val thumbnail: String,
    @SerializedName("large")
    val large: String
)

