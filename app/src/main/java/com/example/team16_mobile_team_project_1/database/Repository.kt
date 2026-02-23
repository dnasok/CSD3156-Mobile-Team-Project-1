package com.example.team16_mobile_team_project_1.database

import android.util.Log
import com.example.team16_mobile_team_project_1.network.ApiService
import com.example.team16_mobile_team_project_1.network.OnlineScore
import kotlinx.coroutines.flow.Flow

/**
 * A repository that handles data operations for scores. It abstracts the data sources,
 * providing a clean API for the rest of the app to interact with.
 *
 * @param highScoreDao The Data Access Object for the local high score database.
 * @param apiService The service for interacting with the online leaderboard API.
 */
class ScoreRepository(private val highScoreDao: HighScoreDao, private val apiService: ApiService) {

    /**
     * Retrieves the local high score from the database.
     *
     * @return A Flow that emits the local [HighScore], or null if none exists.
     */
    fun getLocalHighScore(): Flow<HighScore?> = highScoreDao.getHighScore()

    /**
     * Saves a new high score to the local database.
     *
     * @param score The score to save.
     */
    suspend fun saveLocalHighScore(score: Int) {
        highScoreDao.insertHighScore(HighScore(score = score))
    }

    /**
     * Fetches the top 5 scores from the online leaderboard.
     *
     * @return A list of [OnlineScore] objects representing the top 5, or an empty list on failure.
     */
    suspend fun getOnlineLeaderboard(): List<OnlineScore> {
        return try {
            val response = apiService.getLeaderboard()
            if(response.isSuccessful) {
                val scoresMap = response.body()
                scoresMap?.values?.toList()
                    ?.sortedByDescending { it.score }
                    ?.take(5)
                    ?: emptyList()
            } else {
                Log.e("ScoreRepository", "Failed to fetch leaderboard. Code: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            // Handle network errors, maybe return an empty list
            Log.e("ScoreRepository","Failed to fetch online leaderboard",e)
            emptyList()
        }
    }

    /**
     * Submits a score to the online leaderboard for a given player.
     * It only submits the score if it is higher than the player's existing score.
     *
     * @param playerName The name of the player.
     * @param score The score to submit.
     */
    suspend fun submitOnlineScore(playerName: String, score: Int) {
        try {
            val onlineScores = getOnlineLeaderboard()
            // This comparison is case-sensitive. Use .equals(playerName, ignoreCase = true) for case-insensitivity
            val existingScore = onlineScores.find { it.playerName == playerName }
            val isHigher = existingScore == null || score > existingScore.score

            if(isHigher){
                Log.d("ScoreRepository", "Submitting new high score for $playerName: $score")
                apiService.submitScore(playerName, OnlineScore(playerName, score))

            } else {
                Log.d("ScoreRepository", "New score for $playerName ($score) is not high enough")
            }
        } catch (e: Exception) {
            // Handle network errors
            Log.e("ScoreRepository", "Failed to submit to online leaderboard", e)
        }
    }
}
