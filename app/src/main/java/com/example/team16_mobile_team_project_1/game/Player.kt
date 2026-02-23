package com.example.team16_mobile_team_project_1.game

/**
 * Represents the player character in the game.
 *
 * @property x The current x-coordinate of the player's center.
 * @property y The current y-coordinate of the player's center.
 * @property radius The radius of the player.
 * @property velX The current velocity of the player on the x-axis.
 * @property velY The current velocity of the player on the y-axis.
 */
data class Player(
    var x: Float,
    var y: Float,
    val radius: Float = 37.5f,
    private var velX: Float = 0f,
    private var velY: Float = 0f
) {
    /**
     * Updates the player's position based on accelerometer input.
     * The movement is physics-based, with acceleration, velocity, and friction.
     *
     * @param accelX The acceleration on the x-axis, from the device's accelerometer.
     * @param accelY The acceleration on the y-axis, from the device's accelerometer.
     */
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

    /**
     * Companion object to hold screen dimensions, used for boundary checking.
     */
    companion object {
        /** The width of the screen in pixels. */
        var screenWidth: Int = 0
        /** The height of the screen in pixels. */
        var screenHeight: Int = 0
    }
}
