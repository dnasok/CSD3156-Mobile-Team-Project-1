package com.example.team16_mobile_team_project_1.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "high_scores")
data class HighScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Int
)