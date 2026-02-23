package com.example.team16_mobile_team_project_1

import com.example.team16_mobile_team_project_1.database.HighScore
import com.example.team16_mobile_team_project_1.database.HighScoreDao
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
            apiService.getLeaderboard()
        } catch (e: Exception) {
            // Handle network errors, maybe return an empty list
            emptyList()
        }
    }

    suspend fun submitOnlineScore(playerName: String, score: Int) {
        try {
            apiService.submitScore(OnlineScore(playerName, score))
        } catch (e: Exception) {
            // Handle network errors
        }
    }
}
