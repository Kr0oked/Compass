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

package com.bobek.compass

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobek.compass.data.AppNightMode
import com.bobek.compass.data.Azimuth
import com.bobek.compass.data.LocationStatus
import com.bobek.compass.data.SensorAccuracy
import com.bobek.compass.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

private const val SETTINGS_DEBOUNCE_MILLIS = 1_000L

interface ICompassViewModel {
    fun getAzimuthFlow(): StateFlow<Azimuth>
    fun setAzimuth(azimuth: Azimuth)
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
    fun getNightModeFlow(): StateFlow<AppNightMode>
    fun setNightMode(nightMode: AppNightMode)
}

@HiltViewModel
@OptIn(FlowPreview::class)
class CompassViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel(), ICompassViewModel {

    private val azimuthFlow = MutableStateFlow(Azimuth(0.0f))

    private val sensorAccuracyFlow = MutableStateFlow(SensorAccuracy.NO_CONTACT)

    private val trueNorthFlow = MutableStateFlow(false)

    private val hapticFeedbackFlow = MutableStateFlow(true)

    private val screenOrientationLockedFlow = MutableStateFlow(false)

    private val locationFlow = MutableStateFlow<Location?>(null)

    private val locationStatusFlow = MutableStateFlow(LocationStatus.NOT_PRESENT)

    private val nightModeFlow = MutableStateFlow(AppNightMode.FOLLOW_SYSTEM)

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
        settingsRepository.getNightMode().firstOrNull()?.let { nightModeFlow.value = it }
    }

    private fun setupFlowsToSettings() {
        viewModelScope.launch {
            trueNorthFlow.drop(1).debounce(SETTINGS_DEBOUNCE_MILLIS)
                .collect { settingsRepository.setTrueNorth(it) }
        }
        viewModelScope.launch {
            hapticFeedbackFlow.drop(1).debounce(SETTINGS_DEBOUNCE_MILLIS)
                .collect { settingsRepository.setHapticFeedback(it) }
        }
        viewModelScope.launch {
            screenOrientationLockedFlow.drop(1).debounce(SETTINGS_DEBOUNCE_MILLIS)
                .collect { settingsRepository.setScreenOrientationLocked(it) }
        }
        viewModelScope.launch {
            nightModeFlow.drop(1).debounce(SETTINGS_DEBOUNCE_MILLIS)
                .collect { settingsRepository.setNightMode(it) }
        }
    }

    override fun getAzimuthFlow() = azimuthFlow

    override fun setAzimuth(azimuth: Azimuth) {
        azimuthFlow.value = azimuth
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

    override fun getNightModeFlow() = nightModeFlow

    override fun setNightMode(nightMode: AppNightMode) {
        nightModeFlow.value = nightMode
    }
}

class ComposeCompassViewModel(
    val azimuth: Azimuth = Azimuth(0.0f),
    val sensorAccuracy: SensorAccuracy = SensorAccuracy.NO_CONTACT,
    val trueNorth: Boolean = false,
    val hapticFeedback: Boolean = true,
    val screenOrientationLocked: Boolean = true,
    val location: Location? = Location(""),
    val locationStatus: LocationStatus = LocationStatus.NOT_PRESENT,
    val nightMode: AppNightMode = AppNightMode.FOLLOW_SYSTEM
) : ICompassViewModel {
    override fun getAzimuthFlow() = MutableStateFlow(azimuth)
    override fun setAzimuth(azimuth: Azimuth) = Unit
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
    override fun getNightModeFlow() = MutableStateFlow(nightMode)
    override fun setNightMode(nightMode: AppNightMode) = Unit
}
