package com.example.team16_mobile_team_project_1.game

/**
 * Represents a cannon positioned on the wall, which fires cannonballs at the player.
 *
 * @property id A unique identifier for the cannon.
 * @property x The x-coordinate of the cannon's center.
 * @property y The y-coordinate of the cannon's center.
 * @property angle The angle in degrees at which the cannon is aimed.
 * @property radius The radius of the cannon.
 * @property nextFireTime The game time at which the cannon is scheduled to fire next.
 */
data class Cannon(
    val id: Int,
    val x: Float,
    val y: Float,
    val angle: Float,
    val radius: Float = 37.5f,
    var nextFireTime: Long = 0L
)

/**
 * Represents a projectile fired by a cannon.
 *
 * @property id A unique identifier for the cannonball.
 * @property cannonId The ID of the cannon that fired this cannonball.
 * @property x The current x-coordinate of the cannonball's center.
 * @property y The current y-coordinate of the cannonball's center.
 * @property velocityX The velocity of the cannonball on the x-axis.
 * @property velocityY The velocity of the cannonball on the y-axis.
 * @property radius The radius of the cannonball.
 */
data class Cannonball(
    val id: Int,
    val cannonId: Int,
    val x: Float,
    val y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val radius: Float = 15f
)
