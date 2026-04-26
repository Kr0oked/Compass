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
import com.bobek.compass.data.AppNightMode
import com.bobek.compass.data.Azimuth
import com.bobek.compass.data.LocationStatus
import com.bobek.compass.data.SensorAccuracy
import com.bobek.compass.settings.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val SETTINGS_DEBOUNCE_MILLIS = 1_000L

@OptIn(ExperimentalCoroutinesApi::class)
class CompassViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())

    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var viewModel: CompassViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeSettingsRepository = FakeSettingsRepository()
        viewModel = CompassViewModel(fakeSettingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Initial state

    @Test
    fun initialAzimuth() {
        assertEquals(Azimuth(0.0f), viewModel.getAzimuthFlow().value)
    }

    @Test
    fun initialSensorAccuracy() {
        assertEquals(SensorAccuracy.NO_CONTACT, viewModel.getSensorAccuracyFlow().value)
    }

    @Test
    fun initialLocation() {
        assertNull(viewModel.getLocationFlow().value)
    }

    @Test
    fun initialLocationStatus() {
        assertEquals(LocationStatus.NOT_PRESENT, viewModel.getLocationStatusFlow().value)
    }

    // Settings loaded from repository on init

    @Test
    fun trueNorthLoadedFromSettings() {
        assertTrue(viewModel.getTrueNorthFlow().value)
    }

    @Test
    fun hapticFeedbackLoadedFromSettings() {
        assertTrue(viewModel.getHapticFeedbackFlow().value)
    }

    @Test
    fun screenOrientationLockedLoadedFromSettings() {
        assertTrue(viewModel.getScreenOrientationLocked().value)
    }

    @Test
    fun nightModeLoadedFromSettings() {
        assertEquals(AppNightMode.FOLLOW_SYSTEM, viewModel.getNightModeFlow().value)
    }

    // Setters update flows immediately

    @Test
    fun setAzimuthUpdatesFlow() {
        viewModel.setAzimuth(Azimuth(90.0f))
        assertEquals(Azimuth(90.0f), viewModel.getAzimuthFlow().value)
    }

    @Test
    fun setSensorAccuracyUpdatesFlow() {
        viewModel.setSensorAccuracy(SensorAccuracy.HIGH)
        assertEquals(SensorAccuracy.HIGH, viewModel.getSensorAccuracyFlow().value)
    }

    @Test
    fun setTrueNorthUpdatesFlow() {
        viewModel.setTrueNorth(false)
        assertFalse(viewModel.getTrueNorthFlow().value)
    }

    @Test
    fun setHapticFeedbackUpdatesFlow() {
        viewModel.setHapticFeedback(false)
        assertFalse(viewModel.getHapticFeedbackFlow().value)
    }

    @Test
    fun setScreenOrientationLockedUpdatesFlow() {
        viewModel.setScreenOrientationLocked(false)
        assertFalse(viewModel.getScreenOrientationLocked().value)
    }

    @Test
    fun setNightModeUpdatesFlow() {
        viewModel.setNightMode(AppNightMode.YES)
        assertEquals(AppNightMode.YES, viewModel.getNightModeFlow().value)
    }

    @Test
    fun setLocationUpdatesFlow() {
        val location = Location("test")
        viewModel.setLocation(location)
        assertSame(location, viewModel.getLocationFlow().value)
    }

    @Test
    fun setLocationStatusUpdatesFlow() {
        viewModel.setLocationStatus(LocationStatus.PRESENT)
        assertEquals(LocationStatus.PRESENT, viewModel.getLocationStatusFlow().value)
    }

    // Settings persistence after debounce

    @Test
    fun trueNorthIsPersistedToSettingsAfterDebounce() = runTest(testDispatcher) {
        viewModel.setTrueNorth(false)
        advanceTimeBy(SETTINGS_DEBOUNCE_MILLIS + 1)
        assertFalse(fakeSettingsRepository.trueNorthValue)
    }

    @Test
    fun hapticFeedbackIsPersistedToSettingsAfterDebounce() = runTest(testDispatcher) {
        viewModel.setHapticFeedback(false)
        advanceTimeBy(SETTINGS_DEBOUNCE_MILLIS + 1)
        assertFalse(fakeSettingsRepository.hapticFeedbackValue)
    }

    @Test
    fun screenOrientationLockedIsPersistedToSettingsAfterDebounce() = runTest(testDispatcher) {
        viewModel.setScreenOrientationLocked(false)
        advanceTimeBy(SETTINGS_DEBOUNCE_MILLIS + 1)
        assertFalse(fakeSettingsRepository.screenOrientationLockedValue)
    }

    @Test
    fun nightModeIsPersistedToSettingsAfterDebounce() = runTest(testDispatcher) {
        viewModel.setNightMode(AppNightMode.NO)
        advanceTimeBy(SETTINGS_DEBOUNCE_MILLIS + 1)
        assertEquals(AppNightMode.NO, fakeSettingsRepository.nightModeValue)
    }

    @Test
    fun rapidChangesOnlyPersistLastValueAfterDebounce() = runTest(testDispatcher) {
        viewModel.setTrueNorth(false)
        advanceTimeBy(SETTINGS_DEBOUNCE_MILLIS - 1)
        // Debounce not expired yet — repository not yet updated
        assertTrue(fakeSettingsRepository.trueNorthValue)

        viewModel.setTrueNorth(true)
        advanceTimeBy(SETTINGS_DEBOUNCE_MILLIS + 1)
        // Only the last value is persisted
        assertTrue(fakeSettingsRepository.trueNorthValue)
    }
}

private class FakeSettingsRepository : SettingsRepository {

    private val trueNorthFlow = MutableStateFlow(true)
    private val hapticFeedbackFlow = MutableStateFlow(true)
    private val screenOrientationLockedFlow = MutableStateFlow(true)
    private val nightModeFlow = MutableStateFlow(AppNightMode.FOLLOW_SYSTEM)
    private val accessLocationPermissionRequestedFlow = MutableStateFlow(false)

    val trueNorthValue get() = trueNorthFlow.value
    val hapticFeedbackValue get() = hapticFeedbackFlow.value
    val screenOrientationLockedValue get() = screenOrientationLockedFlow.value
    val nightModeValue get() = nightModeFlow.value

    override fun getTrueNorth(): Flow<Boolean> = trueNorthFlow
    override suspend fun setTrueNorth(trueNorth: Boolean) {
        trueNorthFlow.value = trueNorth
    }

    override fun getHapticFeedback(): Flow<Boolean> = hapticFeedbackFlow
    override suspend fun setHapticFeedback(hapticFeedback: Boolean) {
        hapticFeedbackFlow.value = hapticFeedback
    }

    override fun getScreenOrientationLocked(): Flow<Boolean> = screenOrientationLockedFlow
    override suspend fun setScreenOrientationLocked(screenOrientationLocked: Boolean) {
        screenOrientationLockedFlow.value = screenOrientationLocked
    }

    override fun getNightMode(): Flow<AppNightMode> = nightModeFlow
    override suspend fun setNightMode(nightMode: AppNightMode) {
        nightModeFlow.value = nightMode
    }

    override fun getAccessLocationPermissionRequested(): Flow<Boolean> = accessLocationPermissionRequestedFlow
    override suspend fun setAccessLocationPermissionRequested(accessLocationPermissionRequested: Boolean) {
        accessLocationPermissionRequestedFlow.value = accessLocationPermissionRequested
    }
}
