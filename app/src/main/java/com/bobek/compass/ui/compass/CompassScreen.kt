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

package com.bobek.compass.ui.compass

import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.bobek.compass.ComposeCompassViewModel
import com.bobek.compass.ICompassViewModel
import com.bobek.compass.R
import com.bobek.compass.data.LocationStatus

@Composable
@PreviewScreenSizes
@OptIn(ExperimentalMaterial3Api::class)
fun CompassScreen(
    viewModel: ICompassViewModel = ComposeCompassViewModel(),
    onSettingsClick: () -> Unit = {},
    onLocationReload: () -> Unit = {}
) {
    val trueNorth by viewModel.getTrueNorthFlow().collectAsState()
    val locationStatus by viewModel.getLocationStatusFlow().collectAsState()

    var showSensorStatusDialog by rememberSaveable { mutableStateOf(false) }

    KeepScreenOnEffect()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.compass)) },
                actions = {
                    ScreenOrientationLockedButton(viewModel = viewModel)
                    SensorStatusButton(viewModel = viewModel, onClick = { showSensorStatusDialog = true })
                    SettingsButton(onClick = onSettingsClick)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CompassRose(
                viewModel = viewModel,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (trueNorth) {
                LocationSection(
                    locationStatus = locationStatus,
                    onLocationReload = onLocationReload
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            DeclinationText(
                trueNorth = trueNorth,
                locationStatus = locationStatus
            )
        }
    }

    if (showSensorStatusDialog) {
        SensorStatusDialog(
            viewModel = viewModel,
            onDismiss = {
                @Suppress("AssignedValueIsNeverRead")
                showSensorStatusDialog = false
            }
        )
    }
}

@Composable
private fun KeepScreenOnEffect() {
    val activity = LocalActivity.current
    DisposableEffect(Unit) {
        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

@Composable
private fun ScreenOrientationLockedButton(viewModel: ICompassViewModel) {
    val screenOrientationLocked by viewModel.getScreenOrientationLocked().collectAsState()

    val painter = if (screenOrientationLocked) {
        painterResource(R.drawable.ic_mobile_rotate_lock)
    } else {
        painterResource(R.drawable.ic_mobile_rotate)
    }

    IconButton(
        onClick = {
            viewModel.setScreenOrientationLocked(!screenOrientationLocked)
        }
    ) {
        Icon(
            painter = painter,
            contentDescription = stringResource(R.string.lock_screen_rotation)
        )
    }
}

@Composable
private fun SensorStatusButton(
    viewModel: ICompassViewModel,
    onClick: () -> Unit
) {
    val sensorAccuracy by viewModel.getSensorAccuracyFlow().collectAsState()

    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(sensorAccuracy.iconResourceId),
            contentDescription = stringResource(R.string.sensor_status),
            tint = sensorAccuracy.tintColor
        )
    }
}

@Composable
private fun SettingsButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(R.drawable.ic_settings),
            contentDescription = stringResource(R.string.settings)
        )
    }
}

@Composable
private fun LocationSection(
    locationStatus: LocationStatus,
    onLocationReload: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        when (locationStatus) {
            LocationStatus.NOT_PRESENT -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(R.drawable.ic_warning),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = stringResource(R.string.location_not_present),
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = onLocationReload) {
                        Icon(painter = painterResource(R.drawable.ic_refresh), contentDescription = null)
                        Text(text = stringResource(R.string.location_reload))
                    }
                }
            }

            LocationStatus.LOADING -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text(text = stringResource(R.string.location_loading))
                }
            }

            LocationStatus.PERMISSION_DENIED -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(R.drawable.ic_warning),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = stringResource(R.string.access_location_permission_denied),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            LocationStatus.PRESENT -> {
                // Location is present, nothing specific to show here according to XML
            }
        }
    }
}

@Composable
private fun DeclinationText(
    trueNorth: Boolean,
    locationStatus: LocationStatus
) {
    val text = if (trueNorth && locationStatus == LocationStatus.PRESENT) {
        stringResource(R.string.true_north)
    } else {
        stringResource(R.string.magnetic_north)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.ic_explore),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
