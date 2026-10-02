/*
 * This file is part of Compass.
 * Copyright (C) 2026 Philipp Bobek <philipp.bobek@mailbox.org>
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

package com.bobek.compass.ui.compass

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobek.compass.data.CompassReading
import com.bobek.compass.data.LocationStatus
import com.bobek.compass.data.SensorAccuracy
import com.bobek.compass.settings.SettingsRepository
import com.bobek.compass.util.CompassReadingCalculator
import com.bobek.compass.util.MagneticFieldStrengthFilter
import com.bobek.compass.util.MathUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

private val SETTINGS_DEBOUNCE = 1.seconds
private val SENSOR_ACCURACY_DEBOUNCE = 1.seconds

interface ICompassViewModel {
    fun getCompassReadingFlow(): StateFlow<CompassReading>
    fun setDeviceRotation(rotationMatrix: FloatArray)
    fun getSensorAccuracyFlow(): StateFlow<SensorAccuracy>
    fun setSensorAccuracy(sensorAccuracy: SensorAccuracy)
    fun getShouldAutoShowSensorStatusDialogFlow(): StateFlow<Boolean>
    fun onSensorStatusDialogAutoShown()
    fun getTrueNorthFlow(): StateFlow<Boolean>
    fun setTrueNorth(trueNorth: Boolean)
    fun getHapticFeedbackFlow(): StateFlow<Boolean>
    fun setHapticFeedback(hapticFeedback: Boolean)
    fun getScreenOrientationLocked(): StateFlow<Boolean>
    fun setScreenOrientationLocked(screenOrientationLocked: Boolean)
    fun getSightingModeFlow(): StateFlow<Boolean>
    fun setSightingMode(sightingMode: Boolean)
    fun getAutoShowSensorStatusDialogEnabledFlow(): StateFlow<Boolean>
    fun setAutoShowSensorStatusDialogEnabled(autoShowSensorStatusDialogEnabled: Boolean)
    fun getShowMagneticFieldStrengthFlow(): StateFlow<Boolean>
    fun setShowMagneticFieldStrength(showMagneticFieldStrength: Boolean)
    fun setMagneticField(magneticField: FloatArray)
    fun getMagneticFieldStrengthFlow(): StateFlow<Float?>
    fun getExpectedMagneticFieldStrengthFlow(): StateFlow<Float?>
    fun getLocationFlow(): StateFlow<Location?>
    fun setLocation(location: Location?)
    fun getLocationStatusFlow(): StateFlow<LocationStatus>
    fun setLocationStatus(locationStatus: LocationStatus)
}

@HiltViewModel
@OptIn(FlowPreview::class)
class CompassViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel(), ICompassViewModel {

    private val deviceRotationFlow = MutableStateFlow<FloatArray?>(null)

    private val rawSensorAccuracyFlow = MutableStateFlow(SensorAccuracy.UNKNOWN)

    // Debounced so a brief accuracy blip (e.g. the sensor briefly re-settling during a screen
    // rotation) never reaches the icon, the dialog, or the auto-show logic below.
    private val sensorAccuracyFlow: StateFlow<SensorAccuracy> =
        rawSensorAccuracyFlow
            .debounce(SENSOR_ACCURACY_DEBOUNCE)
            .stateIn(viewModelScope, SharingStarted.Eagerly, SensorAccuracy.UNKNOWN)

    private val shouldAutoShowSensorStatusDialogFlow = MutableStateFlow(false)

    private var sensorStatusDialogShownForCurrentAccuracyDrop = false

    private val trueNorthFlow = MutableStateFlow(false)

    private val hapticFeedbackFlow = MutableStateFlow(true)

    private val screenOrientationLockedFlow = MutableStateFlow(false)

    private val sightingModeFlow = MutableStateFlow(true)

    private val autoShowSensorStatusDialogEnabledFlow = MutableStateFlow(true)

    private val showMagneticFieldStrengthFlow = MutableStateFlow(false)

    private val locationFlow = MutableStateFlow<Location?>(null)

    private val locationStatusFlow = MutableStateFlow(LocationStatus.NOT_PRESENT)

    private val compassReadingFlow: StateFlow<CompassReading> =
        combine(deviceRotationFlow, trueNorthFlow, locationFlow, sightingModeFlow, ::RotationInput)
            .scan(CompassReading.INITIAL) { previous, input ->
                val rotationMatrix = input.rotationMatrix ?: return@scan previous
                val declination = if (input.trueNorth && input.location != null) {
                    MathUtils.getMagneticDeclination(input.location)
                } else {
                    0f
                }
                CompassReadingCalculator.next(previous, rotationMatrix, declination, input.sightingModeEnabled)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, CompassReading.INITIAL)

    private val magneticFieldFlow = MutableStateFlow<FloatArray?>(null)

    private val magneticFieldStrengthFlow: StateFlow<Float?> =
        magneticFieldFlow
            .filterNotNull()
            .scan(null as Float?) { previous, field ->
                MagneticFieldStrengthFilter.next(previous, field[0], field[1], field[2])
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Follows the last known location regardless of the true-north setting; it's still accurate
    // for where the user is.
    private val expectedMagneticFieldStrengthFlow: StateFlow<Float?> =
        locationFlow
            .map { location -> location?.let(MathUtils::getExpectedMagneticFieldStrength) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch {
            initFromSettings()
        }

        setupFlowsToSettings()
        observeSensorAccuracyForAutoShowDialog()
    }

    private fun observeSensorAccuracyForAutoShowDialog() {
        viewModelScope.launch {
            sensorAccuracyFlow.collect { accuracy ->
                if (accuracy == SensorAccuracy.HIGH) {
                    sensorStatusDialogShownForCurrentAccuracyDrop = false
                } else if (
                    accuracy.isWarning &&
                    !sensorStatusDialogShownForCurrentAccuracyDrop &&
                    autoShowSensorStatusDialogEnabledFlow.value
                ) {
                    sensorStatusDialogShownForCurrentAccuracyDrop = true
                    shouldAutoShowSensorStatusDialogFlow.value = true
                }
            }
        }
    }

    private suspend fun initFromSettings() {
        settingsRepository.getTrueNorth().firstOrNull()?.let { trueNorthFlow.value = it }
        settingsRepository.getHapticFeedback().firstOrNull()?.let { hapticFeedbackFlow.value = it }
        settingsRepository.getScreenOrientationLocked().firstOrNull()?.let { screenOrientationLockedFlow.value = it }
        settingsRepository.getSightingMode().firstOrNull()?.let { sightingModeFlow.value = it }
        settingsRepository.getAutoShowSensorStatusDialogEnabled().firstOrNull()
            ?.let { autoShowSensorStatusDialogEnabledFlow.value = it }
        settingsRepository.getShowMagneticFieldStrength().firstOrNull()
            ?.let { showMagneticFieldStrengthFlow.value = it }
    }

    private fun setupFlowsToSettings() {
        viewModelScope.launch {
            trueNorthFlow.drop(1).debounce(SETTINGS_DEBOUNCE)
                .collect { settingsRepository.setTrueNorth(it) }
        }
        viewModelScope.launch {
            hapticFeedbackFlow.drop(1).debounce(SETTINGS_DEBOUNCE)
                .collect { settingsRepository.setHapticFeedback(it) }
        }
        viewModelScope.launch {
            screenOrientationLockedFlow.drop(1).debounce(SETTINGS_DEBOUNCE)
                .collect { settingsRepository.setScreenOrientationLocked(it) }
        }
        viewModelScope.launch {
            sightingModeFlow.drop(1).debounce(SETTINGS_DEBOUNCE)
                .collect { settingsRepository.setSightingMode(it) }
        }
        viewModelScope.launch {
            autoShowSensorStatusDialogEnabledFlow.drop(1).debounce(SETTINGS_DEBOUNCE)
                .collect { settingsRepository.setAutoShowSensorStatusDialogEnabled(it) }
        }
        viewModelScope.launch {
            showMagneticFieldStrengthFlow.drop(1).debounce(SETTINGS_DEBOUNCE)
                .collect { settingsRepository.setShowMagneticFieldStrength(it) }
        }
    }

    override fun getCompassReadingFlow() = compassReadingFlow

    override fun setDeviceRotation(rotationMatrix: FloatArray) {
        deviceRotationFlow.value = rotationMatrix
    }

    override fun getSensorAccuracyFlow() = sensorAccuracyFlow

    override fun setSensorAccuracy(sensorAccuracy: SensorAccuracy) {
        rawSensorAccuracyFlow.value = sensorAccuracy
    }

    override fun getShouldAutoShowSensorStatusDialogFlow() = shouldAutoShowSensorStatusDialogFlow

    override fun onSensorStatusDialogAutoShown() {
        shouldAutoShowSensorStatusDialogFlow.value = false
    }

    override fun getTrueNorthFlow() = trueNorthFlow


    override fun setTrueNorth(trueNorth: Boolean) {
        trueNorthFlow.value = trueNorth
    }

    override fun getHapticFeedbackFlow() = hapticFeedbackFlow

    override fun setHapticFeedback(hapticFeedback: Boolean) {
        hapticFeedbackFlow.value = hapticFeedback
    }

    override fun getScreenOrientationLocked() = screenOrientationLockedFlow

    override fun setScreenOrientationLocked(screenOrientationLocked: Boolean) {
        screenOrientationLockedFlow.value = screenOrientationLocked
    }

    override fun getSightingModeFlow() = sightingModeFlow

    override fun setSightingMode(sightingMode: Boolean) {
        sightingModeFlow.value = sightingMode
    }

    override fun getAutoShowSensorStatusDialogEnabledFlow() = autoShowSensorStatusDialogEnabledFlow

    override fun setAutoShowSensorStatusDialogEnabled(autoShowSensorStatusDialogEnabled: Boolean) {
        autoShowSensorStatusDialogEnabledFlow.value = autoShowSensorStatusDialogEnabled
    }

    override fun getShowMagneticFieldStrengthFlow() = showMagneticFieldStrengthFlow

    override fun setShowMagneticFieldStrength(showMagneticFieldStrength: Boolean) {
        showMagneticFieldStrengthFlow.value = showMagneticFieldStrength
    }

    override fun setMagneticField(magneticField: FloatArray) {
        magneticFieldFlow.value = magneticField
    }

    override fun getMagneticFieldStrengthFlow() = magneticFieldStrengthFlow

    override fun getExpectedMagneticFieldStrengthFlow() = expectedMagneticFieldStrengthFlow

    override fun getLocationFlow() = locationFlow

    override fun setLocation(location: Location?) {
        locationFlow.value = location
    }

    override fun getLocationStatusFlow() = locationStatusFlow

    override fun setLocationStatus(locationStatus: LocationStatus) {
        locationStatusFlow.value = locationStatus
    }
}

private class RotationInput(
    val rotationMatrix: FloatArray?,
    val trueNorth: Boolean,
    val location: Location?,
    val sightingModeEnabled: Boolean
)

class ComposeCompassViewModel(
    val compassReading: CompassReading = CompassReading.INITIAL,
    val sensorAccuracy: SensorAccuracy = SensorAccuracy.UNKNOWN,
    val trueNorth: Boolean = false,
    val hapticFeedback: Boolean = true,
    val screenOrientationLocked: Boolean = true,
    val sightingMode: Boolean = true,
    val autoShowSensorStatusDialogEnabled: Boolean = true,
    val showMagneticFieldStrength: Boolean = false,
    val magneticFieldStrength: Float? = null,
    val expectedMagneticFieldStrength: Float? = null,
    val location: Location? = Location(""),
    val locationStatus: LocationStatus = LocationStatus.NOT_PRESENT
) : ICompassViewModel {
    override fun getCompassReadingFlow() = MutableStateFlow(compassReading)
    override fun setDeviceRotation(rotationMatrix: FloatArray) = Unit
    override fun getSensorAccuracyFlow() = MutableStateFlow(sensorAccuracy)
    override fun setSensorAccuracy(sensorAccuracy: SensorAccuracy) = Unit
    override fun getShouldAutoShowSensorStatusDialogFlow() = MutableStateFlow(false)
    override fun onSensorStatusDialogAutoShown() = Unit
    override fun getTrueNorthFlow() = MutableStateFlow(trueNorth)
    override fun setTrueNorth(trueNorth: Boolean) = Unit
    override fun getHapticFeedbackFlow() = MutableStateFlow(hapticFeedback)
    override fun setHapticFeedback(hapticFeedback: Boolean) = Unit
    override fun getScreenOrientationLocked() = MutableStateFlow(screenOrientationLocked)
    override fun setScreenOrientationLocked(screenOrientationLocked: Boolean) = Unit
    override fun getSightingModeFlow() = MutableStateFlow(sightingMode)
    override fun setSightingMode(sightingMode: Boolean) = Unit
    override fun getAutoShowSensorStatusDialogEnabledFlow() = MutableStateFlow(autoShowSensorStatusDialogEnabled)
    override fun setAutoShowSensorStatusDialogEnabled(autoShowSensorStatusDialogEnabled: Boolean) = Unit
    override fun getShowMagneticFieldStrengthFlow() = MutableStateFlow(showMagneticFieldStrength)
    override fun setShowMagneticFieldStrength(showMagneticFieldStrength: Boolean) = Unit
    override fun setMagneticField(magneticField: FloatArray) = Unit
    override fun getMagneticFieldStrengthFlow() = MutableStateFlow(magneticFieldStrength)
    override fun getExpectedMagneticFieldStrengthFlow() = MutableStateFlow(expectedMagneticFieldStrength)
    override fun getLocationFlow() = MutableStateFlow(location)
    override fun setLocation(location: Location?) = Unit
    override fun getLocationStatusFlow() = MutableStateFlow(locationStatus)
    override fun setLocationStatus(locationStatus: LocationStatus) = Unit
}
