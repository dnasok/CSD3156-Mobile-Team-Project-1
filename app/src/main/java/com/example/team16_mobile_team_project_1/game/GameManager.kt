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
data class CannonState(
    val id: Int,
    val x: Float,
    val y: Float,
    val angle: Float,
    var nextFireTime: Long = 0L
)

data class CannonballState(
    val id: Int,
    val cannonId: Int,
    val x: Float,
    val y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val radius: Float = 10f
)

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

    private val _coin = MutableStateFlow<Coin?>(null)
    val coin = _coin.asStateFlow()

    val score = mutableStateOf(0L)
    private var gameTime = 0L
    private var lastScoreUpdateTime = 0L

    private var accelX = 0f
    private var accelY = 0f

    private var nextCannonId = 0
    private var nextCannonSpawnTime = 0L

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
            gameTime = 0
            score.value = 0
            lastScoreUpdateTime = 0L
            _player.value = Player(x = screenWidth / 2, y = screenHeight / 2)
            _cannonballs.value = emptyList()
            nextCannonId = 0
            spawnInitialCannons()
            spawnCoin()
            nextCannonSpawnTime = 5000L // First new cannon after 5 seconds
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
            // Score increases over time
            if (gameTime - lastScoreUpdateTime >= 100) {
                score.value += 1
                lastScoreUpdateTime = gameTime
            }

            _player.value.updatePosition(accelX, accelY)
            updateCannonballs()
            fireCannons()
            spawnMoreCannons()
            checkCollisions()
            checkCoinCollision()
            checkKillZone()
        }
    }

    fun onSensorChanged(newAccelX: Float, newAccelY: Float) {
        accelX = newAccelX
        accelY = newAccelY
    }

    private fun spawnInitialCannons() {
        val initialCannons = 7
        val cannons = mutableListOf<CannonState>()
        for (i in 0 until initialCannons) {
            cannons.add(spawnSingleCannon())
        }
        _cannons.value = cannons
    }

    private fun spawnSingleCannon(): CannonState {
        val centerX = screenWidth / 2f
        val centerY = screenHeight / 2f
        val cannonRadius = 25f // Half of the cannon image size (50)

        val side = Random.nextInt(4)
        val x: Float
        val y: Float

        when (side) {
            0 -> { // Top wall
                x = Random.nextFloat() * (screenWidth - cannonRadius * 2) + cannonRadius
                y = cannonRadius
            }

            1 -> { // Right wall
                x = screenWidth - cannonRadius
                y = Random.nextFloat() * (screenHeight - cannonRadius * 2) + cannonRadius
            }

            2 -> { // Bottom wall
                x = Random.nextFloat() * (screenWidth - cannonRadius * 2) + cannonRadius
                y = screenHeight - cannonRadius
            }

            else -> { // Left wall
                x = cannonRadius
                y = Random.nextFloat() * (screenHeight - cannonRadius * 2) + cannonRadius
            }
        }

        val angleToCenter =
            Math.toDegrees(kotlin.math.atan2(centerY - y, centerX - x).toDouble()).toFloat()

        return CannonState(
            id = nextCannonId++,
            x = x,
            y = y,
            angle = angleToCenter,
            nextFireTime = gameTime + Random.nextLong(1000, 3000)
        )
    }

    private fun fireCannons() {
        val newCannonballs = _cannonballs.value.toMutableList()

        _cannons.value.forEach { cannon ->
            if (gameTime >= cannon.nextFireTime) {
                val angleRad = Math.toRadians(cannon.angle.toDouble()).toFloat()
                val speed = 7f + (gameTime / 15000f) // Increase speed over time

                newCannonballs.add(
                    CannonballState(
                        id = Random.nextInt(),
                        cannonId = cannon.id,
                        x = cannon.x,
                        y = cannon.y,
                        velocityX = cos(angleRad) * speed,
                        velocityY = sin(angleRad) * speed
                    )
                )

                cannon.nextFireTime = Long.MAX_VALUE // Prevent this cannon from firing again
                AudioManager.playSound(AudioManager.Sound.SHOOT)
            }
        }
        _cannonballs.value = newCannonballs
    }

    private fun spawnMoreCannons() {
        if (gameTime >= nextCannonSpawnTime) {
            val newCannons = _cannons.value.toMutableList()
            newCannons.add(spawnSingleCannon())
            _cannons.value = newCannons

            val spawnDelay = (5000 - (gameTime / 10)).coerceAtLeast(1000L)
            nextCannonSpawnTime = gameTime + spawnDelay
        }
    }

    private fun updateCannonballs() {
        val newCannonballs = _cannonballs.value
            .map { it.copy(x = it.x + it.velocityX, y = it.y + it.velocityY) }
            .filter { cb ->
                val despawned = cb.x < 0 || cb.x > screenWidth || cb.y < 0 || cb.y > screenHeight
                if (despawned) {
                    // When a cannonball is despawned, remove the cannon that fired it and spawn a new one
                    val newCannons = _cannons.value.toMutableList()
                    newCannons.removeAll { it.id == cb.cannonId }
                    newCannons.add(spawnSingleCannon())
                    _cannons.value = newCannons
                }
                !despawned
            }
        _cannonballs.value = newCannonballs
    }

    private fun checkCollisions() {
        val player = _player.value
        val newCannonballs = _cannonballs.value.toMutableList()
        var collisionOccurred = false

        val iterator = newCannonballs.iterator()
        while (iterator.hasNext()) {
            val cannonball = iterator.next()
            val dx = player.x - cannonball.x
            val dy = player.y - cannonball.y
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
            if (distance < player.radius + cannonball.radius) {
                iterator.remove()
                val newCannons = _cannons.value.toMutableList()
                newCannons.removeAll { it.id == cannonball.cannonId }
                newCannons.add(spawnSingleCannon())
                _cannons.value = newCannons
                AudioManager.playSound(AudioManager.Sound.HIT)
                endGame()
                collisionOccurred = true
                break
            }
        }

        if (collisionOccurred) {
            _cannonballs.value = newCannonballs
        }
    }

    private fun spawnCoin() {
        val padding = 50f
        val x = Random.nextFloat() * (screenWidth - padding * 2) + padding
        val y = Random.nextFloat() * (screenHeight - padding * 2) + padding
        _coin.value = Coin(x, y)
    }

    private fun checkCoinCollision() {
        _coin.value?.let { coin ->
            val player = _player.value
            val dx = player.x - coin.x
            val dy = player.y - coin.y
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
            if (distance < player.radius + coin.radius) {
                score.value += 50
                _coin.value = null
                AudioManager.playSound(AudioManager.Sound.COIN)
                spawnCoin()
            }
        }
    }

    private fun checkKillZone() {
        val player = _player.value
        val killZone = 10f // 10 pixels from the edge
        if (player.x < killZone || player.x > screenWidth - killZone ||
            player.y < killZone || player.y > screenHeight - killZone
        ) {
            AudioManager.playSound(AudioManager.Sound.HIT)
            endGame()
        }
    }

    private fun endGame() {
        if (_gameState.value == GameState.Running) {
            _gameState.value = GameState.GameOver(score.value)
        }
    }
}
