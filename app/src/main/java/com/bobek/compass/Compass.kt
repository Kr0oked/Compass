package com.bobek.compass

import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_MAGNETIC_FIELD
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.*
import android.util.Log

private const val TAG = "Compass"
private const val SENSOR_SAMPLING_PERIOD_US = SENSOR_DELAY_UI
private const val X = 0
private const val Y = 1
private const val Z = 2
private const val AZIMUTH = 0
private const val AXIS_SIZE = 3
private const val ROTATION_MATRIX_SIZE = 9

class Compass(
    private val sensorManager: SensorManager,
    private val compassListener: CompassListener
) : SensorEventListener {

    private val accelerometerReading = FloatArray(AXIS_SIZE)
    private val magnetometerReading = FloatArray(AXIS_SIZE)

    fun start() {
        startListeningToAccelerometerEvents()
        startListeningToMagnetometerEvents()
    }

    private fun startListeningToAccelerometerEvents() {
        val success = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)
            ?.let { accelerometer -> sensorManager.registerListener(this, accelerometer, SENSOR_SAMPLING_PERIOD_US) }
            ?: false

        if (!success) {
            throw AccelerometerNotAvailableException()
        }
    }

    private fun startListeningToMagnetometerEvents() {
        val success = sensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD)
            ?.let { magnetometer -> sensorManager.registerListener(this, magnetometer, SENSOR_SAMPLING_PERIOD_US) }
            ?: false

        if (!success) {
            stop()
            throw MagnetometerNotAvailableException()
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        when (accuracy) {
            SENSOR_STATUS_ACCURACY_HIGH -> Log.v(TAG, "Accuracy of ${sensor.name} changed to HIGH")
            SENSOR_STATUS_ACCURACY_MEDIUM -> Log.v(TAG, "Accuracy of ${sensor.name} changed to MEDIUM")
            SENSOR_STATUS_ACCURACY_LOW -> Log.v(TAG, "Accuracy of ${sensor.name} changed to LOW")
            SENSOR_STATUS_UNRELIABLE -> Log.v(TAG, "Accuracy of ${sensor.name} changed to UNRELIABLE")
            SENSOR_STATUS_NO_CONTACT -> Log.v(TAG, "Accuracy of ${sensor.name} changed to NO_CONTACT")
            else -> Log.wtf(TAG, "Accuracy of ${sensor.name} changed to unknown status: $accuracy")
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            TYPE_ACCELEROMETER -> handleAccelerometerValues(event.values)
            TYPE_MAGNETIC_FIELD -> handleMagneticFieldValues(event.values)
            else -> Log.wtf(TAG, "Unexpected sensor event of type ${event.sensor.type}")
        }
    }

    private fun handleAccelerometerValues(values: FloatArray) {
        Log.v(TAG, "Accelerometer - X: ${values[X]} Y: ${values[Y]} Z: ${values[Z]}")
        values.copyInto(accelerometerReading)
        updateAzimuth()
    }

    private fun handleMagneticFieldValues(values: FloatArray) {
        Log.v(TAG, "Magnetic Field - X: ${values[X]} Y: ${values[Y]} Z: ${values[Z]}")
        values.copyInto(magnetometerReading)
        updateAzimuth()
    }

    private fun updateAzimuth() {
        val rotationMatrix = FloatArray(ROTATION_MATRIX_SIZE)

        val success = getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)

        if (success) {
            val azimuth = calculateAzimuth(rotationMatrix)
            Log.v(TAG, "Calculated azimuth of $azimuth")
            compassListener.onAzimuthChanged(azimuth)
        } else {
            Log.v(TAG, "Failed to calculate rotation matrix")
        }
    }

    private fun calculateAzimuth(rotationMatrix: FloatArray): Float {
        val orientationAnglesInRadians = getOrientation(rotationMatrix, FloatArray(AXIS_SIZE))
        val azimuthInRadians = orientationAnglesInRadians[AZIMUTH]
        val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
        return MathUtils.normalizeAngle(azimuthInDegrees)
    }
}
