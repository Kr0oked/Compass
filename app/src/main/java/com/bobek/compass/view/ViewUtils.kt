/*
 * This file is part of Compass.
 * Copyright (C) 2021 Philipp Bobek <philipp.bobek@mailbox.org>
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

package com.bobek.compass.view

object ViewUtils {

    @JvmStatic
    fun determineCardinalDirection(azimuth: Float): CardinalDirection {
        val cardinalDirection: CardinalDirection

        if (azimuth >= 22.5f && azimuth < 67.5f) {
            cardinalDirection = CardinalDirection.NORTHEAST
        } else if (azimuth >= 67.5f && azimuth < 112.5f) {
            cardinalDirection = CardinalDirection.EAST
        } else if (azimuth >= 112.5f && azimuth < 157.5f) {
            cardinalDirection = CardinalDirection.SOUTHEAST
        } else if (azimuth >= 157.5f && azimuth < 202.5f) {
            cardinalDirection = CardinalDirection.SOUTH
        } else if (azimuth >= 202.5f && azimuth < 247.5f) {
            cardinalDirection = CardinalDirection.SOUTHWEST
        } else if (azimuth >= 247.5f && azimuth < 292.5f) {
            cardinalDirection = CardinalDirection.WEST
        } else if (azimuth >= 292.5f && azimuth < 337.5f) {
            cardinalDirection = CardinalDirection.NORTHWEST
        } else {
            cardinalDirection = CardinalDirection.NORTH
        }

        return cardinalDirection
    }
}
