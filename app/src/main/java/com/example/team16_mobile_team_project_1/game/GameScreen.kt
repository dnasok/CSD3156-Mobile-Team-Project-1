package com.example.team16_mobile_team_project_1.game

import android.graphics.BitmapFactory
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.team16_mobile_team_project_1.R
import com.example.team16_mobile_team_project_1.network.OnlineScore
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * The main screen for the game, which displays the game state and handles user input.
 *
 * @param modifier The modifier to be applied to the screen.
 * @param gameManager The GameManager that manages the game's state.
 */
@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    gameManager: GameManager
) {

    val gameState by gameManager.gameState.collectAsState()
    val player by gameManager.player.collectAsState()
    val cannons by gameManager.cannons.collectAsState()
    val cannonballs by gameManager.cannonballs.collectAsState()
    val coin by gameManager.coin.collectAsState()
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
    val coinImage = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.coin).asImageBitmap()
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
                    modifier = Modifier.fillMaxSize(),
                    cannonballImage = cannonballImage
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

                GameCanvas(
                    player,
                    cannons,
                    cannonballs,
                    coin,
                    playerImage,
                    enemyImage,
                    cannonballImage,
                    coinImage
                )
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
                    isNewHighScore = state.isNewHighScore,
                    onRestartClick = {
                        gameManager.quitToMenu()
                        AudioManager.playSound(AudioManager.Sound.SELECT)
                    },
                    onSubmitScore = { playerName ->
                        gameManager.submitScore(playerName)
                        gameManager.fetchOnlineLeaderboard()
                        gameManager.fetchOnlineLeaderboard()
                    },
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
                GameCanvas(
                    player,
                    cannons,
                    cannonballs,
                    coin,
                    playerImage,
                    enemyImage,
                    cannonballImage,
                    coinImage
                )
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
                GameCanvas(
                    player,
                    cannons,
                    cannonballs,
                    coin,
                    playerImage,
                    enemyImage,
                    cannonballImage,
                    coinImage
                )
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

/**
 * The canvas where the game is drawn.
 *
 * @param player The player object.
 * @param cannons The list of cannon objects.
 * @param cannonballs The list of cannonball objects.
 * @param coin The coin object.
 * @param playerImage The image for the player.
 * @param enemyImage The image for the cannons.
 * @param cannonballImage The image for the cannonballs.
 * @param coinImage The image for the coin.
 */
@Composable
fun GameCanvas(
    player: Player,
    cannons: List<Cannon>,
    cannonballs: List<Cannonball>,
    coin: Coin?,
    playerImage: ImageBitmap,
    enemyImage: ImageBitmap,
    cannonballImage: ImageBitmap,
    coinImage: ImageBitmap
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
                    (cannon.x - cannon.radius).toInt(),
                    (cannon.y - cannon.radius).toInt()
                ),
                dstSize = IntSize(
                    (
                            cannon.radius * 2).toInt(),
                    (cannon.radius * 2).toInt()
                )
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

        // Draw Coin
        coin?.let {
            drawImage(
                image = coinImage,
                dstOffset = IntOffset(
                    (it.x - it.radius).toInt(),
                    (it.y - it.radius).toInt()
                ),
                dstSize = IntSize(
                    (it.radius * 2).toInt(),
                    (it.radius * 2).toInt()
                )
            )
        }
    }
}

/**
 * The start menu of the game.
 *
 * @param onStartClick The action to perform when the start button is clicked.
 * @param modifier The modifier to be applied to the menu.
 */
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

/**
 * The game over menu.
 *
 * @param onlineLeaderboard The list of online scores.
 * @param score The player's score.
 * @param highScore The player's high score.
 * @param isNewHighScore Whether the player achieved a new high score.
 * @param onRestartClick The action to perform when the restart button is clicked.
 * @param onSubmitScore The action to perform when the submit score button is clicked.
 * @param modifier The modifier to be applied to the menu.
 */
@Composable
fun GameOverMenu(
    onlineLeaderboard: List<OnlineScore>,
    score: Long,
    highScore: Long,
    isNewHighScore: Boolean,
    onRestartClick: () -> Unit,
    onSubmitScore: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    var playerName by remember { mutableStateOf("Player") }
//    val isNewHighScore = score > highScore


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {

        Text("Game Over", fontSize = 48.sp, color = Color.Red, fontWeight = FontWeight.Bold)
        if (onlineLeaderboard.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    "Local High Score",
                    fontSize = 22.sp,
                    color = Color.Yellow,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = highScore.toString(),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
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
        }
        Text("Your Current Score: $score", fontSize = 28.sp, color = Color.White)

        // leaderboard submission UI
        if (isNewHighScore) {
            Text("New High Score!", fontSize = 24.sp, color = Color.Yellow)

            OutlinedTextField(
                value = playerName,
                onValueChange = { playerName = it },
                label = { Text("Enter your name") },
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(30.dp)
                        .clickable { onSubmitScore(playerName) }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.button),
                        contentDescription = "Submit Score",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )

                    Text(
                        text = "Submit Score",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Black,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(30.dp)
                        .clickable { onRestartClick() }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.button),
                        contentDescription = "Back to Menu",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )

                    Text(
                        text = "Back to Menu",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Black,
                        fontSize = 12.sp
                    )
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

/**
 * The pause menu of the game.
 *
 * @param onResume The action to perform when the resume button is clicked.
 * @param onRestart The action to perform when the restart button is clicked.
 * @param onQuit The action to perform when the quit button is clicked.
 * @param modifier The modifier to be applied to the menu.
 */
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

/**
 * A background for the main menu, with cannonballs flying across the screen.
 *
 * @param modifier The modifier to be applied to the background.
 * @param cannonballImage The image for the cannonballs.
 */
@Composable
fun MenuCannonballBackground(
    modifier: Modifier = Modifier,
    cannonballImage: ImageBitmap
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
            drawImage(
                image = cannonballImage,
                dstOffset = IntOffset(
                    (b.x - b.r).toInt(),
                    (b.y - b.r).toInt()
                ),
                dstSize = IntSize(
                    (b.r * 2).toInt(),
                    (b.r * 2).toInt()
                )
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
