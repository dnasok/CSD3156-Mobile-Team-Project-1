package com.example.team16_mobile_team_project_1.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the high_scores table.
 */
@Dao
interface HighScoreDao {
    /**
     * Inserts a high score in the database. If the high score already exists, it's replaced.
     * @param highscore the high score to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertHighScore(highscore: HighScore)

    /**
     * Fetches the highest score from the high_scores table.
     * @return a Flow of the highest score, or null if the table is empty.
     */
    @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT 1")
    fun getHighScore(): Flow<HighScore?>

}