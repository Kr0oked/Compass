package com.bobek.compass

import com.bobek.compass.CardinalDirection.*

object MathUtils {

    @JvmStatic
    fun normalizeAngle(angleInDegrees: Float): Float {
        return (angleInDegrees + 360.0f) % 360.0f
    }

    @JvmStatic
    fun determineCardinalDirection(azimuth: Float): CardinalDirection {
        val cardinalDirection: CardinalDirection

        if (azimuth >= 22.5f && azimuth < 67.5f) {
            cardinalDirection = NORTH_EAST
        } else if (azimuth >= 67.5f && azimuth < 112.5f) {
            cardinalDirection = EAST
        } else if (azimuth >= 112.5f && azimuth < 157.5f) {
            cardinalDirection = SOUTH_EAST
        } else if (azimuth >= 157.5f && azimuth < 202.5f) {
            cardinalDirection = SOUTH
        } else if (azimuth >= 202.5f && azimuth < 247.5f) {
            cardinalDirection = SOUTH_WEST
        } else if (azimuth >= 247.5f && azimuth < 292.5f) {
            cardinalDirection = WEST
        } else if (azimuth >= 292.5f && azimuth < 337.5f) {
            cardinalDirection = NORTH_WEST
        } else {
            cardinalDirection = NORTH
        }

        return cardinalDirection
    }
}
