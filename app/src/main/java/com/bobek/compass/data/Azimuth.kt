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

package com.bobek.compass.data

import kotlin.math.roundToInt

class Azimuth(rawDegrees: Float) {

    init {
        if (!rawDegrees.isFinite()) {
            throw IllegalArgumentException("Degrees must be finite but was '$rawDegrees'")
        }
    }

    val degrees = normalizeAngle(rawDegrees)

    val roundedDegrees = normalizeAngle(rawDegrees.roundToInt().toFloat()).toInt()

    val cardinalDirection: CardinalDirection = when (degrees) {
        in 11.25f until 33.75f -> CardinalDirection.NORTH_NORTHEAST
        in 33.75f until 56.25f -> CardinalDirection.NORTHEAST
        in 56.25f until 78.75f -> CardinalDirection.EAST_NORTHEAST
        in 78.75f until 101.25f -> CardinalDirection.EAST
        in 101.25f until 123.75f -> CardinalDirection.EAST_SOUTHEAST
        in 123.75f until 146.25f -> CardinalDirection.SOUTHEAST
        in 146.25f until 168.75f -> CardinalDirection.SOUTH_SOUTHEAST
        in 168.75f until 191.25f -> CardinalDirection.SOUTH
        in 191.25f until 213.75f -> CardinalDirection.SOUTH_SOUTHWEST
        in 213.75f until 236.25f -> CardinalDirection.SOUTHWEST
        in 236.25f until 258.75f -> CardinalDirection.WEST_SOUTHWEST
        in 258.75f until 281.25f -> CardinalDirection.WEST
        in 281.25f until 303.75f -> CardinalDirection.WEST_NORTHWEST
        in 303.75f until 326.25f -> CardinalDirection.NORTHWEST
        in 326.25f until 348.75f -> CardinalDirection.NORTH_NORTHWEST
        else -> CardinalDirection.NORTH
    }

    private fun normalizeAngle(angleInDegrees: Float): Float {
        return (angleInDegrees + 360f) % 360f
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Azimuth

        return degrees == other.degrees
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

private operator fun SemiClosedFloatRange.contains(value: Float) = value in fromInclusive..<toExclusive

private infix fun Float.until(to: Float) = SemiClosedFloatRange(this, to)
