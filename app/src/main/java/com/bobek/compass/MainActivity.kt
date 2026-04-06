/*
 * This file is part of Compass.
 * Copyright (C) 2025 Philipp Bobek <philipp.bobek@mailbox.org>
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
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Log
import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.bobek.compass.data.AppError
import com.bobek.compass.data.DisplayRotation
import com.bobek.compass.data.LocationStatus
import com.bobek.compass.data.RotationVector
import com.bobek.compass.data.SensorAccuracy
import com.bobek.compass.settings.SettingsRepository
import com.bobek.compass.ui.MainContent
import com.bobek.compass.util.MathUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"
const val OPTION_INSTRUMENTED_TEST = "OPTION_INSTRUMENTED_TEST"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    internal val viewModel: CompassViewModel by viewModels()
    private val accessLocationPermissionRequest = registerAccessLocationPermissionRequest()

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private var sensorManager: SensorManager? = null
    private var locationManager: LocationManager? = null
    private var locationRequestCancellationSignal: CancellationSignal? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainContent(
                viewModel = viewModel,
                onLocationReload = ::requestLocation
            )
        }

        sensorManager = getSystemService(SensorManager::class.java)
        locationManager = getSystemService(LocationManager::class.java)

        lifecycleScope.launch {
            settingsRepository.getTrueNorth().collect { trueNorth ->
                setupTrueNorthFunctionality(trueNorth)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!intent.getBooleanExtra(OPTION_INSTRUMENTED_TEST, false)) {
            registerSensorListener()
            requestLocation()
        }
    }

    private fun registerSensorListener() {
        val sensorManager = sensorManager ?: run {
            Log.w(TAG, "SensorManager not present")
            showErrorDialog(AppError.SENSOR_MANAGER_NOT_PRESENT)
            return
        }

        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationVectorSensor != null) {
            val success = sensorManager.registerListener(
                compassSensorEventListener,
                rotationVectorSensor,
                SensorManager.SENSOR_DELAY_FASTEST
            )
            if (!success) {
                Log.w(TAG, "Could not enable rotation vector sensor")
                showErrorDialog(AppError.ROTATION_VECTOR_SENSOR_FAILED)
            }
        } else {
            Log.w(TAG, "Rotation vector sensor not available")
            showErrorDialog(AppError.ROTATION_VECTOR_SENSOR_NOT_AVAILABLE)
        }

        val magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (magneticFieldSensor != null) {
            val success = sensorManager.registerListener(
                compassSensorEventListener,
                magneticFieldSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            if (!success) {
                Log.w(TAG, "Could not enable magnetic field sensor")
                showErrorDialog(AppError.MAGNETIC_FIELD_SENSOR_FAILED)
            }
        } else {
            Log.w(TAG, "Magnetic field sensor not available")
            showErrorDialog(AppError.MAGNETIC_FIELD_SENSOR_NOT_AVAILABLE)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(compassSensorEventListener)
        locationRequestCancellationSignal?.cancel()
    }

    private fun setSensorAccuracy(accuracy: Int) {
        val sensorAccuracy = when (accuracy) {
            SensorManager.SENSOR_STATUS_NO_CONTACT -> SensorAccuracy.NO_CONTACT
            SensorManager.SENSOR_STATUS_UNRELIABLE -> SensorAccuracy.UNRELIABLE
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> SensorAccuracy.LOW
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> SensorAccuracy.MEDIUM
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> SensorAccuracy.HIGH
            else -> SensorAccuracy.NO_CONTACT
        }
        viewModel.setSensorAccuracy(sensorAccuracy)
    }

    private fun updateCompass(event: SensorEvent) {
        val rotationVector = RotationVector(event.values[0], event.values[1], event.values[2])
        val displayRotation = getDisplayRotation()
        val magneticAzimuth = MathUtils.calculateAzimuth(rotationVector, displayRotation)

        if (viewModel.getTrueNorthFlow().value) {
            val location = viewModel.getLocationFlow().value
            if (location != null) {
                val magneticDeclination = MathUtils.getMagneticDeclination(location)
                viewModel.setAzimuth(magneticAzimuth + magneticDeclination)
            } else {
                viewModel.setAzimuth(magneticAzimuth)
            }
        } else {
            viewModel.setAzimuth(magneticAzimuth)
        }
    }

    @Suppress("DEPRECATION")
    private fun getDisplayRotation(): DisplayRotation {
        val rotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.rotation ?: Surface.ROTATION_0
        } else {
            windowManager.defaultDisplay.rotation
        }
        return when (rotation) {
            Surface.ROTATION_90 -> DisplayRotation.ROTATION_90
            Surface.ROTATION_180 -> DisplayRotation.ROTATION_180
            Surface.ROTATION_270 -> DisplayRotation.ROTATION_270
            else -> DisplayRotation.ROTATION_0
        }
    }

    private suspend fun setupTrueNorthFunctionality(trueNorth: Boolean?) {
        if (trueNorth == true) {
            handleLocationPermission()
        }
    }

    private suspend fun handleLocationPermission() {
        if (neverRequestedAccessLocationPermission() && accessLocationPermissionDenied()) {
            startAccessLocationPermissionRequestWorkflow()
        }
    }

    private suspend fun neverRequestedAccessLocationPermission(): Boolean =
        settingsRepository.getAccessLocationPermissionRequested().first().not()

    private fun accessLocationPermissionDenied() =
        ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_DENIED
                && ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_DENIED

    private fun startAccessLocationPermissionRequestWorkflow() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_COARSE_LOCATION)
            || ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)
        ) {
            showRequestNotificationsPermissionRationale()
        } else {
            launchAccessLocationPermissionRequest()
        }
    }

    private fun showRequestNotificationsPermissionRationale() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.access_location_permission_rationale_title)
            .setMessage(R.string.access_location_permission_rationale_message)
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                launchAccessLocationPermissionRequest()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.no_thanks) { dialog, _ ->
                Log.i(TAG, "Continuing without requesting location permission")
                lifecycleScope.launch {
                    settingsRepository.setAccessLocationPermissionRequested(true)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun launchAccessLocationPermissionRequest() {
        Log.i(TAG, "Requesting location permission")
        accessLocationPermissionRequest.launch(arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION))
    }

    private val compassSensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                setSensorAccuracy(accuracy)
            }
        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                updateCompass(event)
            }
        }
    }

    private fun registerAccessLocationPermissionRequest() =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions[ACCESS_FINE_LOCATION] == true -> {
                    Log.i(TAG, "Permission ACCESS_FINE_LOCATION granted")
                    requestLocation()
                }

                permissions[ACCESS_COARSE_LOCATION] == true -> {
                    Log.i(TAG, "Permission ACCESS_COARSE_LOCATION granted")
                    requestLocation()
                }

                else -> {
                    Log.i(TAG, "Location permission denied")
                    viewModel.setLocationStatus(LocationStatus.PERMISSION_DENIED)
                    lifecycleScope.launch {
                        settingsRepository.setAccessLocationPermissionRequested(true)
                    }
                }
            }
        }

    fun requestLocation() {
        val locationManager = locationManager ?: run {
            Log.w(TAG, "LocationManager not present")
            showErrorDialog(AppError.LOCATION_MANAGER_NOT_PRESENT)
            return
        }
        if (!viewModel.getTrueNorthFlow().value) return

        if (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
        ) {
            viewModel.setLocationStatus(LocationStatus.PERMISSION_DENIED)
            return
        }

        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
            Log.w(TAG, "Location is disabled")
            viewModel.setLocationStatus(LocationStatus.NOT_PRESENT)
            showErrorDialog(AppError.LOCATION_DISABLED)
            return
        }

        val provider = getBestLocationProvider() ?: run {
            Log.w(TAG, "No LocationProvider available")
            viewModel.setLocationStatus(LocationStatus.NOT_PRESENT)
            showErrorDialog(AppError.NO_LOCATION_PROVIDER_AVAILABLE)
            return
        }

        viewModel.setLocationStatus(LocationStatus.LOADING)
        locationRequestCancellationSignal?.cancel()
        locationRequestCancellationSignal = CancellationSignal()
        LocationManagerCompat.getCurrentLocation(
            locationManager,
            provider,
            locationRequestCancellationSignal,
            ContextCompat.getMainExecutor(this)
        ) { location -> setLocation(location) }
    }

    private fun showErrorDialog(appError: AppError) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.error)
            .setIcon(R.drawable.ic_error)
            .setMessage(getString(R.string.error_message, getString(appError.messageId), appError.name))
            .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun getBestLocationProvider(): String? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED ->
            LocationManager.FUSED_PROVIDER

        ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED ->
            LocationManager.GPS_PROVIDER

        ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED ->
            LocationManager.NETWORK_PROVIDER

        else -> null
    }

    private fun setLocation(location: Location?) {
        viewModel.setLocation(location)
        viewModel.setLocationStatus(if (location != null) LocationStatus.PRESENT else LocationStatus.NOT_PRESENT)
    }
}
