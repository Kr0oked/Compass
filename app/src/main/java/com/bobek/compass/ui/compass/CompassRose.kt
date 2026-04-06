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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
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

@Composable
@Preview(widthDp = 512)
fun CompassRose(
    @PreviewParameter(CompassRoseViewModelProvider::class) viewModel: ICompassViewModel,
    modifier: Modifier = Modifier
) {
    val azimuth by viewModel.getAzimuthFlow().collectAsState()

    HapticFeedbackEffect(viewModel)

    val drawData = rememberCompassDrawData(azimuth)

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    contentDescription = drawData.description
                }
        ) {
            val metrics = CompassMetrics(
                canvasSize = minOf(size.width, size.height),
                center = Offset(size.width / 2f, size.height / 2f)
            )
            val styles = CompassStyles(
                cardinalStyle = TextStyle(
                    fontSize = (metrics.canvasSize * 0.055f).toSp(),
                    fontWeight = FontWeight.Bold
                ),
                degreeStyle = TextStyle(fontSize = (metrics.canvasSize * 0.028f).toSp()),
                azimuthStyle = drawData.azimuthTypography.copy(
                    fontSize = (metrics.canvasSize * 0.115f).toSp(),
                    color = drawData.onSurfaceColor
                ),
                cardinalDirectionStyle = drawData.cardinalDirectionTypography.copy(
                    fontSize = (metrics.canvasSize * 0.062f).toSp(),
                    color = drawData.onSurfaceColor
                )
            )

            drawFixedOverlay(metrics, styles, drawData)
            drawRotatingRose(metrics, styles, drawData)
        }
    }
}

private const val HAPTIC_FEEDBACK_INTERVAL = 2.0f

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

@Composable
private fun rememberCompassDrawData(azimuth: Azimuth): CompassDrawData {
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val azimuthTypography = MaterialTheme.typography.displaySmall
    val cardinalDirectionTypography = MaterialTheme.typography.titleLarge
    val northAbbreviation = stringResource(R.string.cardinal_direction_north_abbreviation)
    val eastAbbreviation = stringResource(R.string.cardinal_direction_east_abbreviation)
    val southAbbreviation = stringResource(R.string.cardinal_direction_south_abbreviation)
    val westAbbreviation = stringResource(R.string.cardinal_direction_west_abbreviation)
    val azimuthText = stringResource(R.string.degrees, azimuth.roundedDegrees)
    val cardinalDirectionText = stringResource(azimuth.cardinalDirection.labelResourceId)
    val description = stringResource(R.string.compass_rose_image_description)

    return CompassDrawData(
        textMeasurer = textMeasurer,
        primaryColor = primaryColor,
        errorColor = errorColor,
        onSurfaceColor = onSurfaceColor,
        azimuthDegrees = azimuth.degrees,
        azimuthTypography = azimuthTypography,
        cardinalDirectionTypography = cardinalDirectionTypography,
        northAbbreviation = northAbbreviation,
        eastAbbreviation = eastAbbreviation,
        southAbbreviation = southAbbreviation,
        westAbbreviation = westAbbreviation,
        azimuthText = azimuthText,
        cardinalDirectionText = cardinalDirectionText,
        description = description
    )
}

private fun DrawScope.drawFixedOverlay(
    metrics: CompassMetrics,
    styles: CompassStyles,
    drawData: CompassDrawData
) {
    drawHeadingIndicator(metrics, drawData)
    drawCenterText(metrics, styles, drawData)
}

private fun DrawScope.drawHeadingIndicator(metrics: CompassMetrics, drawData: CompassDrawData) {
    val triangleTipY = metrics.center.y - metrics.canvasSize * 0.49f
    val triangleBaseY = metrics.center.y - metrics.outerRadius * 1.14f - metrics.canvasSize * 0.02f
    val triangleHalfWidth = metrics.canvasSize * 0.03f

    val indicatorPath = Path().apply {
        moveTo(metrics.center.x, triangleTipY)
        lineTo(metrics.center.x - triangleHalfWidth, triangleBaseY)
        lineTo(metrics.center.x + triangleHalfWidth, triangleBaseY)
        close()
    }
    drawPath(path = indicatorPath, color = drawData.primaryColor)
    drawLine(
        color = drawData.primaryColor,
        start = Offset(metrics.center.x, triangleBaseY),
        end = Offset(metrics.center.x, metrics.center.y - metrics.outerRadius),
        strokeWidth = metrics.canvasSize * 0.017f,
        cap = StrokeCap.Square
    )
}

private fun DrawScope.drawCenterText(
    metrics: CompassMetrics,
    styles: CompassStyles,
    drawData: CompassDrawData
) {
    val measuredAzimuth = drawData.textMeasurer.measure(text = drawData.azimuthText, style = styles.azimuthStyle)
    val measuredCardinalDirection =
        drawData.textMeasurer.measure(text = drawData.cardinalDirectionText, style = styles.cardinalDirectionStyle)
    val totalHeight = measuredAzimuth.size.height + metrics.textGap + measuredCardinalDirection.size.height
    val topY = metrics.center.y - totalHeight / 2f

    drawText(
        measuredAzimuth,
        topLeft = Offset(
            metrics.center.x - measuredAzimuth.size.width / 2f,
            topY
        )
    )
    drawText(
        measuredCardinalDirection,
        topLeft = Offset(
            metrics.center.x - measuredCardinalDirection.size.width / 2f,
            topY + measuredAzimuth.size.height + metrics.textGap
        )
    )
}

