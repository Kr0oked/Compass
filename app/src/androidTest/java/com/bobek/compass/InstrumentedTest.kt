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

import android.content.Intent
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
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

    private fun setAzimuth(degrees: Float) {
        activityRule.scenario.onActivity { mainActivity ->
            findCompassFragment(mainActivity)!!.setAzimuth(Azimuth(degrees))
        }
    }

    private fun setAccuracy(accuracy: SensorAccuracy) {
        activityRule.scenario.onActivity { mainActivity ->
            findCompassFragment(mainActivity)!!.setSensorAccuracy(accuracy)
        }
    }

    private fun findCompassFragment(mainActivity: MainActivity): CompassFragment? {
        return findNavHostFragment(mainActivity)
            ?.childFragmentManager
            ?.primaryNavigationFragment as CompassFragment?
    }

    private fun findNavHostFragment(mainActivity: MainActivity) =
        mainActivity
            .supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment?
}
