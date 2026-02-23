package com.example.team16_mobile_team_project_1

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.team16_mobile_team_project_1.database.AppDatabase
import com.example.team16_mobile_team_project_1.database.ScoreRepository
import com.example.team16_mobile_team_project_1.game.AudioManager
import com.example.team16_mobile_team_project_1.game.GameManager
import com.example.team16_mobile_team_project_1.game.GameManagerFactory
import com.example.team16_mobile_team_project_1.game.GameScreen
import com.example.team16_mobile_team_project_1.game.GameState
import com.example.team16_mobile_team_project_1.network.RetrofitInstance
import com.example.team16_mobile_team_project_1.ui.theme.Team16_Mobile_Team_Project_1Theme

/**
 * The main activity of the application, which hosts the game.
 */
class MainActivity : ComponentActivity(), SensorEventListener {
    //    private val gameManager by viewModels<GameManager>()
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gameManager: GameManager? = null

    /**
     * Called when the activity is first created. This is where you should do all of your normal
     * static set up: create views, bind data to lists, etc. This method also provides you with a
     * Bundle containing the activity's previously frozen state, if there was one.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     * Note: Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val scoreRepository = ScoreRepository(database.highScoreDao(), RetrofitInstance.api)
        val gameManagerFactory = GameManagerFactory(scoreRepository)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
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

    /**
     * Called after onRestoreInstanceState(Bundle), onRestart(), or onPause(), for your activity to start
     * interacting with the user. This is a good place to begin animations, open exclusive-access
     * devices (such as the camera), etc.
     */
    override fun onResume() {
        super.onResume()
        accelerometer?.also { accel ->
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME)
        }
        gameManager?.let {
            AudioManager.resumeMusicForLifecycle(this, it.gameState.value)
        }
    }

    /**
     * Called as part of the activity lifecycle when an activity is going into the background, but has
     * not (yet) been killed. The counterpart to onResume().
     */
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        gameManager?.pauseGame()
        AudioManager.pauseMusicForLifecycle()
    }

    /**
     * Called when the accuracy of the registered sensor has changed. Unlike onSensorChanged(SensorEvent),
     * this is only called when this value changes.
     *
     * @param sensor The Sensor that has a new accuracy value.
     * @param accuracy The new accuracy of this sensor.
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Can be left empty for this implementation
    }

    /**
     * Called when there is a new sensor event. Note that "on changed" is somewhat of a misnomer, as
     * this is called at a rate determined by the sensor update frequency; so we have to call this
     * method even if the sensor values have not changed.
     *
     * @param event The SensorEvent.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        gameManager?.let { manager ->
            if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                val accelX = event.values[0]
                val accelY = event.values[1]

                if (manager.gameState.value is GameState.Running) {
                    manager.onSensorChanged(newAccelX = accelX, newAccelY = accelY)
                }
            }
        }
    }
}
