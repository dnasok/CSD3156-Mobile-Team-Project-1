package com.example.team16_mobile_team_project_1.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single high score entry in the database.
 *
 * @property id The unique identifier for the high score entry.
 * @property score The score value.
 */
@Entity(tableName = "high_scores")
data class HighScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Int
)