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
