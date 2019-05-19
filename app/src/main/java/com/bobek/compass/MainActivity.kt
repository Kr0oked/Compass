package com.bobek.compass

import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_MAGNETIC_FIELD
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_GAME
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import kotlin.math.roundToInt

private const val TAG = "MainActivity"
private const val SENSOR_SAMPLING_PERIOD_US = SENSOR_DELAY_GAME
private const val LOW_PASS_FILTER_ALPHA = 0.03f
private const val X = 0
private const val Y = 1
private const val Z = 2

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var degreeText: TextView
    private lateinit var compassRoseImage: ImageView

    private lateinit var sensorManager: SensorManager

    private lateinit var compass: Compass
    private lateinit var accelerometerFilter: LowPassFilter
    private lateinit var magnetometerFilter: LowPassFilter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        degreeText = findViewById(R.id.compass_degree_text)
        compassRoseImage = findViewById(R.id.compass_rose_image)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        compass = Compass()
        accelerometerFilter = LowPassFilter(LOW_PASS_FILTER_ALPHA)
        magnetometerFilter = LowPassFilter(LOW_PASS_FILTER_ALPHA)
    }

    override fun onResume() {
        super.onResume()

        reset()

        val accelerometerSuccess = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)?.let { accelerometer ->
            sensorManager.registerListener(this, accelerometer, SENSOR_SAMPLING_PERIOD_US)
        } ?: false

        if (!accelerometerSuccess) {
            showErrorDialog(R.string.compass_accelerometer_error_message)
        }

        val magnetometerSuccess = sensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD)?.let { magnetometer ->
            sensorManager.registerListener(this, magnetometer, SENSOR_SAMPLING_PERIOD_US)
        } ?: false

        if (!magnetometerSuccess) {
            stopSensorEventListening()
            showErrorDialog(R.string.compass_magnetometer_error_message)
        }
    }

    private fun reset() {
        accelerometerFilter.reset()
        magnetometerFilter.reset()
        compass.reset()
    }

    private fun showErrorDialog(@StringRes messageId: Int) {
        AlertDialog.Builder(this)
            .setMessage(messageId)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setCancelable(false)
            .create()
            .show()
    }

    override fun onPause() {
        super.onPause()
        stopSensorEventListening()
    }

    private fun stopSensorEventListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            TYPE_ACCELEROMETER -> handleAccelerometerValues(event.values)
            TYPE_MAGNETIC_FIELD -> handleMagneticFieldValues(event.values)
            else -> Log.wtf(TAG, "Unexpected sensor event of type ${event.sensor.type}")
        }
    }

    private fun handleAccelerometerValues(values: FloatArray) {
        Log.v(TAG, "Accelerometer - X: ${values[X]} Y: ${values[Y]} Z: ${values[Z]}")
        SensorValues(values[X], values[Y], values[Z])
            .let { sensorValues -> accelerometerFilter.filter(sensorValues) }
            .let { filteredValues -> compass.handleAccelerometerValues(filteredValues) }
            ?.let { azimuth -> updateCompass(azimuth) }
    }

    private fun handleMagneticFieldValues(values: FloatArray) {
        Log.v(TAG, "Magnetic Field - X: ${values[X]} Y: ${values[Y]} Z: ${values[Z]}")
        SensorValues(values[X], values[Y], values[Z])
            .let { sensorValues -> magnetometerFilter.filter(sensorValues) }
            .let { filteredValues -> compass.handleMagneticFieldValues(filteredValues) }
            ?.let { azimuth -> updateCompass(azimuth) }
    }

    private fun updateCompass(azimuth: Float) {
        runOnUiThread {
            degreeText.text = azimuth.roundToInt().toString()
            compassRoseImage.rotation = azimuth.unaryMinus()
        }
    }
}
