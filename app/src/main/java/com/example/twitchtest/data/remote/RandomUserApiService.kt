package com.example.twitchtest.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface RandomUserApiService {

    @GET("api/")
    suspend fun getRandomUsers(
        @Query("results") results: Int = 20
    ): RandomUserResponse
}

