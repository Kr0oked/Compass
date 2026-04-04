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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
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

    HapticFeedbackEffect(viewModel)

    val textMeasurer = rememberTextMeasurer()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val errorColor = MaterialTheme.colorScheme.error
    val primaryColor = MaterialTheme.colorScheme.primary

    // Strings must be collected in composable context
    val northAbbr = stringResource(R.string.cardinal_direction_north_abbreviation)
    val eastAbbr = stringResource(R.string.cardinal_direction_east_abbreviation)
    val southAbbr = stringResource(R.string.cardinal_direction_south_abbreviation)
    val westAbbr = stringResource(R.string.cardinal_direction_west_abbreviation)
    val azimuthText = stringResource(id = R.string.degrees, azimuth.roundedDegrees)
    val cardinalDirectionText = stringResource(id = azimuth.cardinalDirection.labelResourceId)

    // Capture typeface/weight from MaterialTheme; fontSize is overridden inside Canvas
    val azimuthTypography = MaterialTheme.typography.displaySmall
    val cardinalDirectionTypography = MaterialTheme.typography.titleLarge

    Box(modifier = modifier) {
        val description = stringResource(R.string.compass_rose_image_description)

        // todo: refactor
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = description
                }
        ) {
            val canvasSize = minOf(size.width, size.height)
            val center = Offset(size.width / 2f, size.height / 2f)

            // All sizes proportional to canvasSize
            val outerRadius = canvasSize * 0.34f
            val labelRadius = canvasSize * 0.44f
            val cardinalRadius = outerRadius * 0.82f

            val highlightedTickStroke = canvasSize * 0.010f
            val smallTickStroke = canvasSize * 0.003f

            val textGap = canvasSize * 0.012f

            val cardinalStyle = TextStyle(fontSize = (canvasSize * 0.055f).toSp(), fontWeight = FontWeight.Bold)
            val degreeStyle = TextStyle(fontSize = (canvasSize * 0.028f).toSp())
            val azimuthStyle = azimuthTypography.copy(fontSize = (canvasSize * 0.115f).toSp(), color = onSurfaceColor)
            val cardinalDirectionStyle =
                cardinalDirectionTypography.copy(fontSize = (canvasSize * 0.062f).toSp(), color = onSurfaceColor)

            // Fixed heading indicator: upward-pointing triangle above the strong ticks
            val triangleTipY = center.y - canvasSize * 0.48f
            val triangleBaseY = center.y - outerRadius * 1.14f - canvasSize * 0.02f
            val triangleHalfWidth = canvasSize * 0.03f

            val indicatorPath = Path().apply {
                moveTo(center.x, triangleTipY)
                lineTo(center.x - triangleHalfWidth, triangleBaseY)
                lineTo(center.x + triangleHalfWidth, triangleBaseY)
                close()
            }
            drawPath(indicatorPath, color = primaryColor)
            drawLine(
                color = primaryColor,
                start = Offset(center.x, triangleBaseY),
                end = Offset(center.x, center.y - outerRadius),
                strokeWidth = canvasSize * 0.017f,
                cap = StrokeCap.Square
            )

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
                        strokeWidth = highlightedTickStroke
                    } else {
                        tickLength = outerRadius * 0.08f
                        strokeWidth = smallTickStroke
                    }

                    val tickColor = when {
                        degree == 0 -> errorColor
                        degree % 30 == 0 -> onSurfaceColor
                        else -> onSurfaceColor.copy(alpha = 0.9f)
                    }
                    val innerX = center.x + outerRadius * sinA
                    val innerY = center.y - outerRadius * cosA
                    val outerX = center.x + (outerRadius + tickLength) * sinA
                    val outerY = center.y - (outerRadius + tickLength) * cosA

                    drawLine(
                        color = tickColor,
                        start = Offset(innerX, innerY),
                        end = Offset(outerX, outerY),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Square
                    )
                }

                // Degree numbers outside at every 30°
                for (degree in 0 until 360 step 30) {
                    val labelColor = if (degree == 0) errorColor else onSurfaceColor
                    val measured = textMeasurer.measure(degree.toString(), style = degreeStyle.copy(color = labelColor))

                    val angleRad = degree * PI.toFloat() / 180f
                    val tx = center.x + labelRadius * sin(angleRad)
                    val ty = center.y - labelRadius * cos(angleRad)

                    withTransform({
                        translate(tx, ty)
                        rotate(azimuth.degrees, pivot = Offset.Zero)
                    }) {
                        drawText(measured, topLeft = Offset(-measured.size.width / 2f, -measured.size.height / 2f))
                    }
                }

                // Cardinal abbreviations inside at 0/90/180/270
                val cardinals = listOf(0 to northAbbr, 90 to eastAbbr, 180 to southAbbr, 270 to westAbbr)
                for ((degree, abbr) in cardinals) {
                    val labelColor = if (degree == 0) errorColor else onSurfaceColor
                    val measured = textMeasurer.measure(abbr, style = cardinalStyle.copy(color = labelColor))

                    val angleRad = degree * PI.toFloat() / 180f
                    val tx = center.x + cardinalRadius * sin(angleRad)
                    val ty = center.y - cardinalRadius * cos(angleRad)

                    withTransform({
                        translate(tx, ty)
                        rotate(azimuth.degrees, pivot = Offset.Zero)
                    }) {
                        drawText(measured, topLeft = Offset(-measured.size.width / 2f, -measured.size.height / 2f))
                    }
                }
            }

            // Azimuth and cardinal direction texts centered on the canvas
            val measuredAzimuth = textMeasurer.measure(azimuthText, style = azimuthStyle)
            val measuredCardinalDirection = textMeasurer.measure(cardinalDirectionText, style = cardinalDirectionStyle)
            val totalHeight = measuredAzimuth.size.height + textGap + measuredCardinalDirection.size.height
            val topY = center.y - totalHeight / 2f

            drawText(
                measuredAzimuth,
                topLeft = Offset(center.x - measuredAzimuth.size.width / 2f, topY)
            )
            drawText(
                measuredCardinalDirection,
                topLeft = Offset(
                    center.x - measuredCardinalDirection.size.width / 2f,
                    topY + measuredAzimuth.size.height + textGap
                )
            )
        }
    }
}

@Composable
private fun HapticFeedbackEffect(viewModel: ICompassViewModel) {
    val azimuth by viewModel.getAzimuthFlow().collectAsState()
    val hapticFeedback by viewModel.getHapticFeedbackFlow().collectAsState()

    val view = LocalView.current
    var lastHapticFeedbackPoint by remember { mutableStateOf<Azimuth?>(null) }

    LaunchedEffect(azimuth) {
        if (hapticFeedback) {
            val lastPoint = lastHapticFeedbackPoint
            if (lastPoint == null) {
                val closestIntervalPoint =
                    MathUtils.getClosestNumberFromInterval(azimuth.degrees, HAPTIC_FEEDBACK_INTERVAL)
                lastHapticFeedbackPoint = Azimuth(closestIntervalPoint)
            } else {
                val boundaryStart = lastPoint - HAPTIC_FEEDBACK_INTERVAL
                val boundaryEnd = lastPoint + HAPTIC_FEEDBACK_INTERVAL

                if (!MathUtils.isAzimuthBetweenTwoPoints(azimuth, boundaryStart, boundaryEnd)) {
                    val closestIntervalPoint =
                        MathUtils.getClosestNumberFromInterval(azimuth.degrees, HAPTIC_FEEDBACK_INTERVAL)
                    lastHapticFeedbackPoint = Azimuth(closestIntervalPoint)
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
            }
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
