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

import android.content.Context
import android.util.AttributeSet
import android.view.Surface
import androidx.annotation.IdRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.bobek.compass.R
import com.bobek.compass.model.Azimuth
import kotlin.math.roundToInt

private const val CARDINAL_DIRECTION_TEXT_RATIO = 0.23f
private const val DEGREE_TEXT_RATIO = 0.08f

class CompassView(context: Context, attributeSet: AttributeSet) : ConstraintLayout(context, attributeSet) {

    @IdRes
    private val center = R.id.compass_rose_image

    private lateinit var statusDegreesText: AppCompatTextView
    private lateinit var statusCardinalDirectionText: AppCompatTextView
    private lateinit var compassRoseImage: AppCompatImageView

    private var azimuth = Azimuth(0f)

    init {
        inflate(context, R.layout.compass_view, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        statusDegreesText = findViewById(R.id.status_degrees_text)
        statusCardinalDirectionText = findViewById(R.id.status_cardinal_direction_text)
        compassRoseImage = findViewById(R.id.compass_rose_image)
    }

    fun update(azimuth: Azimuth) {
        this.azimuth = azimuth + displayRotation()
        updateView()
    }

    private fun displayRotation(): Float {
        return when (display?.rotation) {
            Surface.ROTATION_90 -> 90f
            Surface.ROTATION_180 -> 180f
            Surface.ROTATION_270 -> 270f
            else -> 0f
        }
    }

    private fun updateView() {
        updateStatusDegreesText()
        updateStatusDirectionText()

        val rotation = azimuth.degrees.unaryMinus()
        rotateCompassRoseImage(rotation)
        rotateCompassRoseTexts(rotation)
    }

    private fun updateStatusDegreesText() {
        statusDegreesText.text = context.getString(R.string.degrees, azimuth.degrees.roundToInt())
    }

    private fun updateStatusDirectionText() {
        statusCardinalDirectionText.text = context.getString(azimuth.cardinalDirection.labelResourceId)
    }

    private fun rotateCompassRoseImage(rotation: Float) {
        compassRoseImage.rotation = rotation
    }

    private fun rotateCompassRoseTexts(rotation: Float) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(this)

        rotateCardinalDirectionTexts(constraintSet, rotation)
        rotateDegreeTexts(constraintSet, rotation)

        constraintSet.applyTo(this)
    }

    private fun rotateCardinalDirectionTexts(constraintSet: ConstraintSet, rotation: Float) {
        val radius = calculateTextRadius(CARDINAL_DIRECTION_TEXT_RATIO)

        constraintSet.constrainCircle(R.id.cardinal_direction_north_text, center, radius, rotation)
        constraintSet.constrainCircle(R.id.cardinal_direction_east_text, center, radius, rotation + 90)
        constraintSet.constrainCircle(R.id.cardinal_direction_south_text, center, radius, rotation + 180)
        constraintSet.constrainCircle(R.id.cardinal_direction_west_text, center, radius, rotation + 270)
    }

    private fun rotateDegreeTexts(constraintSet: ConstraintSet, rotation: Float) {
        val radius = calculateTextRadius(DEGREE_TEXT_RATIO)

        constraintSet.constrainCircle(R.id.degree_0_text, center, radius, rotation)
        constraintSet.constrainCircle(R.id.degree_30_text, center, radius, rotation + 30)
        constraintSet.constrainCircle(R.id.degree_60_text, center, radius, rotation + 60)
        constraintSet.constrainCircle(R.id.degree_90_text, center, radius, rotation + 90)
        constraintSet.constrainCircle(R.id.degree_120_text, center, radius, rotation + 120)
        constraintSet.constrainCircle(R.id.degree_150_text, center, radius, rotation + 150)
        constraintSet.constrainCircle(R.id.degree_180_text, center, radius, rotation + 180)
        constraintSet.constrainCircle(R.id.degree_210_text, center, radius, rotation + 210)
        constraintSet.constrainCircle(R.id.degree_240_text, center, radius, rotation + 240)
        constraintSet.constrainCircle(R.id.degree_270_text, center, radius, rotation + 270)
        constraintSet.constrainCircle(R.id.degree_300_text, center, radius, rotation + 300)
        constraintSet.constrainCircle(R.id.degree_330_text, center, radius, rotation + 330)
    }

    private fun calculateTextRadius(ratio: Float): Int {
        return width / 2 - (width * ratio).toInt()
    }
}
