package com.example.team16_mobile_team_project_1.game

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Data classes to hold the state of game objects
data class CannonState(val id: Int, val x: Float, val y: Float, val angle: Float, var nextFireTime: Long = 0L)
data class CannonballState(val id: Int, val x: Float, val y: Float, val velocityX: Float, val velocityY: Float, val radius: Float = 10f)

// Sealed interface to represent the different states of the game
sealed interface GameState {
    object Ready : GameState
    object Running : GameState
    object Paused : GameState // in game pause
    data class Countdown(val number: Int) : GameState // count down for resume
    data class GameOver(val score: Long) : GameState
}

class GameManager : ViewModel() {

    private val _gameState = MutableStateFlow<GameState>(GameState.Ready)
    val gameState = _gameState.asStateFlow()

    private val _player = MutableStateFlow(Player(0f, 0f))
    val player = _player.asStateFlow()

    private val _cannons = MutableStateFlow<List<CannonState>>(emptyList())
    val cannons = _cannons.asStateFlow()

    private val _cannonballs = MutableStateFlow<List<CannonballState>>(emptyList())
    val cannonballs = _cannonballs.asStateFlow()

    val score = mutableStateOf(0L)
    private var gameTime = 0L

    private var accelX = 0f
    private var accelY = 0f

    // Screen dimensions, to be set from the UI
    var screenWidth = 0f
        set(value) {
            field = value
            Player.screenWidth = value.toInt()
        }
    var screenHeight = 0f
        set(value) {
            field = value
            Player.screenHeight = value.toInt()
        }

    fun startGame() {
        if (_gameState.value !is GameState.Running) {
            _player.value = Player(x = screenWidth / 2, y = screenHeight / 2)
            _cannonballs.value = emptyList()
            spawnCannons()
            gameTime = 0
            score.value = 0
            _gameState.value = GameState.Running
            viewModelScope.launch {
                gameLoop()
            }
        }
    }

    // in game pause
    fun pauseGame() {
        if (_gameState.value == GameState.Running) {
            _gameState.value = GameState.Paused
        }
    }

    // in game resume after pausing
    fun resumeGame() {
        if (_gameState.value != GameState.Paused) return

        viewModelScope.launch {
            for (i in 3 downTo 1) {
                _gameState.value = GameState.Countdown(i)
                delay(1000)
            }

            _gameState.value = GameState.Running
            gameLoop() // restart the loop after countdown
        }
    }

    fun quitToMenu() {
        _gameState.value = GameState.Ready
    }

    private suspend fun gameLoop() {
        while (_gameState.value == GameState.Running) {
            delay(16) // Aim for ~60 FPS
            gameTime += 16
            score.value = gameTime / 100 // Score increases over time

            _player.value.updatePosition(accelX, accelY)
            updateCannonballs()
            fireCannons()
            checkCollisions()
            checkKillZone()
        }
    }

    fun onSensorChanged(newAccelX: Float, newAccelY: Float) {
        accelX = newAccelX
        accelY = newAccelY
    }

    private fun spawnCannons() {
        val centerX = screenWidth / 2f
        val centerY = screenHeight / 2f
        val radius = 1300f // distance from center

        val numberOfCannons = 8 // change this to spawn more/less

        val cannons = mutableListOf<CannonState>()

        for (i in 0 until numberOfCannons) {
            val angleDegrees = (360f / numberOfCannons) * i
            val angleRadians = Math.toRadians(angleDegrees.toDouble())

            val x = centerX + (radius * kotlin.math.cos(angleRadians)).toFloat()
            val y = centerY + (radius * kotlin.math.sin(angleRadians)).toFloat()

            cannons.add(
                CannonState(
                    id = i + 1,
                    x = x,
                    y = y,
                    angle = angleDegrees +180f, // makes cannon face outwar
                    nextFireTime = Random.nextLong(0, 2000)

            )
            )
        }

        _cannons.value = cannons
    }

    private fun fireCannons() {

        val newCannonballs = _cannonballs.value.toMutableList()

        _cannons.value.forEach { cannon ->

            if (gameTime >= cannon.nextFireTime) {

                val angleRad = Math.toRadians(cannon.angle.toDouble()).toFloat()
                val speed = 3f

                newCannonballs.add(
                    CannonballState(
                        id = Random.nextInt(),
                        x = cannon.x,
                        y = cannon.y,
                        velocityX = cos(angleRad) * speed,
                        velocityY = sin(angleRad) * speed
                    )
                )

                // Schedule next shot (random delay)
                cannon.nextFireTime = gameTime + Random.nextLong(1000, 3000)
            }
        }

        _cannonballs.value = newCannonballs
    }

    private fun updateCannonballs() {
        val margin = 800f  // allow bullets outside screen before despawn

        _cannonballs.value = _cannonballs.value
            .map { it.copy(x = it.x + it.velocityX, y = it.y + it.velocityY) }
            .filter {
                it.x > -margin &&
                        it.x < screenWidth + margin &&
                        it.y > -margin &&
                        it.y < screenHeight + margin
            }

        /*_cannonballs.value = _cannonballs.value
            .map { it.copy(x = it.x + it.velocityX, y = it.y + it.velocityY) }
            .filter { it.x > 0 && it.x < screenWidth && it.y > 0 && it.y < screenHeight }
         */
    }


    private fun checkCollisions() {
        val player = _player.value
        _cannonballs.value.forEach { cannonball ->
            val dx = player.x - cannonball.x
            val dy = player.y - cannonball.y
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
            if (distance < player.radius + cannonball.radius) {
                endGame()
                return@forEach
            }
        }
    }

    private fun checkKillZone() {
        val player = _player.value
        val killZone = 20f // 20 pixels from the edge
        if (player.x < killZone || player.x > screenWidth - killZone ||
            player.y < killZone || player.y > screenHeight - killZone
        ) {
            endGame()
        }
    }

    private fun endGame() {
        if (_gameState.value == GameState.Running) {
            _gameState.value = GameState.GameOver(score.value)
        }
    }
}
