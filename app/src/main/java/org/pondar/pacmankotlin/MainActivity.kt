package org.pondar.pacmankotlin

import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import org.pondar.pacmankotlin.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener {

    // Reference to the game class.
    private lateinit var game: Game
    private lateinit var binding: ActivityMainBinding

    // Base movement speed for pacman and the ghosts.
    private val movementSpeed = 8

    // Timers for the game and pacman.
    private var pacmanTimer: Timer = Timer()
    private var gameTimer: Timer = Timer()
    private var isGameRunning = false
    private var gameStatusTxt = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // Force portrait mode.
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        game = Game(this, binding.pointsView)

        // Initialize the game view class and game class.
        game.setGameView(binding.gameView)
        binding.gameView.setGame(game)
        game.newGame()

        //Initialize the rotation vector sensor.
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorManager.registerListener(this, sensor, 10000)

        // Utilize by calling run function every 20 milliseconds.
        isGameRunning = true
        pacmanTimer.schedule(object : TimerTask() {
            override fun run() {
                pacmanTimerFunction()
            }
        }, 0, 20)
        // Utilize by calling run function every second.
        gameTimer.schedule(object : TimerTask() {
            override fun run() {
                timerCountDown()
            }
        }, 0, 1000)
    }

    // Called by the pacman timer.
    private fun pacmanTimerFunction() {
        if (isGameRunning && !game.isGameOver) {
            if (game.countDownTime > 0) {
                when (game.currentDirection) {
                    game.UP -> game.movePacmanUp(movementSpeed)
                    game.RIGHT -> game.movePacmanRight(movementSpeed)
                    game.DOWN -> game.movePacmanDown(movementSpeed)
                    game.LEFT -> game.movePacmanLeft(movementSpeed)
                }
                binding.gameView.invalidate()
            }
            if (game.isGameWon) {
                game.isGameOver = true
                gameStatusTxt =
                    getString(R.string.levelCompleted, game.currentPoints)
            } else if ((game.countDownTime == 0 && !game.isGameWon) || game.isConsumed) {
                game.isGameOver = true
                gameStatusTxt =
                    getString(R.string.levelLost, game.currentPoints, game.maxPoints)
            }
            if (game.isGameOver) {
                this.runOnUiThread(displayGameStatusToast)
            }
        }
    }

    private val displayGameStatusToast = Runnable() {
        var gameStatusToast =
            Toast.makeText(this, gameStatusTxt, Toast.LENGTH_SHORT)
        gameStatusToast.show()
    }

    // Called by the game timer.
    private fun timerCountDown() {
        if (isGameRunning && !game.isGameOver) {
            game.countDownTime--
            binding.gameTimerView.post {
                binding.gameTimerView.text = getString(R.string.gameTimer, game.countDownTime)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Cancel the timers if the application is stopped
        pacmanTimer.cancel()
        gameTimer.cancel()
    }

    // Event listener for the rotation vector sensor.
    override fun onSensorChanged(event: SensorEvent) {
        if (isGameRunning) {
            // Converts the rotation vector to a rotation matrix.
            var rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

            // Compute the devices orientation based on the orientation matrix.
            var axisRotations = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, axisRotations)

            // Convert the orientation angles from radians to degrees.
            val axisX = axisRotations[1] * 180 / Math.PI
            val axisY = axisRotations[2] * 180 / Math.PI

            // Check the current rotation of the device and move pacman accordingly.
            if (axisY < -20 && axisX < 10 && axisX > -10) {
                game.currentDirection = game.LEFT
            } else if (axisY > 20 && axisX < 10 && axisX > -10) {
                game.currentDirection = game.RIGHT
            } else if (axisX > 20 && axisY < 10 && axisY > -10) {
                game.currentDirection = game.UP
            } else if (axisX < -20 && axisY < 10 && axisY > -10) {
                game.currentDirection = game.DOWN
            }
            game.doCollisionCheck()
        }
    }

    // Necessary override from implementing SensorEventListener.
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    // Handle action bar item clicks here.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            // TODO implement settings menu.
            Toast.makeText(this, "settings clicked", Toast.LENGTH_LONG).show()
            return true
        } else if (id == R.id.action_newGame) {
            Toast.makeText(this, getString(R.string.newGameStarted), Toast.LENGTH_LONG).show()
            game.newGame()
            return true
        } else if (id == R.id.action_pause_resume_game && !game.isGameOver) {
            var toastText =
                if (isGameRunning) getString(R.string.pauseGame) else getString(R.string.resumeGame)
            Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show()
            // Change action item icon.
            item.icon =
                if (isGameRunning)
                    getDrawable(R.drawable.ic_play_arrow_white_24dp)
                else
                    getDrawable(R.drawable.ic_pause_white_24dp)
            isGameRunning = !isGameRunning
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}