private fun DrawScope.drawRotatingRose(
    metrics: CompassMetrics,
    styles: CompassStyles,
    drawData: CompassDrawData
) {
    withTransform({ rotate(-drawData.azimuthDegrees, pivot = metrics.center) }) {
        drawTickMarks(metrics, drawData)
        drawDegreeLabels(metrics, styles, drawData)
        drawCardinalAbbreviations(metrics, styles, drawData)
    }
}

private fun DrawScope.drawTickMarks(metrics: CompassMetrics, drawData: CompassDrawData) {
    for (degree in 0 until 360 step 2) {
        val angleRadians = degree * PI.toFloat() / 180f
        val sinAngle = sin(angleRadians)
        val cosAngle = cos(angleRadians)

        val tickLength: Float
        val strokeWidth: Float
        if (degree % 30 == 0) {
            tickLength = metrics.outerRadius * 0.14f
            strokeWidth = metrics.highlightedTickStroke
        } else {
            tickLength = metrics.outerRadius * 0.08f
            strokeWidth = metrics.smallTickStroke
        }

        val tickColor = when {
            degree == 0 -> drawData.errorColor
            degree % 30 == 0 -> drawData.onSurfaceColor
            else -> drawData.onSurfaceColor.copy(alpha = 0.9f)
        }
        val innerX = metrics.center.x + metrics.outerRadius * sinAngle
        val innerY = metrics.center.y - metrics.outerRadius * cosAngle
        val outerX = metrics.center.x + (metrics.outerRadius + tickLength) * sinAngle
        val outerY = metrics.center.y - (metrics.outerRadius + tickLength) * cosAngle

        drawLine(
            color = tickColor,
            start = Offset(innerX, innerY),
            end = Offset(outerX, outerY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Square
        )
    }
}

private fun DrawScope.drawDegreeLabels(
    metrics: CompassMetrics,
    styles: CompassStyles,
    drawData: CompassDrawData
) {
    for (degree in 0 until 360 step 30) {
        val labelColor = if (degree == 0) drawData.errorColor else drawData.onSurfaceColor
        val measured = drawData.textMeasurer.measure(
            text = degree.toString(),
            style = styles.degreeStyle.copy(color = labelColor)
        )

        val angleRadians = degree * PI.toFloat() / 180f
        val tx = metrics.center.x + metrics.labelRadius * sin(angleRadians)
        val ty = metrics.center.y - metrics.labelRadius * cos(angleRadians)

        withTransform({
            translate(tx, ty)
            rotate(degrees = drawData.azimuthDegrees, pivot = Offset.Zero)
        }) {
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(-measured.size.width / 2f, -measured.size.height / 2f)
            )
        }
    }
}

private fun DrawScope.drawCardinalAbbreviations(
    metrics: CompassMetrics,
    styles: CompassStyles,
    drawData: CompassDrawData
) {
    val cardinals = listOf(
        0 to drawData.northAbbreviation,
        90 to drawData.eastAbbreviation,
        180 to drawData.southAbbreviation,
        270 to drawData.westAbbreviation
    )

    for ((degree, abbreviation) in cardinals) {
        val labelColor = if (degree == 0) drawData.errorColor else drawData.onSurfaceColor
        val measured = drawData.textMeasurer.measure(
            text = abbreviation,
            style = styles.cardinalStyle.copy(color = labelColor)
        )

        val angleRadians = degree * PI.toFloat() / 180f
        val tx = metrics.center.x + metrics.cardinalRadius * sin(angleRadians)
        val ty = metrics.center.y - metrics.cardinalRadius * cos(angleRadians)

        withTransform({
            translate(tx, ty)
            rotate(drawData.azimuthDegrees, pivot = Offset.Zero)
        }) {
            drawText(measured, topLeft = Offset(-measured.size.width / 2f, -measured.size.height / 2f))
        }
    }
}

private class CompassDrawData(
    val textMeasurer: TextMeasurer,
    val primaryColor: Color,
    val errorColor: Color,
    val onSurfaceColor: Color,
    val azimuthDegrees: Float,
    val azimuthTypography: TextStyle,
    val cardinalDirectionTypography: TextStyle,
    val northAbbreviation: String,
    val eastAbbreviation: String,
    val southAbbreviation: String,
    val westAbbreviation: String,
    val azimuthText: String,
    val cardinalDirectionText: String,
    val description: String
)

private data class CompassMetrics(val canvasSize: Float, val center: Offset) {
    val outerRadius = canvasSize * 0.36f
    val labelRadius = canvasSize * 0.46f
    val cardinalRadius = outerRadius * 0.82f
    val highlightedTickStroke = canvasSize * 0.010f
    val smallTickStroke = canvasSize * 0.003f
    val textGap = canvasSize * 0.012f
}

private data class CompassStyles(
    val cardinalStyle: TextStyle,
    val degreeStyle: TextStyle,
    val azimuthStyle: TextStyle,
    val cardinalDirectionStyle: TextStyle
)

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