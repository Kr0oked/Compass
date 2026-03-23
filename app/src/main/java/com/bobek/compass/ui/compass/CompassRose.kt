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

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobek.compass.ComposeCompassViewModel
import com.bobek.compass.ICompassViewModel
import com.bobek.compass.R
import com.bobek.compass.data.Azimuth
import com.bobek.compass.util.MathUtils
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val HAPTIC_FEEDBACK_INTERVAL = 2.0f

@Composable
@Preview(widthDp = 512)
fun CompassRose(
    @PreviewParameter(CompassRoseViewModelProvider::class) viewModel: ICompassViewModel,
    modifier: Modifier = Modifier
) {
    val azimuth by viewModel.getAzimuthFlow().collectAsState()
    val hapticFeedback by viewModel.getHapticFeedbackFlow().collectAsState()

    val view = LocalView.current
    var lastHapticFeedbackPoint by remember { mutableStateOf<Azimuth?>(null) }

    LaunchedEffect(azimuth) {
        if (hapticFeedback) {
            val lastPoint = lastHapticFeedbackPoint
            if (lastPoint == null) {
                val closestIntervalPoint = MathUtils.getClosestNumberFromInterval(azimuth.degrees, HAPTIC_FEEDBACK_INTERVAL)
                lastHapticFeedbackPoint = Azimuth(closestIntervalPoint)
            } else {
                val boundaryStart = lastPoint - HAPTIC_FEEDBACK_INTERVAL
                val boundaryEnd = lastPoint + HAPTIC_FEEDBACK_INTERVAL

                if (!MathUtils.isAzimuthBetweenTwoPoints(azimuth, boundaryStart, boundaryEnd)) {
                    val closestIntervalPoint = MathUtils.getClosestNumberFromInterval(azimuth.degrees, HAPTIC_FEEDBACK_INTERVAL)
                    lastHapticFeedbackPoint = Azimuth(closestIntervalPoint)
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
            }
        }
    }

    val textMeasurer = rememberTextMeasurer()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val errorColor = MaterialTheme.colorScheme.error

    val northAbbr = stringResource(R.string.cardinal_direction_north_abbreviation)
    val eastAbbr = stringResource(R.string.cardinal_direction_east_abbreviation)
    val southAbbr = stringResource(R.string.cardinal_direction_south_abbreviation)
    val westAbbr = stringResource(R.string.cardinal_direction_west_abbreviation)

    val cardinalStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
    val degreeStyle = TextStyle(fontSize = 11.sp)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = minOf(size.width, size.height)
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = canvasSize / 2f * 0.68f
            val labelRadius = canvasSize / 2f * 0.88f

            // Rotating compass rose
            withTransform({ rotate(-azimuth.degrees, pivot = center) }) {

                // Tick marks
                for (degree in 0 until 360 step 2) {
                    val angleRad = degree * PI.toFloat() / 180f
                    val sinA = sin(angleRad)
                    val cosA = cos(angleRad)

                    val tickLength: Float
                    val strokeWidth: Float
                    if (degree % 30 == 0) {
                        tickLength = outerRadius * 0.14f
                        strokeWidth = 2.dp.toPx()
                    } else {
                        tickLength = outerRadius * 0.05f
                        strokeWidth = 1.dp.toPx()
                    }

                    val tickColor = if (degree == 0) errorColor else onSurfaceColor
                    val innerX = center.x + outerRadius * sinA
                    val innerY = center.y - outerRadius * cosA
                    val outerX = center.x + (outerRadius + tickLength) * sinA
                    val outerY = center.y - (outerRadius + tickLength) * cosA

                    drawLine(
                        color = tickColor,
                        start = Offset(innerX, innerY),
                        end = Offset(outerX, outerY),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }

                // Labels every 30°: cardinal abbreviations for 0/90/180/270, degree numbers for the rest
                for (degree in 0 until 360 step 30) {
                    val label = when (degree) {
                        0 -> northAbbr
                        90 -> eastAbbr
                        180 -> southAbbr
                        270 -> westAbbr
                        else -> degree.toString()
                    }
                    val isCardinal = degree % 90 == 0
                    val labelColor = if (degree == 0) errorColor else onSurfaceColor
                    val style = (if (isCardinal) cardinalStyle else degreeStyle).copy(color = labelColor)
                    val measured = textMeasurer.measure(label, style = style)

                    val angleRad = degree * PI.toFloat() / 180f
                    val tx = center.x + labelRadius * sin(angleRad)
                    val ty = center.y - labelRadius * cos(angleRad)

                    // Translate to the label position, counter-rotate to keep text upright on screen
                    withTransform({
                        translate(tx, ty)
                        rotate(azimuth.degrees, pivot = Offset.Zero)
                    }) {
                        drawText(
                            measured,
                            topLeft = Offset(-measured.size.width / 2f, -measured.size.height / 2f)
                        )
                    }
                }
            }

            // Fixed heading indicator: downward-pointing triangle at the top of the circle
            val indicatorHeight = outerRadius * 0.10f
            val indicatorHalfWidth = outerRadius * 0.055f
            val indicatorPath = Path().apply {
                moveTo(center.x, center.y - outerRadius + indicatorHeight)
                lineTo(center.x - indicatorHalfWidth, center.y - outerRadius - indicatorHeight * 0.3f)
                lineTo(center.x + indicatorHalfWidth, center.y - outerRadius - indicatorHeight * 0.3f)
                close()
            }
            drawPath(indicatorPath, color = errorColor)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.degrees, azimuth.roundedDegrees),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(id = azimuth.cardinalDirection.labelResourceId),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private class CompassRoseViewModelProvider : PreviewParameterProvider<ICompassViewModel> {
    override val values: Sequence<ICompassViewModel> = sequenceOf(
        ComposeCompassViewModel(azimuth = Azimuth(0.0f)),
        ComposeCompassViewModel(azimuth = Azimuth(95.0f)),
        ComposeCompassViewModel(azimuth = Azimuth(185.5f)),
        ComposeCompassViewModel(azimuth = Azimuth(268.1f)),
    )

    override fun getDisplayName(index: Int): String? =
        when (index) {
            0 -> "0.0f"
            1 -> "95.0f"
            2 -> "185.5f"
            3 -> "268.1f"
            else -> null
        }
}
