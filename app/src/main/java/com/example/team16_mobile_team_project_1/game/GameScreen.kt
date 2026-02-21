package com.example.team16_mobile_team_project_1.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    gameManager: GameManager = viewModel()
) {
    val gameState by gameManager.gameState.collectAsState()
    val playerState by gameManager.playerState.collectAsState()
    val cannons by gameManager.cannons.collectAsState()
    val cannonballs by gameManager.cannonballs.collectAsState()
    val score by gameManager.score

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                gameManager.screenWidth = it.width.toFloat()
                gameManager.screenHeight = it.height.toFloat()
            }
    ) {
        when (val state = gameState) {
            is GameState.Ready -> {
                StartMenu(onStartClick = { gameManager.startGame() }, modifier = Modifier.align(Alignment.Center))
            }
            is GameState.Running -> {
                GameCanvas(playerState, cannons, cannonballs)
                Text("Score: $score", modifier = Modifier.align(Alignment.TopCenter), fontSize = 24.sp)

                // Pause button
                Button(
                    onClick = { gameManager.pauseGame() },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text("||")
                }
            }
            is GameState.GameOver -> {
                GameOverMenu(
                    score = state.score,
                    onRestartClick = { gameManager.startGame() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is GameState.Paused -> {
                // Draw frozen game scene
                GameCanvas(playerState, cannons, cannonballs)
                Text("Score: $score", modifier = Modifier.align(Alignment.TopCenter), fontSize = 24.sp)

                PauseMenu(
                    onResume = { gameManager.resumeGame() },
                    onRestart = { gameManager.startGame() },
                    onQuit = { gameManager.quitToMenu() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is GameState.Countdown -> {
                // Draw frozen game scene behind
                GameCanvas(playerState, cannons, cannonballs)
                Text("Score: $score", modifier = Modifier.align(Alignment.TopCenter), fontSize = 24.sp)

                // Big countdown number in the middle
                Text(
                    text = state.number.toString(),
                    fontSize = 96.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun GameCanvas(player: PlayerState, cannons: List<CannonState>, cannonballs: List<CannonballState>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Draw Kill Zone border
        drawRect(color = Color.Red, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 20f))

        // Draw Player
        drawCircle(color = Color.Blue, radius = player.radius, center = Offset(player.x, player.y))

        // Draw Cannons
        cannons.forEach { cannon ->
            drawRect(color = Color.Black, topLeft = Offset(cannon.x - 25, cannon.y - 25), size = androidx.compose.ui.geometry.Size(50f, 50f))
        }

        // Draw Cannonballs
        cannonballs.forEach { cannonball ->
            drawCircle(color = Color.DarkGray, radius = cannonball.radius, center = Offset(cannonball.x, cannonball.y))
        }
    }
}

@Composable
fun StartMenu(onStartClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text("Cannon Crossfire", fontSize = 32.sp)
        Button(onClick = onStartClick) {
            Text("Start Game")
        }
    }
}

@Composable
fun GameOverMenu(score: Long, onRestartClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text("Game Over", fontSize = 32.sp)
        Text("Your Score: $score", fontSize = 24.sp)
        Button(onClick = onRestartClick) {
            Text("Restart Game")
        }
    }
}

@Composable
fun PauseMenu(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onQuit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text("Paused", fontSize = 32.sp)
        Button(onClick = onResume) { Text("Resume") }
        Button(onClick = onRestart) { Text("Restart") }
        Button(onClick = onQuit) { Text("Quit to Menu") }
    }
}