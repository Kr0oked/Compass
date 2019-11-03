/*
 * This file is part of Compass.
 * Copyright (C) 2019 Philipp Bobek <philipp.bobek@mailbox.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Compass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.bobek.compass

import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_MAGNETIC_FIELD
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_GAME
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import com.bobek.compass.sensor.LowPassFilter
import com.bobek.compass.sensor.SensorFilter
import com.bobek.compass.sensor.SensorHandler
import com.bobek.compass.sensor.SensorValues
import com.bobek.compass.view.Compass
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"
private const val SAMPLING_PERIOD_US = SENSOR_DELAY_GAME
private const val FILTER_TIME_CONSTANT = 0.18f
private const val STATE_NIGHT_MODE = "night-mode"
private const val X = 0
private const val Y = 1
private const val Z = 2

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var compass: Compass

    private lateinit var sensorManager: SensorManager

    private lateinit var sensorHandler: SensorHandler
    private lateinit var accelerometerFilter: SensorFilter
    private lateinit var magnetometerFilter: SensorFilter

    private var nightMode: Int = MODE_NIGHT_FOLLOW_SYSTEM

    override fun onCreate(savedInstanceState: Bundle?) {
        initializeNightMode(savedInstanceState)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        compass = findViewById(R.id.compass)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        sensorHandler = SensorHandler()
        accelerometerFilter = LowPassFilter(FILTER_TIME_CONSTANT)
        magnetometerFilter = LowPassFilter(FILTER_TIME_CONSTANT)
    }

    private fun initializeNightMode(savedInstanceState: Bundle?) {
        nightMode = savedInstanceState?.getInt(STATE_NIGHT_MODE) ?: nightMode
        setDefaultNightMode(nightMode)
        Log.d(TAG, "Initialized night mode with value $nightMode")
    }

    override fun onResume() {
        super.onResume()

        compass.visibility = GONE

        val accelerometer = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)
        if (accelerometer == null) {
            showErrorDialog(R.string.compass_accelerometer_error_message)
            Log.w(TAG, "No accelerometer available")
            return
        }

        val magnetometer = sensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD)
        if (magnetometer == null) {
            showErrorDialog(R.string.compass_magnetometer_error_message)
            Log.w(TAG, "No magnetometer available")
            return
        }

        sensorManager.registerListener(this, accelerometer, SAMPLING_PERIOD_US)
        sensorManager.registerListener(this, magnetometer, SAMPLING_PERIOD_US)

        compass.visibility = VISIBLE

        Log.i(TAG, "Initialized compass")
    }

    private fun showErrorDialog(@StringRes messageId: Int) {
        MaterialAlertDialogBuilder(this)
            .setMessage(messageId)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setCancelable(false)
            .create()
            .show()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        accelerometerFilter.reset()
        magnetometerFilter.reset()
        sensorHandler.reset()

        Log.i(TAG, "Stopped compass")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu.findItem(R.id.action_night_mode)
            .setIcon(getNightModeIcon())
        return true
    }

    @DrawableRes
    private fun getNightModeIcon(): Int {
        return when (nightMode) {
            MODE_NIGHT_NO -> R.drawable.ic_night_mode_no
            MODE_NIGHT_YES -> R.drawable.ic_night_mode_yes
            else -> R.drawable.ic_night_mode_auto
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_night_mode -> {
                toggleNightMode()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleNightMode() {
        nightMode = when (nightMode) {
            MODE_NIGHT_NO -> MODE_NIGHT_YES
            MODE_NIGHT_YES -> MODE_NIGHT_FOLLOW_SYSTEM
            else -> MODE_NIGHT_NO
        }

        recreate()

        Log.d(TAG, "Changed night mode to value $nightMode and scheduled recreation of activity")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_NIGHT_MODE, nightMode)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            TYPE_ACCELEROMETER -> handleAccelerometerValues(event.values, event.timestamp)
            TYPE_MAGNETIC_FIELD -> handleMagneticFieldValues(event.values, event.timestamp)
            else -> Log.w(TAG, "Unexpected sensor event of type ${event.sensor.type}")
        }
    }

    private fun handleAccelerometerValues(values: FloatArray, timestamp: Long) {
        Log.v(TAG, "Accelerometer - X: ${values[X]} Y: ${values[Y]} Z: ${values[Z]}")
        SensorValues(values[X], values[Y], values[Z], timestamp)
            .let { sensorValues -> accelerometerFilter.filter(sensorValues) }
            .let { filteredValues -> sensorHandler.handleAccelerometerValues(filteredValues) }
            ?.let { azimuth -> compass.setDegrees(azimuth) }
    }

    private fun handleMagneticFieldValues(values: FloatArray, timestamp: Long) {
        Log.v(TAG, "Magnetometer - X: ${values[X]} Y: ${values[Y]} Z: ${values[Z]}")
        SensorValues(values[X], values[Y], values[Z], timestamp)
            .let { sensorValues -> magnetometerFilter.filter(sensorValues) }
            .let { filteredValues -> sensorHandler.handleMagneticFieldValues(filteredValues) }
            ?.let { azimuth -> compass.setDegrees(azimuth) }
    }
}
