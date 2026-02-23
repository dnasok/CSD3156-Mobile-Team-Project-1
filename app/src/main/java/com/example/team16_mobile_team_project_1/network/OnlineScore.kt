package com.example.team16_mobile_team_project_1.network

/**
 * Represents a player's score on the online leaderboard.
 *
 * @property playerName The name of the player.
 * @property score The score achieved by the player.
 */
data class OnlineScore (
    val playerName : String = "",
    val score: Int = 0
)