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

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.bobek.compass.R
import com.bobek.compass.data.Azimuth
import com.bobek.compass.data.CompassReading
import com.bobek.compass.data.CompassRegime
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private const val REGIME_CROSSFADE_MILLIS = 250
private const val HINT_ROSE_ALPHA = 0.28f

/**
 * Picks the compass visualization that suits the current device orientation and crossfades between
 * them: the rotating rose when the phone is roughly flat, the sighting strip when it is roughly
 * upright, and a "hold level" hint when it is roughly face-down.
 */
@Composable
fun CompassDisplay(
    viewModel: ICompassViewModel,
    modifier: Modifier = Modifier
) {
    val reading by viewModel.getCompassReadingFlow().collectAsState()
    val hapticFeedbackEnabled by viewModel.getHapticFeedbackFlow().collectAsState()

    // Keep the last rose heading so the hint can freeze it instead of showing the meaningless spin.
    var frozenRoseAzimuth by remember { mutableStateOf(reading.azimuth) }
    if (reading.regime != CompassRegime.HINT) {
        frozenRoseAzimuth = reading.azimuth
    }

    // Suppress haptics briefly after a regime change: the active bearing basis jumps.
    var regimeSettling by remember { mutableStateOf(false) }
    LaunchedEffect(reading.regime) {
        regimeSettling = true
        delay(REGIME_CROSSFADE_MILLIS.milliseconds)
        regimeSettling = false
    }

    val hapticBearing = when (reading.regime) {
        CompassRegime.ROSE -> reading.azimuth
        CompassRegime.SIGHTING -> reading.sightingBearing
        CompassRegime.HINT -> reading.azimuth
    }
    val hapticsActive = hapticFeedbackEnabled &&
            reading.reliable &&
            !regimeSettling &&
            reading.regime != CompassRegime.HINT
    CompassHapticFeedback(
        enabled = hapticsActive,
        bearing = hapticBearing
    )

    Crossfade(
        targetState = reading.regime,
        animationSpec = tween(REGIME_CROSSFADE_MILLIS),
        label = "compass-regime",
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite }
    ) { regime ->
        when (regime) {
            CompassRegime.ROSE -> CompassRose(
                azimuth = reading.azimuth,
                modifier = Modifier.fillMaxSize()
            )

            CompassRegime.SIGHTING -> CompassStrip(
                bearing = reading.sightingBearing,
                modifier = Modifier.fillMaxSize()
            )

            CompassRegime.HINT -> HoldLevelHint(
                frozenAzimuth = frozenRoseAzimuth,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun HoldLevelHint(
    frozenAzimuth: Azimuth,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CompassRose(
            azimuth = frozenAzimuth,
            modifier = Modifier
                .fillMaxSize()
                .alpha(HINT_ROSE_ALPHA)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(R.drawable.ic_explore),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.compass_hold_level),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
@Preview(widthDp = 360, heightDp = 420)
private fun CompassDisplayPreview(
    @PreviewParameter(CompassDisplayReadingProvider::class) reading: CompassReading
) {
    CompassDisplay(viewModel = ComposeCompassViewModel(compassReading = reading))
}

private class CompassDisplayReadingProvider : PreviewParameterProvider<CompassReading> {
    override val values: Sequence<CompassReading> = sequenceOf(
        CompassReadingShowcase.ROSE,
        CompassReadingShowcase.SIGHTING,
        CompassReadingShowcase.HINT
    )

    override fun getDisplayName(index: Int): String? =
        listOf("Rose", "Sighting", "Hint").getOrNull(index)
}
