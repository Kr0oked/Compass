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

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.performClick
import androidx.test.filters.LargeTest
import com.bobek.compass.data.SensorAccuracy
import org.junit.Before
import org.junit.Test

@LargeTest
class InstrumentedTest : AbstractAndroidTest() {

    @Before
    fun setup() {
        waitUntilCompassIsDisplayed()
    }

    @Test
    fun compass() {
        setAzimuth(0f)
        onCompassRose().assertStateDescription("0°")

        setAzimuth(180f)
        onCompassRose().assertStateDescription("180°")
    }

    @Test
    fun sensorStatusDialog() {
        onSensorStatusButton().performClick()

        setAccuracy(SensorAccuracy.NO_CONTACT)
        assertSensorAccuracyText(R.string.sensor_accuracy_no_contact)

        setAccuracy(SensorAccuracy.UNRELIABLE)
        assertSensorAccuracyText(R.string.sensor_accuracy_unreliable)

        setAccuracy(SensorAccuracy.LOW)
        assertSensorAccuracyText(R.string.sensor_accuracy_low)

        setAccuracy(SensorAccuracy.MEDIUM)
        assertSensorAccuracyText(R.string.sensor_accuracy_medium)

        setAccuracy(SensorAccuracy.HIGH)
        assertSensorAccuracyText(R.string.sensor_accuracy_high)
    }

    private fun assertSensorAccuracyText(@StringRes resourceId: Int) {
        val expectedText = composeTestRule.activity.getString(resourceId)
        onSensorAccuracyText().assertTextEquals(expectedText)
    }
}
