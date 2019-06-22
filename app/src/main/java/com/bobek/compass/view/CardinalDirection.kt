package com.bobek.compass.view

import androidx.annotation.StringRes
import com.bobek.compass.R

enum class CardinalDirection(@StringRes val abbreviationResourceId: Int) {
    NORTH(R.string.cardinal_direction_north),
    NORTH_EAST(R.string.cardinal_direction_north_east),
    EAST(R.string.cardinal_direction_east),
    SOUTH_EAST(R.string.cardinal_direction_south_east),
    SOUTH(R.string.cardinal_direction_south),
    SOUTH_WEST(R.string.cardinal_direction_south_west),
    WEST(R.string.cardinal_direction_west),
    NORTH_WEST(R.string.cardinal_direction_north_west)
}
