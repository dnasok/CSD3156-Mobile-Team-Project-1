package com.example.team16_mobile_team_project_1.game

import com.example.team16_mobile_team_project_1.game.Player.Companion.screenHeight
import com.example.team16_mobile_team_project_1.game.Player.Companion.screenWidth

data class Player(
    var x: Float,
    var y: Float,
    val radius: Float = 25f,
    private var velX: Float = 0f,
    private var velY: Float = 0f
) {
    fun updatePosition(accelX: Float, accelY: Float) {
        // Using accelerometer for a tilt-based "balancing" movement.
        // The phone's tilt affects the player's velocity.
        val sensitivity = 0.5f

        // The accelerometer values are used to simulate tilt.
        velX += -accelX * sensitivity
        velY += accelY * sensitivity

        // Apply friction so the player eventually stops.
        velX *= 0.92f
        velY *= 0.92f

        // Update position based on velocity
        x += velX
        y += velY

        // Clamp the player's position to stay within the screen bounds.
        x = x.coerceIn(0f, screenWidth.toFloat())
        y = y.coerceIn(0f, screenHeight.toFloat())
    }

    companion object {
        var screenWidth: Int = 0
        var screenHeight: Int = 0
    }
}
