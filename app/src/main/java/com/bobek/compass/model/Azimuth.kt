/*
 * This file is part of Compass.
 * Copyright (C) 2023 Philipp Bobek <philipp.bobek@mailbox.org>
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

package com.bobek.compass.model

import kotlin.math.roundToInt

class Azimuth(_degrees: Float) {

    init {
        if (!_degrees.isFinite()) {
            throw IllegalArgumentException("Degrees must be finite but was '$_degrees'")
        }
    }

    val degrees = normalizeAngle(_degrees)

    val roundedDegrees = normalizeAngle(_degrees.roundToInt().toFloat()).toInt()

    val cardinalDirection: CardinalDirection = when (degrees) {
        in 22.5f until 67.5f -> CardinalDirection.NORTHEAST
        in 67.5f until 112.5f -> CardinalDirection.EAST
        in 112.5f until 157.5f -> CardinalDirection.SOUTHEAST
        in 157.5f until 202.5f -> CardinalDirection.SOUTH
        in 202.5f until 247.5f -> CardinalDirection.SOUTHWEST
        in 247.5f until 292.5f -> CardinalDirection.WEST
        in 292.5f until 337.5f -> CardinalDirection.NORTHWEST
        else -> CardinalDirection.NORTH
    }

    private fun normalizeAngle(angleInDegrees: Float): Float {
        return (angleInDegrees + 360f) % 360f
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Azimuth

        if (degrees != other.degrees) return false

        return true
    }

    override fun hashCode(): Int {
        return degrees.hashCode()
    }

    override fun toString(): String {
        return "Azimuth(degrees=$degrees)"
    }

    operator fun plus(degrees: Float) = Azimuth(this.degrees + degrees)

    operator fun minus(degrees: Float) = Azimuth(this.degrees - degrees)

    operator fun compareTo(azimuth: Azimuth) = this.degrees.compareTo(azimuth.degrees)
}

private data class SemiClosedFloatRange(val fromInclusive: Float, val toExclusive: Float)

private operator fun SemiClosedFloatRange.contains(value: Float) = fromInclusive <= value && value < toExclusive
private infix fun Float.until(to: Float) = SemiClosedFloatRange(this, to)
