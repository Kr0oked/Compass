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

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationManager.NETWORK_PROVIDER
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.bobek.compass.databinding.FragmentCompassBinding
import com.bobek.compass.databinding.SensorAlertDialogViewBinding
import com.bobek.compass.model.AppError
import com.bobek.compass.model.Azimuth
import com.bobek.compass.model.DisplayRotation
import com.bobek.compass.model.RotationVector
import com.bobek.compass.model.SensorAccuracy
import com.bobek.compass.preference.PreferenceStore
import com.bobek.compass.util.MathUtils
import com.bobek.compass.view.CompassViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

const val OPTION_INSTRUMENTED_TEST = "INSTRUMENTED_TEST"

private const val TAG = "CompassFragment"

private const val LOCATION_UPDATES_MIN_TIME_MS = 1000L
private const val LOCATION_UPDATES_MIN_DISTANCE_M = 10.0f

class CompassFragment : Fragment() {

    private val compassViewModel: CompassViewModel by viewModels()
    private val compassMenuProvider = CompassMenuProvider()
    private val compassSensorEventListener = CompassSensorEventListener()
    private val compassLocationListener = CompassLocationListener()

    private lateinit var binding: FragmentCompassBinding
    private lateinit var preferenceStore: PreferenceStore

    private var sensorManager: SensorManager? = null
    private var locationManager: LocationManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCompassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBinding()
        initPreferenceStore()
        setupSystemServices()
        setupMenu()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.model = compassViewModel
    }

    private fun initPreferenceStore() {
        preferenceStore = PreferenceStore(requireContext(), lifecycle)
        preferenceStore.trueNorth.observe(viewLifecycleOwner) { compassViewModel.trueNorth.value = it }
        preferenceStore.hapticFeedback.observe(viewLifecycleOwner) { compassViewModel.hapticFeedback.value = it }
    }

    private fun setupSystemServices() {
        sensorManager = ActivityCompat.getSystemService(requireContext(), SensorManager::class.java)
        locationManager = ActivityCompat.getSystemService(requireContext(), LocationManager::class.java)
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(compassMenuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onResume() {
        super.onResume()

        if (isInstrumentedTest()) {
            Log.i(TAG, "Skipping registration of sensor listener")
        } else {
            startSystemServiceFunctionalities()
        }

        Log.i(TAG, "Started compass")
    }

    private fun isInstrumentedTest() = requireActivity().intent.extras?.getBoolean(OPTION_INSTRUMENTED_TEST) ?: false

    private fun startSystemServiceFunctionalities() {
        registerSensorListener()

        if (preferenceStore.trueNorth.value == true) {
            handleAccessCoarseLocationPermission()
        }
    }

    private fun registerSensorListener() {
        sensorManager?.also { sensorManager ->
            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.also { rotationVectorSensor ->
                val success = sensorManager.registerListener(
                    compassSensorEventListener,
                    rotationVectorSensor,
                    SensorManager.SENSOR_DELAY_FASTEST
                )
                if (success) {
                    Log.d(TAG, "Registered listener for rotation vector sensor")
                } else {
                    Log.w(TAG, "Could not enable rotation vector sensor")
                    showErrorDialog(AppError.ROTATION_VECTOR_SENSOR_FAILED)
                }
            } ?: run {
                Log.w(TAG, "Rotation vector sensor not available")
                showErrorDialog(AppError.ROTATION_VECTOR_SENSOR_NOT_AVAILABLE)
            }
        } ?: run {
            Log.w(TAG, "SensorManager not present")
            showErrorDialog(AppError.SENSOR_MANAGER_NOT_PRESENT)
        }
    }

    private fun handleAccessCoarseLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED) {
            compassViewModel.accessCoarseLocationPermissionGranted.value = true
            registerCoarseLocationListener()
        } else {
            compassViewModel.accessCoarseLocationPermissionGranted.value = false
        }
    }

    @RequiresPermission(value = ACCESS_COARSE_LOCATION)
    private fun registerCoarseLocationListener() {
        locationManager?.also { locationManager ->
            locationManager.requestLocationUpdates(
                NETWORK_PROVIDER,
                LOCATION_UPDATES_MIN_TIME_MS,
                LOCATION_UPDATES_MIN_DISTANCE_M,
                compassLocationListener
            )
        } ?: run {
            Log.w(TAG, "LocationManager not present")
            showErrorDialog(AppError.LOCATION_MANAGER_NOT_PRESENT)
        }
    }

    private fun showErrorDialog(appError: AppError) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.error)
            .setIcon(R.drawable.ic_error)
            .setMessage(getString(R.string.error_message, getString(appError.messageId), appError.name))
            .setNeutralButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(compassSensorEventListener)
        locationManager?.removeUpdates(compassLocationListener)
        Log.i(TAG, "Stopped compass")
    }


    private inner class CompassSensorEventListener : SensorEventListener {

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            when (sensor.type) {
                Sensor.TYPE_ROTATION_VECTOR -> setSensorAccuracy(accuracy)
                else -> Log.w(TAG, "Unexpected accuracy changed event of type ${sensor.type}")
            }
        }

        private fun setSensorAccuracy(accuracy: Int) {
            val sensorAccuracy = adaptSensorAccuracy(accuracy)
            setSensorAccuracy(sensorAccuracy)
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
            val magneticAzimuth = MathUtils.calculateAzimuth(rotationVector, displayRotation)

            if (compassViewModel.trueNorth.value == true) {
                val magneticDeclination = getMagneticDeclination()
                val trueAzimuth = magneticAzimuth.plus(magneticDeclination)
                setAzimuth(trueAzimuth)
            } else {
                setAzimuth(magneticAzimuth)
            }
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
            return if (VERSION.SDK_INT >= VERSION_CODES.R) {
                requireContext().display
            } else {
                requireActivity().windowManager.defaultDisplay
            }
        }

        private fun getMagneticDeclination(): Float {
            return compassViewModel.location.value
                ?.let(MathUtils::getMagneticDeclination)
                ?: 0.0f
        }
    }


    private inner class CompassLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.v(TAG, "Location changed to $location")
            compassViewModel.location.value = location
        }
    }


    private inner class CompassMenuProvider : MenuProvider {

        private var optionsMenu: Menu? = null

        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_metronome, menu)
            optionsMenu = menu
            compassViewModel.sensorAccuracy.observe(viewLifecycleOwner) { updateSensorStatusIcon(it) }
            preferenceStore.screenOrientationLocked.observe(viewLifecycleOwner) { updateScreenRotationIcon(it) }
        }

        private fun updateSensorStatusIcon(sensorAccuracy: SensorAccuracy) {
            optionsMenu
                ?.findItem(R.id.action_sensor_status)
                ?.setIcon(sensorAccuracy.iconResourceId)
        }

        private fun updateScreenRotationIcon(screenOrientationLocked: Boolean) {
            optionsMenu
                ?.findItem(R.id.action_screen_rotation)
                ?.setIcon(getScreenRotationIcon(screenOrientationLocked))
        }

        @DrawableRes
        private fun getScreenRotationIcon(screenOrientationLocked: Boolean): Int =
            if (screenOrientationLocked) R.drawable.ic_screen_rotation_lock else R.drawable.ic_screen_rotation

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

        private fun showSensorStatusPopup() {
            val alertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
            val dialogContextInflater = LayoutInflater.from(alertDialogBuilder.context)

            val dialogBinding = SensorAlertDialogViewBinding.inflate(dialogContextInflater, null, false)
            dialogBinding.model = compassViewModel
            dialogBinding.lifecycleOwner = viewLifecycleOwner

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
    }


    internal fun setSensorAccuracy(sensorAccuracy: SensorAccuracy) {
        compassViewModel.sensorAccuracy.value = sensorAccuracy
        Log.v(TAG, "Sensor accuracy $sensorAccuracy")
    }

    internal fun setAzimuth(azimuth: Azimuth) {
        compassViewModel.azimuth.value = azimuth
        Log.v(TAG, "Azimuth $azimuth")
    }
}
