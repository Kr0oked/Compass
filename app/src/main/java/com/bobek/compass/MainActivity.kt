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
import android.view.*
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import com.bobek.compass.databinding.AboutAlertDialogViewBinding
import com.bobek.compass.databinding.ActivityMainBinding
import com.bobek.compass.databinding.SensorAlertDialogViewBinding
import com.bobek.compass.model.Azimuth
import com.bobek.compass.model.DisplayRotation
import com.bobek.compass.model.RotationVector
import com.bobek.compass.model.SensorAccuracy
import com.bobek.compass.preference.PreferenceStore
import com.bobek.compass.util.MathUtils
import com.bobek.compass.view.ObservableSensorAccuracy
import com.google.android.material.dialog.MaterialAlertDialogBuilder

const val OPTION_INSTRUMENTED_TEST = "INSTRUMENTED_TEST"

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), SensorEventListener {

    private val observableSensorAccuracy = ObservableSensorAccuracy(SensorAccuracy.NO_CONTACT)

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var preferenceStore: PreferenceStore

    private var optionsMenu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        initPreferenceStore()
    }

    private fun initPreferenceStore() {
        preferenceStore = PreferenceStore(this)
        preferenceStore.screenOrientationLocked.observe(this) { setScreenRotationMode(it) }
        preferenceStore.nightMode.observe(this) { setNightMode(it) }
        Log.d(TAG, "Initialized preference store")
    }

    override fun onResume() {
        super.onResume()

        if (isInstrumentedTest()) {
            Log.i(TAG, "Skipping registration of sensor listener")
        } else {
            registerSensorListener()
        }

        Log.i(TAG, "Started compass")
    }

    private fun registerSensorListener() {
        val rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationVectorSensor == null) {
            Log.w(TAG, "Rotation vector sensor not available")
            showSensorErrorDialog()
            return
        }

        val success = sensorManager.registerListener(this, rotationVectorSensor, SENSOR_DELAY_FASTEST)
        if (!success) {
            Log.w(TAG, "Could not enable rotation vector sensor")
            showSensorErrorDialog()
            return
        }
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

    override fun onDestroy() {
        super.onDestroy()
        closePreferenceStore()
    }

    private fun closePreferenceStore() {
        preferenceStore.close()
        Log.d(TAG, "Closed preference store")
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
                toggleRotationScreenLocked()
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
        val alertDialogBuilder = MaterialAlertDialogBuilder(this)
        val dialogContextInflater = LayoutInflater.from(alertDialogBuilder.context)

        val dialogBinding = SensorAlertDialogViewBinding.inflate(dialogContextInflater, null, false)
        dialogBinding.sensorAccuracy = observableSensorAccuracy

        alertDialogBuilder
            .setTitle(R.string.sensor_status)
            .setView(dialogBinding.root)
            .setNeutralButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun toggleRotationScreenLocked() {
        preferenceStore.screenOrientationLocked.value?.let {
            preferenceStore.screenOrientationLocked.value = it.not()
        }
    }

    private fun toggleNightMode() {
        preferenceStore.nightMode.value?.let {
            when (it) {
                MODE_NIGHT_NO -> preferenceStore.nightMode.value = MODE_NIGHT_YES
                MODE_NIGHT_YES -> preferenceStore.nightMode.value = MODE_NIGHT_FOLLOW_SYSTEM
                else -> preferenceStore.nightMode.value = MODE_NIGHT_NO
            }
        }
    }

    private fun showAboutPopup() {
        val alertDialogBuilder = MaterialAlertDialogBuilder(this)
        val dialogContextInflater = LayoutInflater.from(alertDialogBuilder.context)

        val dialogBinding = AboutAlertDialogViewBinding.inflate(dialogContextInflater, null, false)
        dialogBinding.version = BuildConfig.VERSION_NAME
        dialogBinding.copyrightText.movementMethod = LinkMovementMethod.getInstance()
        dialogBinding.licenseText.movementMethod = LinkMovementMethod.getInstance()
        dialogBinding.sourceCodeText.movementMethod = LinkMovementMethod.getInstance()

        alertDialogBuilder
            .setTitle(R.string.app_name)
            .setView(dialogBinding.root)
            .setNeutralButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        when (sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> setSensorAccuracy(accuracy)
            else -> Log.w(TAG, "Unexpected accuracy changed event of type ${sensor.type}")
        }
    }

    private fun setSensorAccuracy(accuracy: Int) {
        Log.v(TAG, "Sensor accuracy value $accuracy")
        val sensorAccuracy = adaptSensorAccuracy(accuracy)
        setSensorAccuracy(sensorAccuracy)
    }

    internal fun setSensorAccuracy(sensorAccuracy: SensorAccuracy) {
        observableSensorAccuracy.set(sensorAccuracy)
        updateSensorStatusIcon()
    }

    private fun adaptSensorAccuracy(accuracy: Int): SensorAccuracy {
        return when (accuracy) {
            SENSOR_STATUS_NO_CONTACT -> SensorAccuracy.NO_CONTACT
            SENSOR_STATUS_UNRELIABLE -> SensorAccuracy.UNRELIABLE
            SENSOR_STATUS_ACCURACY_LOW -> SensorAccuracy.LOW
            SENSOR_STATUS_ACCURACY_MEDIUM -> SensorAccuracy.MEDIUM
            SENSOR_STATUS_ACCURACY_HIGH -> SensorAccuracy.HIGH
            else -> {
                Log.w(TAG, "Encountered unexpected sensor accuracy value '$accuracy'")
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
        binding.contentMain.compass.setAzimuth(azimuth)
        Log.v(TAG, "Azimuth $azimuth")
    }

    private fun setScreenRotationMode(screenOrientationLocked: Boolean) {
        if (screenOrientationLocked) {
            setScreenRotationMode(SCREEN_ORIENTATION_LOCKED)
        } else {
            setScreenRotationMode(SCREEN_ORIENTATION_UNSPECIFIED)
        }
    }

    private fun setScreenRotationMode(screenOrientation: Int) {
        Log.d(TAG, "Setting requested orientation to value $screenOrientation")
        requestedOrientation = screenOrientation
        updateScreenRotationIcon()
    }

    private fun setNightMode(@NightMode mode: Int) {
        Log.d(TAG, "Setting night mode to value $mode")
        setDefaultNightMode(mode)
        updateNightModeIcon()
    }

    private fun updateSensorStatusIcon() {
        val sensorAccuracy = observableSensorAccuracy.get() ?: SensorAccuracy.NO_CONTACT

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
        return when (preferenceStore.nightMode.value) {
            MODE_NIGHT_NO -> R.drawable.ic_light_mode
            MODE_NIGHT_YES -> R.drawable.ic_dark_mode
            else -> R.drawable.ic_auto_mode
        }
    }
}
