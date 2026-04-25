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

package com.bobek.compass.settings

import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import com.bobek.compass.data.AppNightMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreSettingsRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())
    private val testScope = TestScope(testDispatcher)

    private val repository by lazy {
        DataStoreSettingsRepository(
            PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
                scope = testScope,
                produceFile = { tempFolder.newFile("test_settings.preferences_pb") })
        )
    }

    // trueNorth

    @Test
    fun trueNorthDefaultIsTrue() = testScope.runTest {
        assertTrue(repository.getTrueNorth().first())
    }

    @Test
    fun trueNorthRoundTrip() = testScope.runTest {
        repository.setTrueNorth(false)
        assertFalse(repository.getTrueNorth().first())

        repository.setTrueNorth(true)
        assertTrue(repository.getTrueNorth().first())
    }

    // hapticFeedback

    @Test
    fun hapticFeedbackDefaultIsTrue() = testScope.runTest {
        assertTrue(repository.getHapticFeedback().first())
    }

    @Test
    fun hapticFeedbackRoundTrip() = testScope.runTest {
        repository.setHapticFeedback(false)
        assertFalse(repository.getHapticFeedback().first())

        repository.setHapticFeedback(true)
        assertTrue(repository.getHapticFeedback().first())
    }

    // screenOrientationLocked

    @Test
    fun screenOrientationLockedDefaultIsTrue() = testScope.runTest {
        assertTrue(repository.getScreenOrientationLocked().first())
    }

    @Test
    fun screenOrientationLockedRoundTrip() = testScope.runTest {
        repository.setScreenOrientationLocked(false)
        assertFalse(repository.getScreenOrientationLocked().first())

        repository.setScreenOrientationLocked(true)
        assertTrue(repository.getScreenOrientationLocked().first())
    }

    // nightMode

    @Test
    fun nightModeDefaultIsFollowSystem() = testScope.runTest {
        assertEquals(AppNightMode.FOLLOW_SYSTEM, repository.getNightMode().first())
    }

    @Test
    fun nightModeRoundTripFollowSystem() = testScope.runTest {
        repository.setNightMode(AppNightMode.FOLLOW_SYSTEM)
        assertEquals(AppNightMode.FOLLOW_SYSTEM, repository.getNightMode().first())
    }

    @Test
    fun nightModeRoundTripNo() = testScope.runTest {
        repository.setNightMode(AppNightMode.NO)
        assertEquals(AppNightMode.NO, repository.getNightMode().first())
    }

    @Test
    fun nightModeRoundTripYes() = testScope.runTest {
        repository.setNightMode(AppNightMode.YES)
        assertEquals(AppNightMode.YES, repository.getNightMode().first())
    }

    @Test
    fun nightModeSupportsAllEntries() = testScope.runTest {
        for (entry in AppNightMode.entries) {
            repository.setNightMode(entry)
            assertEquals(entry, repository.getNightMode().first())
        }
    }

    // accessLocationPermissionRequested

    @Test
    fun accessLocationPermissionRequestedDefaultIsFalse() = testScope.runTest {
        assertFalse(repository.getAccessLocationPermissionRequested().first())
    }

    @Test
    fun accessLocationPermissionRequestedRoundTrip() = testScope.runTest {
        repository.setAccessLocationPermissionRequested(true)
        assertTrue(repository.getAccessLocationPermissionRequested().first())

        repository.setAccessLocationPermissionRequested(false)
        assertFalse(repository.getAccessLocationPermissionRequested().first())
    }
}
