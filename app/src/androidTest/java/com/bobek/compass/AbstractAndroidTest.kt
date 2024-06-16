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

import android.content.Intent
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bobek.compass.model.Azimuth
import com.bobek.compass.model.SensorAccuracy
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
abstract class AbstractAndroidTest {

    protected val intent: Intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)

    @get:Rule
    var activityRule = ActivityScenarioRule<MainActivity>(intent)

    protected fun setAzimuth(degrees: Float) {
        activityRule.scenario.onActivity { mainActivity ->
            findCompassFragment(mainActivity)!!.setAzimuth(Azimuth(degrees))
        }
    }

    protected fun setAccuracy(accuracy: SensorAccuracy) {
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
