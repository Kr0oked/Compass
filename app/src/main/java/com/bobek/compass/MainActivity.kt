/*
 * This file is part of Compass.
 * Copyright (C) 2021 Philipp Bobek <philipp.bobek@mailbox.org>
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
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.bobek.compass.model.MathUtils
import com.bobek.compass.model.RotationVector
import com.bobek.compass.model.SensorAccuracy
import com.bobek.compass.view.CompassView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var compassView: CompassView
    private lateinit var sensorManager: SensorManager

    private var optionsMenu: Menu? = null
    private var sensorAccuracy = SensorAccuracy.NO_CONTACT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        compassView = findViewById(R.id.compass)
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

        sensorManager.registerListener(this, rotationVectorSensor, SENSOR_DELAY_FASTEST)
        Log.i(TAG, "Started compass")
    }

    private fun showSensorErrorDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.sensor_error_message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setCancelable(false)
            .show()
    }

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
        val sensorAccuracyView = layoutInflater.inflate(R.layout.sensor_alert_dialog_view, null)

        sensorAccuracyView.findViewById<AppCompatImageView>(R.id.sensor_accuracy_image)
            .setImageResource(sensorAccuracy.iconResourceId)

        sensorAccuracyView.findViewById<AppCompatTextView>(R.id.sensor_accuracy_text)
            .setText(sensorAccuracy.textResourceId)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.sensor_status)
            .setView(sensorAccuracyView)
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
        val aboutView = layoutInflater.inflate(R.layout.about_alert_dialog_view, null)

        val version = aboutView.findViewById<AppCompatTextView>(R.id.version)
        version.text = getString(R.string.version, BuildConfig.VERSION_NAME)

        val copyright = aboutView.findViewById<AppCompatTextView>(R.id.copyright)
        copyright.movementMethod = LinkMovementMethod.getInstance()

        val license = aboutView.findViewById<AppCompatTextView>(R.id.license)
        license.movementMethod = LinkMovementMethod.getInstance()

        val sourceCode = aboutView.findViewById<AppCompatTextView>(R.id.source_code)
        sourceCode.movementMethod = LinkMovementMethod.getInstance()

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.app_name)
            .setView(aboutView)
            .setNeutralButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        when (sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> updateSensorAccuracy(accuracy)
            else -> Log.w(TAG, "Unexpected accuracy changed event of type ${sensor.type}")
        }
    }

    private fun updateSensorAccuracy(accuracy: Int) {
        Log.v(TAG, "Sensor accuracy value $accuracy")
        sensorAccuracy = adaptAccuracy(accuracy)
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
        val azimuth = MathUtils.calculateAzimuth(rotationVector)
        compassView.update(azimuth)
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
