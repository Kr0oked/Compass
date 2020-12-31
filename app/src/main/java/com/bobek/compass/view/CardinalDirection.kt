/*
 * This file is part of Compass.
 * Copyright (C) 2020 Philipp Bobek <philipp.bobek@mailbox.org>
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
