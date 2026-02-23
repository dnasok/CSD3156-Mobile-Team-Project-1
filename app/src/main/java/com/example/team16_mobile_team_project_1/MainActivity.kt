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
import com.example.team16_mobile_team_project_1.game.GameManager
import com.example.team16_mobile_team_project_1.game.GameScreen
import com.example.team16_mobile_team_project_1.ui.theme.Team16_Mobile_Team_Project_1Theme

class MainActivity : ComponentActivity(), SensorEventListener {
    private val gameManager by viewModels<GameManager>()
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            Team16_Mobile_Team_Project_1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameScreen(
                        modifier = Modifier.padding(innerPadding),
                        gameManager = gameManager
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
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val accelX = event.values[0]
            val accelY = event.values[1]

            if (gameManager.gameState.value is com.example.team16_mobile_team_project_1.game.GameState.Running) {
                gameManager.onSensorChanged(newAccelX = accelX, newAccelY = accelY)
            }
        }
    }
}
