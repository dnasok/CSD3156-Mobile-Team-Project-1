package com.example.team16_mobile_team_project_1.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Defines the REST API endpoints for the online leaderboard.
 */
interface ApiService {
    /**
     * Fetches the entire leaderboard from the server.
     * The response is a map where the keys are player names and the values are their scores.
     *
     * @return A Retrofit [Response] containing a map of player names to [OnlineScore] objects.
     */
    @GET("leaderboard.json")
    suspend fun getLeaderboard() : Response<Map<String, OnlineScore>?>

    /**
     * Submits a new score for a player to the leaderboard.
     * This will create or update the player's score.
     *
     * @param username The name of the player.
     * @param score The [OnlineScore] object containing the player's name and score.
     * @return A Retrofit [Response] containing the submitted [OnlineScore].
     */
    @PUT("leaderboard/{username}.json")
    suspend fun submitScore(
        @Path("username") username : String,
        @Body score: OnlineScore): Response<OnlineScore>
}