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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import com.bobek.compass.R
import com.bobek.compass.data.Azimuth
import com.bobek.compass.util.MathUtils
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val HAPTIC_FEEDBACK_INTERVAL = 2.0f

@Composable
fun CompassRose(
    azimuth: Azimuth,
    hapticFeedbackEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var lastHapticFeedbackPoint by remember { mutableStateOf<Azimuth?>(null) }

    LaunchedEffect(azimuth) {
        if (hapticFeedbackEnabled) {
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

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.img_device_heading_indicator),
            contentDescription = stringResource(id = R.string.device_heading_indicator_image_description),
            modifier = Modifier.fillMaxSize()
        )

        val rotation = -azimuth.degrees
        Box(modifier = Modifier.fillMaxSize().rotate(rotation)) {
            Image(
                painter = painterResource(id = R.drawable.img_compass_rose),
                contentDescription = stringResource(id = R.string.compass_rose_image_description),
                modifier = Modifier.fillMaxSize()
            )

            CompassTexts(rotation = 0f) // Texts rotate with the parent Box
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

@Composable
private fun CompassTexts(rotation: Float) {
    // We need to position cardinal directions and degrees in a circle.
    // The parent Box is already rotated, so we just need to position them at fixed angles.

    CircularLayout(radiusRatio = 0.8f) {
        // Cardinal directions
        CardinalDirectionText(stringResource(R.string.cardinal_direction_north_abbreviation), 0f)
        CardinalDirectionText(stringResource(R.string.cardinal_direction_east_abbreviation), 90f)
        CardinalDirectionText(stringResource(R.string.cardinal_direction_south_abbreviation), 180f)
        CardinalDirectionText(stringResource(R.string.cardinal_direction_west_abbreviation), 270f)

        // Degree texts
        DegreeText("0", 0f)
        DegreeText("30", 30f)
        DegreeText("60", 60f)
        DegreeText("90", 90f)
        DegreeText("120", 120f)
        DegreeText("150", 150f)
        DegreeText("180", 180f)
        DegreeText("210", 210f)
        DegreeText("240", 240f)
        DegreeText("270", 270f)
        DegreeText("300", 300f)
        DegreeText("330", 330f)
    }
}

@Composable
private fun CardinalDirectionText(text: String, angle: Float) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.angle(angle)
    )
}

@Composable
private fun DegreeText(text: String, angle: Float) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 12.sp,
        modifier = Modifier.angle(angle)
    )
}

@Composable
private fun CircularLayout(
    radiusRatio: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val size = minOf(constraints.maxWidth, constraints.maxHeight)
        val radius = size / 2f * radiusRatio

        val placeables = measurables.map { it.measure(Constraints()) }

        layout(size, size) {
            placeables.forEachIndexed { index, placeable ->
                val angle = (measurables[index].parentData as? Float) ?: 0f
                val angleRad = (angle - 90f) * PI.toFloat() / 180f

                val x = size / 2f + radius * cos(angleRad) - placeable.width / 2f
                val y = size / 2f + radius * sin(angleRad) - placeable.height / 2f

                placeable.placeRelative(x.toInt(), y.toInt())
            }
        }
    }
}

private fun Modifier.angle(angle: Float): Modifier = this.then(AngleModifier(angle))

private class AngleModifier(val angle: Float) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any? = angle
}
