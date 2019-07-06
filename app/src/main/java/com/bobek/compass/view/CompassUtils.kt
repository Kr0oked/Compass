/*
 * This file is part of Compass.
 * Copyright (C) 2019 Philipp Bobek <philipp.bobek@mailbox.org>
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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.bobek.compass.view

object CompassUtils {

    @JvmStatic
    fun determineCardinalDirection(degrees: Float): CardinalDirection {
        val cardinalDirection: CardinalDirection

        if (degrees >= 22.5f && degrees < 67.5f) {
            cardinalDirection = CardinalDirection.NORTH_EAST
        } else if (degrees >= 67.5f && degrees < 112.5f) {
            cardinalDirection = CardinalDirection.EAST
        } else if (degrees >= 112.5f && degrees < 157.5f) {
            cardinalDirection = CardinalDirection.SOUTH_EAST
        } else if (degrees >= 157.5f && degrees < 202.5f) {
            cardinalDirection = CardinalDirection.SOUTH
        } else if (degrees >= 202.5f && degrees < 247.5f) {
            cardinalDirection = CardinalDirection.SOUTH_WEST
        } else if (degrees >= 247.5f && degrees < 292.5f) {
            cardinalDirection = CardinalDirection.WEST
        } else if (degrees >= 292.5f && degrees < 337.5f) {
            cardinalDirection = CardinalDirection.NORTH_WEST
        } else {
            cardinalDirection = CardinalDirection.NORTH
        }

        return cardinalDirection
    }
}
