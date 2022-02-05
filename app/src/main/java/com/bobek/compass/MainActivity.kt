/*
 * This file is part of Compass.
 * Copyright (C) 2022 Philipp Bobek <philipp.bobek@mailbox.org>
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.bobek.compass

import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LOCKED
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.*
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Display
import android.view.Menu
import android.view.MenuItem
import android.view.Surface
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import com.bobek.compass.databinding.AboutAlertDialogViewBinding
import com.bobek.compass.databinding.ActivityMainBinding
import com.bobek.compass.databinding.SensorAlertDialogViewBinding
import com.bobek.compass.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder

const val OPTION_INSTRUMENTED_TEST = "INSTRUMENTED_TEST"

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var sensorAlertDialogBinding: SensorAlertDialogViewBinding
    private lateinit var aboutAlertDialogBinding: AboutAlertDialogViewBinding
    private lateinit var sensorManager: SensorManager

    private var optionsMenu: Menu? = null
    private var sensorAccuracy = SensorAccuracy.NO_CONTACT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        sensorAlertDialogBinding = SensorAlertDialogViewBinding.inflate(layoutInflater)
        aboutAlertDialogBinding = AboutAlertDialogViewBinding.inflate(layoutInflater)

        setContentView(mainBinding.root)
        setSupportActionBar(mainBinding.toolbar)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
    }

    override fun onResume() {
        super.onResume()

        val rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationVectorSensor == null) {
            showSensorErrorDialog()
            Log.w(TAG, "Rotation vector sensor not available")
            return
        }

        if (isInstrumentedTest()) {
            Log.i(TAG, "Skipping registration of sensor listener")
        } else {
            sensorManager.registerListener(this, rotationVectorSensor, SENSOR_DELAY_FASTEST)
        }

        Log.i(TAG, "Started compass")
    }

    private fun showSensorErrorDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.sensor_error_message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setCancelable(false)
            .show()
    }

    private fun isInstrumentedTest() = intent.extras?.getBoolean(OPTION_INSTRUMENTED_TEST) ?: false

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        Log.i(TAG, "Stopped compass")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        optionsMenu = menu
        updateSensorStatusIcon()
        updateScreenRotationIcon()
        updateNightModeIcon()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sensor_status -> {
                showSensorStatusPopup()
                true
            }
            R.id.action_screen_rotation -> {
                toggleScreenRotationMode()
                true
            }
            R.id.action_night_mode -> {
                toggleNightMode()
                true
            }
            R.id.action_about -> {
                showAboutPopup()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSensorStatusPopup() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.sensor_status)
            .setView(sensorAlertDialogBinding.root)
            .setNeutralButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun toggleScreenRotationMode() {
        when (requestedOrientation) {
            SCREEN_ORIENTATION_UNSPECIFIED -> changeScreenRotationMode(SCREEN_ORIENTATION_LOCKED)
            else -> changeScreenRotationMode(SCREEN_ORIENTATION_UNSPECIFIED)
        }
    }

    private fun changeScreenRotationMode(screenOrientation: Int) {
        Log.d(TAG, "Setting requested orientation to value $screenOrientation")
        requestedOrientation = screenOrientation
        updateScreenRotationIcon()
    }

    private fun toggleNightMode() {
        when (getDefaultNightMode()) {
            MODE_NIGHT_NO -> changeNightMode(MODE_NIGHT_YES)
            MODE_NIGHT_YES -> changeNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
            else -> changeNightMode(MODE_NIGHT_NO)
        }
    }

    private fun changeNightMode(@NightMode mode: Int) {
        Log.d(TAG, "Setting night mode to value $mode")
        setDefaultNightMode(mode)
        updateNightModeIcon()
    }

    private fun showAboutPopup() {
        aboutAlertDialogBinding.version.text = getString(R.string.version, BuildConfig.VERSION_NAME)
        aboutAlertDialogBinding.copyright.movementMethod = LinkMovementMethod.getInstance()
        aboutAlertDialogBinding.license.movementMethod = LinkMovementMethod.getInstance()
        aboutAlertDialogBinding.sourceCode.movementMethod = LinkMovementMethod.getInstance()

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.app_name)
            .setView(aboutAlertDialogBinding.root)
            .setNeutralButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        when (sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> setAccuracy(accuracy)
            else -> Log.w(TAG, "Unexpected accuracy changed event of type ${sensor.type}")
        }
    }

    private fun setAccuracy(accuracy: Int) {
        Log.v(TAG, "Sensor accuracy value $accuracy")
        val sensorAccuracy = adaptAccuracy(accuracy)
        setAccuracy(sensorAccuracy)
    }

    internal fun setAccuracy(accuracy: SensorAccuracy) {
        sensorAccuracy = accuracy
        sensorAlertDialogBinding.sensorAccuracyImage.setImageResource(sensorAccuracy.iconResourceId)
        sensorAlertDialogBinding.sensorAccuracyText.setText(sensorAccuracy.textResourceId)
        updateSensorStatusIcon()
    }

    private fun adaptAccuracy(accuracy: Int): SensorAccuracy {
        return when (accuracy) {
            SENSOR_STATUS_NO_CONTACT -> SensorAccuracy.NO_CONTACT
            SENSOR_STATUS_UNRELIABLE -> SensorAccuracy.UNRELIABLE
            SENSOR_STATUS_ACCURACY_LOW -> SensorAccuracy.LOW
            SENSOR_STATUS_ACCURACY_MEDIUM -> SensorAccuracy.MEDIUM
            SENSOR_STATUS_ACCURACY_HIGH -> SensorAccuracy.HIGH
            else -> {
                Log.w(TAG, "Encountered unexpected sensor accuracy '$sensorAccuracy'")
                SensorAccuracy.NO_CONTACT
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> updateCompass(event)
            else -> Log.w(TAG, "Unexpected sensor changed event of type ${event.sensor.type}")
        }
    }

    private fun updateCompass(event: SensorEvent) {
        val rotationVector = RotationVector(event.values[0], event.values[1], event.values[2])
        val displayRotation = getDisplayRotation()
        val azimuth = MathUtils.calculateAzimuth(rotationVector, displayRotation)
        setAzimuth(azimuth)
    }

    private fun getDisplayRotation(): DisplayRotation {
        return when (getDisplayCompat()?.rotation) {
            Surface.ROTATION_90 -> DisplayRotation.ROTATION_90
            Surface.ROTATION_180 -> DisplayRotation.ROTATION_180
            Surface.ROTATION_270 -> DisplayRotation.ROTATION_270
            else -> DisplayRotation.ROTATION_0
        }
    }

    private fun getDisplayCompat(): Display? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display
        } else {
            windowManager.defaultDisplay
        }
    }

    internal fun setAzimuth(azimuth: Azimuth) {
        mainBinding.contentMain.compass.setAzimuth(azimuth)
        Log.v(TAG, "Azimuth $azimuth")
    }

    private fun updateSensorStatusIcon() {
        optionsMenu
            ?.findItem(R.id.action_sensor_status)
            ?.setIcon(sensorAccuracy.iconResourceId)
    }

    private fun updateScreenRotationIcon() {
        optionsMenu
            ?.findItem(R.id.action_screen_rotation)
            ?.setIcon(getScreenRotationIcon())
    }

    @DrawableRes
    private fun getScreenRotationIcon(): Int {
        return when (requestedOrientation) {
            SCREEN_ORIENTATION_UNSPECIFIED -> R.drawable.ic_screen_rotation
            else -> R.drawable.ic_screen_rotation_lock
        }
    }

    private fun updateNightModeIcon() {
        optionsMenu
            ?.findItem(R.id.action_night_mode)
            ?.setIcon(getNightModeIcon())
    }

    @DrawableRes
    private fun getNightModeIcon(): Int {
        return when (getDefaultNightMode()) {
            MODE_NIGHT_NO -> R.drawable.ic_night_mode_no
            MODE_NIGHT_YES -> R.drawable.ic_night_mode_yes
            else -> R.drawable.ic_night_mode_auto
        }
    }
}
