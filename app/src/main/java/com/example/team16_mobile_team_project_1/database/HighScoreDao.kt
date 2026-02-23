package com.example.team16_mobile_team_project_1.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HighScoreDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertHighScore(highscore: HighScore)

    @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT 1")
    fun getHighScore(): Flow<HighScore?>

}