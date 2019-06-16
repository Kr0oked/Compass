package com.bobek.compass.view

object CompassUtils {

    @JvmStatic
    fun determineCardinalDirection(azimuth: Float): CardinalDirection {
        val cardinalDirection: CardinalDirection

        if (azimuth >= 22.5f && azimuth < 67.5f) {
            cardinalDirection = CardinalDirection.NORTH_EAST
        } else if (azimuth >= 67.5f && azimuth < 112.5f) {
            cardinalDirection = CardinalDirection.EAST
        } else if (azimuth >= 112.5f && azimuth < 157.5f) {
            cardinalDirection = CardinalDirection.SOUTH_EAST
        } else if (azimuth >= 157.5f && azimuth < 202.5f) {
            cardinalDirection = CardinalDirection.SOUTH
        } else if (azimuth >= 202.5f && azimuth < 247.5f) {
            cardinalDirection = CardinalDirection.SOUTH_WEST
        } else if (azimuth >= 247.5f && azimuth < 292.5f) {
            cardinalDirection = CardinalDirection.WEST
        } else if (azimuth >= 292.5f && azimuth < 337.5f) {
            cardinalDirection = CardinalDirection.NORTH_WEST
        } else {
            cardinalDirection = CardinalDirection.NORTH
        }

        return cardinalDirection
    }
}
