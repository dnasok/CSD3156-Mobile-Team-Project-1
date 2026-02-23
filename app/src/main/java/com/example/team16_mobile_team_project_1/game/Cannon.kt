package com.example.team16_mobile_team_project_1.game

// Data classes to hold the state of game objects
data class Cannon(
    val id: Int,
    val x: Float,
    val y: Float,
    val angle: Float,
    val radius: Float = 37.5f,
    var nextFireTime: Long = 0L
)

data class Cannonball(
    val id: Int,
    val cannonId: Int,
    val x: Float,
    val y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val radius: Float = 15f
)
