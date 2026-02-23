package com.example.team16_mobile_team_project_1.database

import android.util.Log
import com.example.team16_mobile_team_project_1.network.ApiService
import com.example.team16_mobile_team_project_1.network.OnlineScore
import kotlinx.coroutines.flow.Flow

class ScoreRepository(private val highScoreDao: HighScoreDao, private val apiService: ApiService) {

    fun getLocalHighScore(): Flow<HighScore?> = highScoreDao.getHighScore()

    suspend fun saveLocalHighScore(score: Int) {
        highScoreDao.insertHighScore(HighScore(score = score))
    }

    suspend fun getOnlineLeaderboard(): List<OnlineScore> {
        return try {
            val response = apiService.getLeaderboard()
            if(response.isSuccessful) {
                val scoresMap = response.body()
                scoresMap?.values?.toList()
                    ?.sortedByDescending { it.score }
                    ?.take(5)
                    ?: emptyList()
            }
            else {
                Log.e("ScoreRepository", "Failed to fetch leaderboard. Code: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            // Handle network errors, maybe return an empty list
            Log.e("ScoreRepository","Failed to fetch online leaderboard",e)
            emptyList()
        }
    }

    suspend fun submitOnlineScore(playerName: String, score: Int) {
        try {
            val onlineScores = getOnlineLeaderboard()
            // This comparison is case-sensitive. Use .equals(playerName, ignoreCase = true) for case-insensitivity
            val existingScore = onlineScores.find { it.playerName == playerName }
            val isHigher = existingScore == null || score > existingScore.score

            if(isHigher){
                Log.d("SCoreRepository", "Submitting new high score for $playerName: $score")
                apiService.submitScore(playerName, OnlineScore(playerName, score))

            }
            else {
                Log.d("ScoreRepository", "New score for $playerName ($score) is not high enough")
            }
        } catch (e: Exception) {
            // Handle network errors
            Log.e("ScoreRepository", "Failed to submit to online leaderboard", e)
        }
    }
}
