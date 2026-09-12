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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

private val SETTINGS_DEBOUNCE = 1.seconds

interface ICompassViewModel {
    fun getCompassReadingFlow(): StateFlow<CompassReading>
    fun setDeviceRotation(rotationMatrix: FloatArray)
    fun getSensorAccuracyFlow(): StateFlow<SensorAccuracy>
    fun setSensorAccuracy(sensorAccuracy: SensorAccuracy)
    fun getTrueNorthFlow(): StateFlow<Boolean>
    fun setTrueNorth(trueNorth: Boolean)
    fun getHapticFeedbackFlow(): StateFlow<Boolean>
    fun setHapticFeedback(hapticFeedback: Boolean)
    fun getScreenOrientationLocked(): StateFlow<Boolean>
    fun setScreenOrientationLocked(screenOrientationLocked: Boolean)
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

    private val sensorAccuracyFlow = MutableStateFlow(SensorAccuracy.NO_CONTACT)

    private val trueNorthFlow = MutableStateFlow(false)

    private val hapticFeedbackFlow = MutableStateFlow(true)

    private val screenOrientationLockedFlow = MutableStateFlow(false)

    private val locationFlow = MutableStateFlow<Location?>(null)

    private val locationStatusFlow = MutableStateFlow(LocationStatus.NOT_PRESENT)

    private val compassReadingFlow: StateFlow<CompassReading> =
        combine(deviceRotationFlow, trueNorthFlow, locationFlow, ::RotationInput)
            .scan(CompassReading.INITIAL) { previous, input ->
                val rotationMatrix = input.rotationMatrix ?: return@scan previous
                val declination = if (input.trueNorth && input.location != null) {
                    MathUtils.getMagneticDeclination(input.location)
                } else {
                    0f
                }
                CompassReadingCalculator.next(previous, rotationMatrix, declination)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, CompassReading.INITIAL)

    init {
        viewModelScope.launch {
            initFromSettings()
        }

        setupFlowsToSettings()
    }

    private suspend fun initFromSettings() {
        settingsRepository.getTrueNorth().firstOrNull()?.let { trueNorthFlow.value = it }
        settingsRepository.getHapticFeedback().firstOrNull()?.let { hapticFeedbackFlow.value = it }
        settingsRepository.getScreenOrientationLocked().firstOrNull()?.let { screenOrientationLockedFlow.value = it }
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
    }

    override fun getCompassReadingFlow() = compassReadingFlow

    override fun setDeviceRotation(rotationMatrix: FloatArray) {
        deviceRotationFlow.value = rotationMatrix
    }

    override fun getSensorAccuracyFlow() = sensorAccuracyFlow

    override fun setSensorAccuracy(sensorAccuracy: SensorAccuracy) {
        sensorAccuracyFlow.value = sensorAccuracy
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
    val location: Location?
)

class ComposeCompassViewModel(
    val compassReading: CompassReading = CompassReading.INITIAL,
    val sensorAccuracy: SensorAccuracy = SensorAccuracy.NO_CONTACT,
    val trueNorth: Boolean = false,
    val hapticFeedback: Boolean = true,
    val screenOrientationLocked: Boolean = true,
    val location: Location? = Location(""),
    val locationStatus: LocationStatus = LocationStatus.NOT_PRESENT
) : ICompassViewModel {
    override fun getCompassReadingFlow() = MutableStateFlow(compassReading)
    override fun setDeviceRotation(rotationMatrix: FloatArray) = Unit
    override fun getSensorAccuracyFlow() = MutableStateFlow(sensorAccuracy)
    override fun setSensorAccuracy(sensorAccuracy: SensorAccuracy) = Unit
    override fun getTrueNorthFlow() = MutableStateFlow(trueNorth)
    override fun setTrueNorth(trueNorth: Boolean) = Unit
    override fun getHapticFeedbackFlow() = MutableStateFlow(hapticFeedback)
    override fun setHapticFeedback(hapticFeedback: Boolean) = Unit
    override fun getScreenOrientationLocked() = MutableStateFlow(screenOrientationLocked)
    override fun setScreenOrientationLocked(screenOrientationLocked: Boolean) = Unit
    override fun getLocationFlow() = MutableStateFlow(location)
    override fun setLocation(location: Location?) = Unit
    override fun getLocationStatusFlow() = MutableStateFlow(locationStatus)
    override fun setLocationStatus(locationStatus: LocationStatus) = Unit
}
