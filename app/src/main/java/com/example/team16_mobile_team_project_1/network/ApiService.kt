package com.example.team16_mobile_team_project_1.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @GET("leaderboard.json")
    suspend fun getLeaderboard() : Response<Map<String, OnlineScore>?>

    @PUT("leaderboard/{username}.json")
    suspend fun submitScore(
        @Path("username") username : String,
        @Body score: OnlineScore): Response<OnlineScore>
}