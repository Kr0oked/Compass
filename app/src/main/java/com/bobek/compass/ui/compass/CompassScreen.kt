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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.bobek.compass.R
import com.bobek.compass.data.Azimuth
import com.bobek.compass.data.CompassReading
import com.bobek.compass.data.CompassRegime
import com.bobek.compass.data.LocationStatus
import com.bobek.compass.ui.TestConstants

@Composable
@PreviewScreenSizes
@OptIn(ExperimentalMaterial3Api::class)
fun CompassScreen(
    @PreviewParameter(CompassScreenViewModelProvider::class) viewModel: ICompassViewModel,
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
        CompassContent(
            viewModel = viewModel,
            trueNorth = trueNorth,
            locationStatus = locationStatus,
            padding = padding,
            onLocationReload = onLocationReload
        )
    }

    if (showSensorStatusDialog) {
        SensorStatusDialog(
            viewModel = viewModel,
            onDismiss = {
                showSensorStatusDialog = false
            }
        )
    }
}

/**
 * One layout for every orientation and regime: [CompassDisplay] fills the area and stays centered,
 * so it never resizes or shifts as the phone tilts, while the info texts float over its lower
 * corners where the round rose and the short strip leave space. Nothing branches on the regime, so
 * the texts stay put while the display crossfades.
 */
@Composable
private fun CompassContent(
    viewModel: ICompassViewModel,
    trueNorth: Boolean,
    locationStatus: LocationStatus,
    padding: PaddingValues,
    onLocationReload: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .consumeWindowInsets(padding)
            .padding(dimensionResource(R.dimen.root_layout_padding))
    ) {
        CompassDisplay(
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize()
        )

        CornerInfo(corner = Alignment.BottomStart, horizontalAlignment = Alignment.Start) {
            DeclinationText(trueNorth = trueNorth, locationStatus = locationStatus)
        }

        if (trueNorth) {
            CornerInfo(corner = Alignment.BottomEnd, horizontalAlignment = Alignment.End) {
                LocationSection(locationStatus = locationStatus, onLocationReload = onLocationReload)
            }
        }
    }
}

/**
 * A half-width info column pinned to one [corner] of the compass. Scrolls internally instead of
 * clipping when its content is too tall, for example a location error with its reload button on a
 * short screen, or large accessibility font scaling.
 */
@Composable
private fun BoxScope.CornerInfo(
    corner: Alignment,
    horizontalAlignment: Alignment.Horizontal,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .align(corner)
            .fillMaxWidth(0.5f)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = horizontalAlignment,
        content = content
    )
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
@OptIn(ExperimentalMaterial3Api::class)
private fun ScreenOrientationLockedButton(viewModel: ICompassViewModel) {
    val screenOrientationLocked by viewModel.getScreenOrientationLocked().collectAsState()

    val iconResourceId = if (screenOrientationLocked) R.drawable.ic_mobile_rotate_lock else R.drawable.ic_mobile_rotate
    val label = stringResource(R.string.lock_screen_rotation)

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
        tooltip = { PlainTooltip { Text(label) } },
        state = rememberTooltipState()
    ) {
        IconButton(onClick = { viewModel.setScreenOrientationLocked(!screenOrientationLocked) }) {
            Icon(painter = painterResource(iconResourceId), contentDescription = label)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SensorStatusButton(
    viewModel: ICompassViewModel,
    onClick: () -> Unit
) {
    val sensorAccuracy by viewModel.getSensorAccuracyFlow().collectAsState()
    val label = stringResource(R.string.sensor_status)

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
        tooltip = { PlainTooltip { Text(label) } },
        state = rememberTooltipState()
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.testTag(TestConstants.SENSOR_STATUS_BUTTON)
        ) {
            Icon(
                painter = painterResource(sensorAccuracy.iconResourceId),
                contentDescription = label,
                tint = sensorAccuracy.tintColor
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SettingsButton(onClick: () -> Unit) {
    val label = stringResource(R.string.settings)

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
        tooltip = { PlainTooltip { Text(label) } },
        state = rememberTooltipState()
    ) {
        IconButton(onClick = onClick) {
            Icon(painter = painterResource(R.drawable.ic_settings), contentDescription = label)
        }
    }
}

@Composable
private fun LocationSection(
    locationStatus: LocationStatus,
    onLocationReload: () -> Unit
) {
    when (locationStatus) {
        LocationStatus.NOT_PRESENT -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LocationError(stringResource(R.string.location_not_present))
                Button(onClick = onLocationReload) {
                    Icon(painter = painterResource(R.drawable.ic_refresh), contentDescription = null)
                    Text(text = stringResource(R.string.location_reload))
                }
            }
        }

        LocationStatus.LOADING -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                Text(text = stringResource(R.string.location_loading), textAlign = TextAlign.Center)
            }
        }

        LocationStatus.PERMISSION_DENIED -> {
            LocationError(stringResource(R.string.access_location_permission_denied))
        }

        LocationStatus.PRESENT -> {}
    }
}

@Composable
private fun LocationError(message: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.ic_warning),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
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

private class CompassScreenViewModelProvider : PreviewParameterProvider<ICompassViewModel> {
    override val values: Sequence<ICompassViewModel> = sequenceOf(
        ComposeCompassViewModel(trueNorth = false),
        ComposeCompassViewModel(trueNorth = true),
        ComposeCompassViewModel(
            compassReading = CompassReading.INITIAL.copy(
                sightingBearing = Azimuth(123.0f),
                tilt = 90f,
                regime = CompassRegime.SIGHTING,
                reliable = true
            ),
            trueNorth = true,
            locationStatus = LocationStatus.PRESENT
        )
    )

    override fun getDisplayName(index: Int): String? =
        when (index) {
            0 -> "Magnetic North"
            1 -> "True North"
            2 -> "Sighting"
            else -> null
        }
}
