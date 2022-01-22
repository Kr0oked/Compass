/*
 * This file is part of Compass.
 * Copyright (C) 2022 Philipp Bobek <philipp.bobek@mailbox.org>
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

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.bobek.compass.model.Azimuth
import com.bobek.compass.model.SensorAccuracy
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class InstrumentedTest {

    private val intent: Intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

    init {
        intent.putExtra(OPTION_INSTRUMENTED_TEST, true)
    }

    @get:Rule
    var activityRule = ActivityScenarioRule<MainActivity>(intent)

    @Test
    fun compass() {
        onView(withId(R.id.status_degrees_text)).check(matches(not(isDisplayed())))

        setAzimuth(0f)
        onView(withId(R.id.status_degrees_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("0°")))

        setAzimuth(180f)
        onView(withId(R.id.status_degrees_text))
            .check(matches(isDisplayed()))
            .check(matches(withText("180°")))
    }

    @Test
    fun sensorStatusDialog() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
        onView(withText(R.string.sensor_status))
            .check(matches(isDisplayed()))
            .perform(click())

        setAccuracy(SensorAccuracy.NO_CONTACT)
        onView(withId(R.id.sensor_accuracy_text)).check(matches(withText(R.string.sensor_accuracy_no_contact)))

        setAccuracy(SensorAccuracy.UNRELIABLE)
        onView(withId(R.id.sensor_accuracy_text)).check(matches(withText(R.string.sensor_accuracy_unreliable)))

        setAccuracy(SensorAccuracy.LOW)
        onView(withId(R.id.sensor_accuracy_text)).check(matches(withText(R.string.sensor_accuracy_low)))

        setAccuracy(SensorAccuracy.MEDIUM)
        onView(withId(R.id.sensor_accuracy_text)).check(matches(withText(R.string.sensor_accuracy_medium)))

        setAccuracy(SensorAccuracy.HIGH)
        onView(withId(R.id.sensor_accuracy_text)).check(matches(withText(R.string.sensor_accuracy_high)))

        onView(withText(R.string.ok)).perform(click())
    }

    @Test
    fun aboutDialog() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().targetContext)
        onView(withText(R.string.about))
            .check(matches(isDisplayed()))
            .perform(click())

        onView(withId(R.id.version)).check(matches(isDisplayed()))
        onView(withId(R.id.copyright)).check(matches(withText(R.string.copyright)))
        onView(withId(R.id.license)).check(matches(withText(R.string.license)))
        onView(withId(R.id.source_code))
            .check(matches(withText(R.string.source_code)))
            .check(matches(hasLinks()))

        onView(withText(R.string.ok)).perform(click())
    }

    private fun setAzimuth(degrees: Float) {
        activityRule.scenario.onActivity { mainActivity -> mainActivity.setAzimuth(Azimuth(degrees)) }
    }

    private fun setAccuracy(accuracy: SensorAccuracy) {
        activityRule.scenario.onActivity { mainActivity -> mainActivity.setAccuracy(accuracy) }
    }
}
