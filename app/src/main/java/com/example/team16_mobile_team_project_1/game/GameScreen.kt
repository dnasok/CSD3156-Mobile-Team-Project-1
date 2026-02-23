package com.example.team16_mobile_team_project_1.game

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.team16_mobile_team_project_1.R
import com.example.team16_mobile_team_project_1.network.OnlineScore
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
//    factory: GameManagerFactory,
    gameManager: GameManager
) {
//    val gameManager: GameManager = viewModel(factory = factory)

    val gameState by gameManager.gameState.collectAsState()
    val player by gameManager.player.collectAsState()
    val cannons by gameManager.cannons.collectAsState()
    val cannonballs by gameManager.cannonballs.collectAsState()
    val score = gameManager.score
    val onlineLeaderboard by gameManager.onlineLeaderboard.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        AudioManager.initialize(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            AudioManager.release()
        }
    }

    LaunchedEffect(gameState) {
        when (gameState) {
            is GameState.Ready, is GameState.GameOver, is GameState.Paused -> AudioManager.playMenuMusic(
                context
            )

            is GameState.Running, is GameState.Countdown -> AudioManager.playGameMusic(context)
        }
    }

    val playerImage = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.player).asImageBitmap()
    }
    val enemyImage = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.enemy).asImageBitmap()
    }
    val cannonballImage = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.cannonball).asImageBitmap()
    }

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
                // Background image
                Image(
                    painter = painterResource(id = R.drawable.menu_background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )

                MenuCannonballBackground(
                    modifier = Modifier.fillMaxSize()
                )

                StartMenu(onStartClick = {
                    gameManager.startGame()
                    AudioManager.playSound(AudioManager.Sound.SELECT)
                }, modifier = Modifier.align(Alignment.Center))
            }

            is GameState.Running -> {
                // Background image
                Image(
                    painter = painterResource(id = R.drawable.game_background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Darken for visibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.15f))
                )

                GameCanvas(player, cannons, cannonballs, playerImage, enemyImage, cannonballImage)
                Text(
                    "Score: $score",
                    modifier = Modifier.align(Alignment.TopCenter),
                    color = Color.DarkGray,
                    fontSize = 20.sp
                )

                // Pause button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .width(50.dp)
                        .height(50.dp)
                        .clickable {
                            gameManager.pauseGame()
                            AudioManager.playSound(AudioManager.Sound.SELECT)
                        }
                ) {
                    Text(
                        text = "⏸", // pause icon
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 30.sp
                    )
                }
            }

            is GameState.GameOver -> {
                val highScore by gameManager.highScore.collectAsState(initial = 0)

                // Background image
                Image(
                    painter = painterResource(id = R.drawable.gameover_background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )

                GameOverMenu(
                    onlineLeaderboard = onlineLeaderboard,
                    score = state.score,
                    highScore = highScore,
                    onRestartClick = {
                        gameManager.startGame()
                        AudioManager.playSound(AudioManager.Sound.SELECT)
                    },
                    onSubmitScore = { playerName -> gameManager.submitScore(playerName) },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is GameState.Paused -> {
                // Background image
                Image(
                    painter = painterResource(id = R.drawable.pause_background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Draw frozen game scene
                GameCanvas(player, cannons, cannonballs, playerImage, enemyImage, cannonballImage)
                Text(
                    "Score: $score",
                    modifier = Modifier.align(Alignment.TopCenter),
                    fontSize = 24.sp
                )

                PauseMenu(
                    onResume = {
                        gameManager.resumeGame()
                        AudioManager.playSound(AudioManager.Sound.SELECT)
                    },
                    onRestart = {
                        gameManager.startGame()
                        AudioManager.playSound(AudioManager.Sound.SELECT)
                    },
                    onQuit = {
                        gameManager.quitToMenu()
                        AudioManager.playSound(AudioManager.Sound.SELECT)
                    },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is GameState.Countdown -> {
                // Background image
                Image(
                    painter = painterResource(id = R.drawable.game_background),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Darken for visibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.15f))
                )

                // Draw frozen game scene behind
                GameCanvas(player, cannons, cannonballs, playerImage, enemyImage, cannonballImage)
                Text(
                    "Score: $score",
                    modifier = Modifier.align(Alignment.TopCenter),
                    fontSize = 24.sp
                )

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
fun GameCanvas(
    player: Player,
    cannons: List<CannonState>,
    cannonballs: List<CannonballState>,
    playerImage: ImageBitmap,
    enemyImage: ImageBitmap,
    cannonballImage: ImageBitmap
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Draw Kill Zone border
        drawRect(
            color = Color.Red,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 20f)
        )

        // Draw Player
        drawImage(
            image = playerImage,
            dstOffset = IntOffset(
                (player.x - player.radius).toInt(),
                (player.y - player.radius).toInt()
            ),
            dstSize = IntSize(
                (player.radius * 2).toInt(),
                (player.radius * 2).toInt()
            )
        )

        // Draw Cannons
        cannons.forEach { cannon ->
            drawImage(
                image = enemyImage,
                dstOffset = IntOffset(
                    (cannon.x - 25).toInt(),
                    (cannon.y - 25).toInt()
                ),
                dstSize = IntSize(50, 50)
            )
        }

        // Draw Cannonballs
        cannonballs.forEach { cannonball ->
            drawImage(
                image = cannonballImage,
                dstOffset = IntOffset(
                    (cannonball.x - cannonball.radius).toInt(),
                    (cannonball.y - cannonball.radius).toInt()
                ),
                dstSize = IntSize(
                    (cannonball.radius * 2).toInt(),
                    (cannonball.radius * 2).toInt()
                )
            )
        }
    }
}

@Composable
fun StartMenu(onStartClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text("Cannon Crossfire", fontSize = 32.sp, color = Color.White)
        // Start button
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(50.dp)
                .clickable { onStartClick() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.button),
                contentDescription = "Start Game",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            Text(
                text = "Start Game",
                modifier = Modifier.align(Alignment.Center),
                color = Color.Black,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun GameOverMenu(onlineLeaderboard: List<OnlineScore>
                 ,score: Long
                 , highScore: Long
                 , onRestartClick: () -> Unit
                 , onSubmitScore: (String) -> Unit
                 , modifier: Modifier = Modifier) {

    var playerName by remember { mutableStateOf("Player") }
    val isNewHighScore = score > highScore
    Log.d("GameScreen", "Game Over Menu: Score: $score, High Score: $highScore, New High Score: $isNewHighScore")

    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,) {
        Text("Game Over", fontSize = 48.sp, color = Color.Red, fontWeight = FontWeight.Bold)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Top 5 Players",
                fontSize = 22.sp,
                color = Color.Yellow,
                fontWeight = FontWeight.Bold
            )
            onlineLeaderboard.forEachIndexed { index, score ->
                Row(
                    modifier = Modifier.width(200.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                )
                {
                    Text(
                        text = "${index + 1}. ${score.playerName}",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Text(
                        text = score.score.toString(),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Text("Your Current Score: $score", fontSize = 28.sp, color = Color.White)

        // leaderboard submission UI
        if(isNewHighScore){
            Text("New High Score!", fontSize = 24.sp, color = Color.Yellow)

            OutlinedTextField(
                value = playerName,
                onValueChange = { playerName = it},
                label = { Text("Enter your name") },
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { onSubmitScore(playerName)}) {
                    Text("Submit Score")
                }
                Button(onClick = onRestartClick) {
                    Text("Cancel")
                }
            }
        } else {
            // Restart button
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(50.dp)
                    .clickable { onRestartClick() }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.button),
                    contentDescription = "Restart Game",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                Text(
                    text = "Restart Game",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Black,
                    fontSize = 18.sp
                )
            }
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
        Text("Paused", fontSize = 32.sp, color = Color.White)

        // Resume button
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(30.dp)
                .clickable { onResume() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.button),
                contentDescription = "Resume Game",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            Text(
                text = "Resume",
                modifier = Modifier.align(Alignment.Center),
                color = Color.Black,
                fontSize = 16.sp
            )
        }
        // Restart button
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(30.dp)
                .clickable { onRestart() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.button),
                contentDescription = "Restart Game",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            Text(
                text = "Restart",
                modifier = Modifier.align(Alignment.Center),
                color = Color.Black,
                fontSize = 16.sp
            )
        }
        // Quit button
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(30.dp)
                .clickable { onQuit() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.button),
                contentDescription = "Quit Game",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            Text(
                text = "Quit to Menu",
                modifier = Modifier.align(Alignment.Center),
                color = Color.Black,
                fontSize = 13.sp
            )
        }
    }
}

private enum class Side { LEFT, RIGHT, TOP, BOTTOM }

private data class MenuBall(
    val side: Side,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val r: Float = 12f
)

@Composable
fun MenuCannonballBackground(
    modifier: Modifier = Modifier
) {
    var size by remember { mutableStateOf(IntSize.Zero) }

    val balls = remember { mutableStateListOf<MenuBall>() }

    LaunchedEffect(size) {
        if (size.width <= 0 || size.height <= 0) return@LaunchedEffect

        balls.clear()
        val w = size.width.toFloat()
        val h = size.height.toFloat()

        balls.add(spawnBall(Side.LEFT, w, h))
        balls.add(spawnBall(Side.RIGHT, w, h))
        balls.add(spawnBall(Side.TOP, w, h))
        balls.add(spawnBall(Side.BOTTOM, w, h))

        while (true) {
            delay(16)

            val w2 = size.width.toFloat()
            val h2 = size.height.toFloat()

            for (i in balls.indices) {
                val b = balls[i]
                val nx = b.x + b.vx
                val ny = b.y + b.vy

                val out =
                    nx < -b.r * 3 || nx > w2 + b.r * 3 ||
                            ny < -b.r * 3 || ny > h2 + b.r * 3

                balls[i] = if (out) {
                    spawnBall(b.side, w2, h2)
                } else {
                    b.copy(x = nx, y = ny)
                }
            }
        }
    }

    Canvas(
        modifier = modifier.onSizeChanged { size = it }
    ) {
        balls.forEach { b ->
            drawCircle(
                color = Color.DarkGray,
                radius = b.r,
                center = Offset(b.x, b.y)
            )
        }
    }
}

private fun spawnBall(side: Side, w: Float, h: Float): MenuBall {
    val r = 12f
    val speed = 10f

    return when (side) {
        // From left/right, random Y
        Side.LEFT -> MenuBall(
            side = side,
            x = -r,
            y = Random.nextFloat() * h,
            vx = speed,
            vy = 0f,
            r = r
        )

        Side.RIGHT -> MenuBall(
            side = side,
            x = w + r,
            y = Random.nextFloat() * h,
            vx = -speed,
            vy = 0f,
            r = r
        )

        // From top/bottom, random X
        Side.TOP -> MenuBall(
            side = side,
            x = Random.nextFloat() * w,
            y = -r,
            vx = 0f,
            vy = speed,
            r = r
        )

        Side.BOTTOM -> MenuBall(
            side = side,
            x = Random.nextFloat() * w,
            y = h + r,
            vx = 0f,
            vy = -speed,
            r = r
        )
    }
}
