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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.bobek.compass.R
import com.bobek.compass.data.Azimuth
import com.bobek.compass.ui.TestConstants
import kotlin.math.abs

/** Degrees of heading visible across the full width of the strip. */
private const val VISIBLE_SPAN_DEGREES = 90f

/** The sighting compass: a horizontal ruler that scrolls under a fixed reticle showing where the
 * phone's back points. Shown when the phone is held roughly upright. */
@Composable
@Preview(widthDp = 360, heightDp = 320)
fun CompassStrip(
    @PreviewParameter(CompassStripBearingProvider::class) bearing: Azimuth,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    val cardinalDirectionText = stringResource(bearing.cardinalDirection.labelResourceId)
    val bearingText = stringResource(R.string.degrees, bearing.roundedDegrees)
    val description = stringResource(R.string.compass_strip_image_description)
    val stateDescription =
        stringResource(R.string.compass_strip_state_description, cardinalDirectionText, bearingText)

    val degreeStyle = MaterialTheme.typography.labelMedium.copy(color = onSurfaceColor)
    val cardinalStyle = MaterialTheme.typography.titleMedium.copy(
        color = onSurfaceColor,
        fontWeight = FontWeight.Bold
    )

    // Degrees always increase clockwise; the ruler must not mirror in right-to-left locales.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = modifier.semantics {
                contentDescription = description
                this.stateDescription = stateDescription
            },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .testTag(TestConstants.COMPASS_STRIP)
            ) {
                val data = CompassStripDrawData(
                    textMeasurer = textMeasurer,
                    bearingDegrees = bearing.degrees,
                    onSurfaceColor = onSurfaceColor,
                    primaryColor = primaryColor,
                    errorColor = errorColor,
                    degreeStyle = degreeStyle,
                    cardinalStyle = cardinalStyle
                )
                drawRuler(data)
                drawReticle(data)
            }

            Text(
                text = bearingText,
                style = MaterialTheme.typography.displaySmall,
                color = onSurfaceColor,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = cardinalDirectionText,
                style = MaterialTheme.typography.titleLarge,
                color = onSurfaceColor
            )
        }
    }
}

private fun DrawScope.drawRuler(data: CompassStripDrawData) {
    val centerX = size.width / 2f
    val baselineY = size.height * 0.72f
    val pixelsPerDegree = size.width / VISIBLE_SPAN_DEGREES
    val halfSpan = VISIBLE_SPAN_DEGREES / 2f + 4f

    for (degree in 0 until 360 step 2) {
        val delta = signedDelta(degree - data.bearingDegrees)
        if (abs(delta) > halfSpan) continue

        val x = centerX + delta * pixelsPerDegree
        val major = degree % 30 == 0
        val tickLength = if (major) size.height * 0.22f else size.height * 0.11f
        val color = when {
            degree == 0 -> data.errorColor
            major -> data.onSurfaceColor
            else -> data.onSurfaceColor.copy(alpha = 0.6f)
        }

        drawLine(
            color = color,
            start = Offset(x, baselineY),
            end = Offset(x, baselineY - tickLength),
            strokeWidth = if (major) size.height * 0.02f else size.height * 0.008f,
            cap = StrokeCap.Round
        )

        if (major) {
            val cardinal = cardinalLabel(degree)
            val style = when {
                degree == 0 -> data.cardinalStyle.copy(color = data.errorColor)
                cardinal != null -> data.cardinalStyle
                else -> data.degreeStyle
            }
            val measured = data.textMeasurer.measure(cardinal ?: degree.toString(), style)
            drawText(
                measured,
                topLeft = Offset(x - measured.size.width / 2f, baselineY + size.height * 0.06f)
            )
        }
    }

    drawLine(
        color = data.onSurfaceColor.copy(alpha = 0.4f),
        start = Offset(0f, baselineY),
        end = Offset(size.width, baselineY),
        strokeWidth = size.height * 0.006f
    )
}

private fun DrawScope.drawReticle(data: CompassStripDrawData) {
    val centerX = size.width / 2f
    val baselineY = size.height * 0.72f
    // Apex above the base, matching the rose's heading indicator: the point reads as an
    // upward-facing arrowhead, with the line continuing from the base down to the ruler.
    val apexY = size.height * 0.22f
    val baseY = size.height * 0.32f
    val halfWidth = size.height * 0.06f

    val path = Path().apply {
        moveTo(centerX, apexY)
        lineTo(centerX - halfWidth, baseY)
        lineTo(centerX + halfWidth, baseY)
        close()
    }
    drawPath(path, color = data.primaryColor)
    drawLine(
        color = data.primaryColor,
        start = Offset(centerX, baseY),
        end = Offset(centerX, baselineY),
        strokeWidth = size.height * 0.02f,
        cap = StrokeCap.Round
    )
}

private fun signedDelta(rawDegrees: Float): Float {
    var delta = rawDegrees % 360f
    if (delta > 180f) delta -= 360f
    if (delta < -180f) delta += 360f
    return delta
}

private fun cardinalLabel(degree: Int): String? = when (degree) {
    0 -> "N"
    90 -> "E"
    180 -> "S"
    270 -> "W"
    else -> null
}

private class CompassStripDrawData(
    val textMeasurer: TextMeasurer,
    val bearingDegrees: Float,
    val onSurfaceColor: Color,
    val primaryColor: Color,
    val errorColor: Color,
    val degreeStyle: TextStyle,
    val cardinalStyle: TextStyle
)

private class CompassStripBearingProvider : PreviewParameterProvider<Azimuth> {
    override val values: Sequence<Azimuth> = sequenceOf(
        Azimuth(0.0f),
        Azimuth(47.5f),
        Azimuth(213.0f)
    )

    override fun getDisplayName(index: Int): String? =
        listOf("0°", "47.5°", "213°").getOrNull(index)
}
