/*
 * This file is part of Compass.
 * Copyright (C) 2024 Philipp Bobek <philipp.bobek@mailbox.org>
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

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import com.bobek.compass.model.SensorAccuracy
import org.hamcrest.Matchers.not
import org.junit.Test

@LargeTest
class InstrumentedTest : AbstractAndroidTest() {

    init {
        intent.putExtra(OPTION_INSTRUMENTED_TEST, true)
    }

    @Test
    fun compass() {
        onView(withId(R.id.status_degrees_text)).check(matches(not(isDisplayed())))

        setAzimuth(0f)
        Thread.sleep(100)
        onView(withId(R.id.status_degrees_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("0°")))

        setAzimuth(180f)
        Thread.sleep(100)
        onView(withId(R.id.status_degrees_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("180°")))
    }

    @Test
    fun sensorStatusDialog() {
        onView(withId(R.id.action_sensor_status))
            .check(matches(isDisplayed()))
            .perform(click())

        setAccuracy(SensorAccuracy.NO_CONTACT)
        onView(withId(R.id.sensor_accuracy_text))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.sensor_accuracy_no_contact)))

        setAccuracy(SensorAccuracy.UNRELIABLE)
        onView(withId(R.id.sensor_accuracy_text))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.sensor_accuracy_unreliable)))

        setAccuracy(SensorAccuracy.LOW)
        onView(withId(R.id.sensor_accuracy_text))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.sensor_accuracy_low)))

        setAccuracy(SensorAccuracy.MEDIUM)
        onView(withId(R.id.sensor_accuracy_text))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.sensor_accuracy_medium)))

        setAccuracy(SensorAccuracy.HIGH)
        onView(withId(R.id.sensor_accuracy_text))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.sensor_accuracy_high)))

        onView(withText(R.string.ok)).perform(click())
    }
}
