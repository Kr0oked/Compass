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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bobek.compass.R
import com.bobek.compass.ui.TestConstants
import kotlin.math.roundToInt

/**
 * Live magnetic field strength for the compass screen. Collects the ~5 Hz strength flow itself so
 * only this readout recomposes on each magnetometer event, and renders nothing until the first one.
 */
@Composable
fun MagneticFieldStrengthReadout(viewModel: ICompassViewModel) {
    val magneticFieldStrength by viewModel.getMagneticFieldStrengthFlow().collectAsState()
    val microtesla = magneticFieldStrength ?: return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .semantics(mergeDescendants = true) {}
            .testTag(TestConstants.MAGNETIC_FIELD_STRENGTH)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_sensors),
            contentDescription = stringResource(R.string.magnetic_field_strength),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = formatMagneticFieldStrength(microtesla))
    }
}

@Composable
fun formatMagneticFieldStrength(microtesla: Float): String =
    stringResource(R.string.magnetic_field_strength_value, microtesla.roundToInt())
