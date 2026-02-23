package com.example.team16_mobile_team_project_1.network

import com.example.team16_mobile_team_project_1.network.OnlineScore
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("leaderboard")
    suspend fun getLeaderboard() : List<OnlineScore>

    @POST("submit")
    suspend fun submitScore(@Body score: OnlineScore)
}