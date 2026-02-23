package com.example.team16_mobile_team_project_1

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.team16_mobile_team_project_1.database.AppDatabase
import com.example.team16_mobile_team_project_1.database.ScoreRepository
import com.example.team16_mobile_team_project_1.game.GameManager
import com.example.team16_mobile_team_project_1.game.GameManagerFactory
import com.example.team16_mobile_team_project_1.game.GameScreen
import com.example.team16_mobile_team_project_1.network.RetrofitInstance
import com.example.team16_mobile_team_project_1.ui.theme.Team16_Mobile_Team_Project_1Theme

class MainActivity : ComponentActivity(), SensorEventListener {
//    private val gameManager by viewModels<GameManager>()
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gameManager: GameManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val scoreRepository = ScoreRepository(database.highScoreDao(), RetrofitInstance.api)
        val gameManagerFactory = GameManagerFactory(scoreRepository)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            Team16_Mobile_Team_Project_1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val manager: GameManager = viewModel(factory = gameManagerFactory)

                    this.gameManager = manager

                    GameScreen(
                        gameManager = manager,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { accel ->
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Can be left empty for this implementation
    }

    override fun onSensorChanged(event: SensorEvent?) {
        gameManager?.let {manager ->
            if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                val accelX = event.values[0]
                val accelY = event.values[1]

                if (manager.gameState.value is com.example.team16_mobile_team_project_1.game.GameState.Running) {
                    manager.onSensorChanged(newAccelX = accelX, newAccelY = accelY)
                }
            }
        }
    }
}
