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

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.bobek.compass.databinding.ActivityMainBinding
import com.bobek.compass.model.AppNightMode
import com.bobek.compass.preference.PreferenceStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private val accessCoarseLocationPermissionRequest = registerAccessCoarseLocationPermissionRequest()

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var preferenceStore: PreferenceStore

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)

        val navController = getNavController()
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        initPreferenceStore()
    }

    private fun initPreferenceStore() {
        preferenceStore = PreferenceStore(this, lifecycle)
        preferenceStore.nightMode.observe(this) { setNightMode(it) }
        preferenceStore.screenOrientationLocked.observe(this) { setScreenRotationMode(it) }
        preferenceStore.trueNorth.observe(this) { setupTrueNorthFunctionality(it) }
    }

    private fun setNightMode(appNightMode: AppNightMode) {
        Log.d(TAG, "Setting night mode to $appNightMode")
        setDefaultNightMode(appNightMode.systemValue)
    }

    private fun setScreenRotationMode(screenOrientationLocked: Boolean) {
        if (screenOrientationLocked) {
            setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED)
        } else {
            setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
        }
    }

    private fun setScreenOrientation(screenOrientation: Int) {
        Log.d(TAG, "Setting requested orientation to value $screenOrientation")
        requestedOrientation = screenOrientation
    }

    private fun setupTrueNorthFunctionality(trueNorth: Boolean?) {
        if (trueNorth == true) {
            handleAccessCoarseLocationPermission()
        }
    }

    private fun handleAccessCoarseLocationPermission() {
        if (neverRequestedAccessCoarseLocationPermission() && accessCoarseLocationPermissionDenied()) {
            startAccessCoarseLocationPermissionRequestWorkflow()
        }
    }

    private fun neverRequestedAccessCoarseLocationPermission() =
        preferenceStore.accessCoarseLocationPermissionRequested.value != true

    private fun accessCoarseLocationPermissionDenied() =
        ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_DENIED

    private fun startAccessCoarseLocationPermissionRequestWorkflow() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_COARSE_LOCATION)) {
            showRequestNotificationsPermissionRationale()
        } else {
            launchAccessCoarseLocationPermissionRequest()
        }
    }

    private fun showRequestNotificationsPermissionRationale() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.access_coarse_location_permission_rationale_title)
            .setMessage(R.string.access_coarse_location_permission_rationale_message)
            .setCancelable(false)
            .setNeutralButton(R.string.ok) { dialog, _ ->
                launchAccessCoarseLocationPermissionRequest()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.no_thanks) { dialog, _ ->
                Log.i(TAG, "Continuing without requesting ACCESS_COARSE_LOCATION permission")
                preferenceStore.accessCoarseLocationPermissionRequested.value = true
                dialog.dismiss()
            }
            .show()
    }

    private fun launchAccessCoarseLocationPermissionRequest() {
        Log.i(TAG, "Requesting ACCESS_COARSE_LOCATION permission")
        accessCoarseLocationPermissionRequest.launch(ACCESS_COARSE_LOCATION)
    }

    private fun registerAccessCoarseLocationPermissionRequest() =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                Log.i(TAG, "Permission ACCESS_COARSE_LOCATION granted")
            } else {
                Log.i(TAG, "Permission ACCESS_COARSE_LOCATION denied")
                preferenceStore.accessCoarseLocationPermissionRequested.value = true
            }
        }


    override fun onSupportNavigateUp(): Boolean {
        val navController = getNavController()
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun getNavController(): NavController {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        return navHostFragment.navController
    }
}
