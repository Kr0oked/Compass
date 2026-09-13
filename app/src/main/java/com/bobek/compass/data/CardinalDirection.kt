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

import androidx.annotation.StringRes
import com.bobek.compass.R

enum class CardinalDirection(@StringRes val labelResourceId: Int) {
    NORTH(R.string.cardinal_direction_north),
    NORTH_NORTHEAST(R.string.cardinal_direction_north_northeast),
    NORTHEAST(R.string.cardinal_direction_northeast),
    EAST_NORTHEAST(R.string.cardinal_direction_east_northeast),
    EAST(R.string.cardinal_direction_east),
    EAST_SOUTHEAST(R.string.cardinal_direction_east_southeast),
    SOUTHEAST(R.string.cardinal_direction_southeast),
    SOUTH_SOUTHEAST(R.string.cardinal_direction_south_southeast),
    SOUTH(R.string.cardinal_direction_south),
    SOUTH_SOUTHWEST(R.string.cardinal_direction_south_southwest),
    SOUTHWEST(R.string.cardinal_direction_southwest),
    WEST_SOUTHWEST(R.string.cardinal_direction_west_southwest),
    WEST(R.string.cardinal_direction_west),
    WEST_NORTHWEST(R.string.cardinal_direction_west_northwest),
    NORTHWEST(R.string.cardinal_direction_northwest),
    NORTH_NORTHWEST(R.string.cardinal_direction_north_northwest)
}
