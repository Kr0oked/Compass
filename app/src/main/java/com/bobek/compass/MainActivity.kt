package com.bobek.compass

import android.hardware.SensorManager
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.annotation.VisibleForTesting
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView

class MainActivity : AppCompatActivity(), CompassListener {

    @VisibleForTesting
    private lateinit var degreeText: TextView

    @VisibleForTesting
    private lateinit var compassRoseImage: ImageView

    @VisibleForTesting
    private lateinit var sensorManager: SensorManager

    @VisibleForTesting
    private lateinit var compass: Compass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        degreeText = findViewById(R.id.compass_degree_text)
        compassRoseImage = findViewById(R.id.compass_rose_image)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        compass = Compass(sensorManager, this)
    }

    override fun onResume() {
        super.onResume()

        try {
            compass.start()
        } catch (exception: AccelerometerNotAvailableException) {
            showErrorDialog(R.string.compass_accelerometer_error_message)
        } catch (exception: MagnetometerNotAvailableException) {
            showErrorDialog(R.string.compass_magnetometer_error_message)
        }
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
        compass.stop()
    }

    override fun onAzimuthChanged(azimuth: Float) {
        runOnUiThread {
            degreeText.text = azimuth.toString()
            compassRoseImage.rotation = azimuth.unaryMinus()
        }
    }
}
