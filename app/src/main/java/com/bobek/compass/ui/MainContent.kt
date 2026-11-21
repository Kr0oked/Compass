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

package com.bobek.compass.ui

import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bobek.compass.R
import com.bobek.compass.ui.compass.ComposeCompassViewModel
import com.bobek.compass.ui.compass.ICompassViewModel
import com.bobek.compass.data.AppNightMode
import com.bobek.compass.ui.compass.CompassScreen
import com.bobek.compass.licenses.LicenseRepository
import com.bobek.compass.ui.licenses.LicenseScreen
import com.bobek.compass.ui.licenses.LicenseScreenState
import com.bobek.compass.ui.licenses.ThirdPartyLicensesScreen
import com.bobek.compass.ui.settings.SettingsScreen
import com.bobek.compass.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
@PreviewScreenSizes
fun MainContent(
    appViewModel: IAppViewModel = ComposeAppViewModel(),
    compassViewModel: ICompassViewModel = ComposeCompassViewModel(),
    onLocationReload: () -> Unit = {}
) {
    val navController = rememberNavController()
    val nightMode by appViewModel.getNightModeFlow().collectAsState()
    val screenOrientationLocked by compassViewModel.getScreenOrientationLocked().collectAsState()

    val isDarkTheme = when (nightMode) {
        AppNightMode.NO -> false
        AppNightMode.YES -> true
        AppNightMode.FOLLOW_SYSTEM -> isSystemInDarkTheme()
    }

    LocalActivity.current?.requestedOrientation = if (screenOrientationLocked) {
        ActivityInfo.SCREEN_ORIENTATION_LOCKED
    } else {
        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    AppTheme(darkTheme = isDarkTheme) {
        // Read here because the transition lambdas below aren't composable
        val motionScheme = MaterialTheme.motionScheme
        val slideSpec = motionScheme.defaultSpatialSpec<IntOffset>()
        val fadeSpec = motionScheme.defaultEffectsSpec<Float>()
        val slideOffset = { fullOffset: Int -> fullOffset / 4 }

        NavHost(
            navController = navController,
            startDestination = "compass",
            enterTransition = { slideIntoContainer(SlideDirection.Start, slideSpec, slideOffset) + fadeIn(fadeSpec) },
            exitTransition = { slideOutOfContainer(SlideDirection.Start, slideSpec, slideOffset) + fadeOut(fadeSpec) },
            popEnterTransition = { slideIntoContainer(SlideDirection.End, slideSpec, slideOffset) + fadeIn(fadeSpec) },
            popExitTransition = { slideOutOfContainer(SlideDirection.End, slideSpec, slideOffset) + fadeOut(fadeSpec) }
        ) {
            composable("compass") {
                CompassScreen(
                    viewModel = compassViewModel,
                    onSettingsClick = { navController.navigate("settings") },
                    onLocationReload = onLocationReload
                )
            }
            composable("settings") {
                SettingsScreen(
                    appViewModel = appViewModel,
                    compassViewModel = compassViewModel,
                    onBackClick = { navController.popBackStack() },
                    onLicenseClick = { navController.navigate("license") },
                    onThirdPartyLicensesClick = { navController.navigate("licenses") }
                )
            }
            composable("license") {
                val resources = LocalResources.current
                val licenseContent by produceState(initialValue = "") {
                    value = withContext(Dispatchers.IO) {
                        LicenseRepository.getAppLicenseContent(resources)
                    }
                }

                LicenseScreen(
                    state = LicenseScreenState(
                        title = stringResource(R.string.license_name),
                        licenseContent = licenseContent,
                    ),
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("licenses") {
                val resources = LocalResources.current
                val libraryNames by produceState(initialValue = emptyList()) {
                    value = withContext(Dispatchers.IO) {
                        LicenseRepository.getThirdPartyLibraryNames(resources)
                    }
                }

                ThirdPartyLicensesScreen(
                    libraryNames = libraryNames,
                    onBackClick = { navController.popBackStack() },
                    onLibraryClick = { libraryName ->
                        navController.navigate("license/${Uri.encode(libraryName)}")
                    }
                )
            }
            composable("license/{libraryName}") { backStackEntry ->
                val libraryName = Uri.decode(backStackEntry.arguments?.getString("libraryName") ?: "")
                val resources = LocalResources.current
                val licenseContent by produceState(initialValue = "", key1 = libraryName) {
                    value = withContext(Dispatchers.IO) {
                        LicenseRepository.getThirdPartyLicenseContent(resources, libraryName)
                    }
                }

                LicenseScreen(
                    state = LicenseScreenState(
                        title = libraryName,
                        licenseContent = licenseContent,
                    ),
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
