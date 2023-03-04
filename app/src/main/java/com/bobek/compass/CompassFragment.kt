/*
 * This file is part of Compass.
 * Copyright (C) 2023 Philipp Bobek <philipp.bobek@mailbox.org>
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

import android.content.Context.SENSOR_SERVICE
import android.content.pm.ActivityInfo
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.DrawableRes
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.bobek.compass.databinding.FragmentCompassBinding
import com.bobek.compass.databinding.SensorAlertDialogViewBinding
import com.bobek.compass.model.Azimuth
import com.bobek.compass.model.DisplayRotation
import com.bobek.compass.model.RotationVector
import com.bobek.compass.model.SensorAccuracy
import com.bobek.compass.preference.PreferenceStore
import com.bobek.compass.util.MathUtils
import com.bobek.compass.view.ObservableSensorAccuracy
import com.google.android.material.dialog.MaterialAlertDialogBuilder

private const val TAG = "CompassFragment"

class CompassFragment : Fragment(), SensorEventListener {

    private val observableSensorAccuracy = ObservableSensorAccuracy(SensorAccuracy.NO_CONTACT)

    private lateinit var binding: FragmentCompassBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var preferenceStore: PreferenceStore

    private var optionsMenu: Menu? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCompassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBinding()
        setupSensorManager()
        setupMenu()
        initPreferenceStore()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun setupSensorManager() {
        sensorManager = requireActivity().getSystemService(SENSOR_SERVICE) as SensorManager
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(getMenuProvider(), viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun initPreferenceStore() {
        preferenceStore = PreferenceStore(requireContext(), lifecycle)
        preferenceStore.screenOrientationLocked.observe(viewLifecycleOwner) { setScreenRotationMode(it) }
    }

    private fun setScreenRotationMode(screenOrientationLocked: Boolean) {
        if (screenOrientationLocked) {
            setScreenRotationMode(ActivityInfo.SCREEN_ORIENTATION_LOCKED)
        } else {
            setScreenRotationMode(SCREEN_ORIENTATION_UNSPECIFIED)
        }
    }

    private fun setScreenRotationMode(screenOrientation: Int) {
        Log.d(TAG, "Setting requested orientation to value $screenOrientation")
        requireActivity().requestedOrientation = screenOrientation
        updateScreenRotationIcon()
    }

    private fun getMenuProvider() = object : MenuProvider {

        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_metronome, menu)
            this@CompassFragment.optionsMenu = menu
            updateSensorStatusIcon()
            updateScreenRotationIcon()
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.action_sensor_status -> {
                    showSensorStatusPopup()
                    true
                }
                R.id.action_screen_rotation -> {
                    toggleRotationScreenLocked()
                    true
                }
                R.id.action_settings -> {
                    showSettings()
                    true
                }
                else -> false
            }
        }
    }

    private fun showSensorStatusPopup() {
        val alertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
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

    private fun showSettings() {
        findNavController().navigate(R.id.action_CompassFragment_to_SettingsFragment)
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

        val success = sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_FASTEST)
        if (!success) {
            Log.w(TAG, "Could not enable rotation vector sensor")
            showSensorErrorDialog()
            return
        }
    }

    private fun showSensorErrorDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.sensor_error_message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setCancelable(false)
            .show()
    }

    private fun isInstrumentedTest() = requireActivity().intent.extras?.getBoolean(OPTION_INSTRUMENTED_TEST) ?: false

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        Log.i(TAG, "Stopped compass")
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
            SensorManager.SENSOR_STATUS_NO_CONTACT -> SensorAccuracy.NO_CONTACT
            SensorManager.SENSOR_STATUS_UNRELIABLE -> SensorAccuracy.UNRELIABLE
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> SensorAccuracy.LOW
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> SensorAccuracy.MEDIUM
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> SensorAccuracy.HIGH
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
            requireActivity().display
        } else {
            requireActivity().windowManager.defaultDisplay
        }
    }

    internal fun setAzimuth(azimuth: Azimuth) {
        binding.compass.setAzimuth(azimuth)
        Log.v(TAG, "Azimuth $azimuth")
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
        return when (requireActivity().requestedOrientation) {
            SCREEN_ORIENTATION_UNSPECIFIED -> R.drawable.ic_screen_rotation
            else -> R.drawable.ic_screen_rotation_lock
        }
    }
}
