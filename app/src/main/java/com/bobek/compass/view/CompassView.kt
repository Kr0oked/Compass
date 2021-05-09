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
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.TypedValue.COMPLEX_UNIT_PX
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

private const val STATUS_TEXT_SIZE_FACTOR = 0.08f
private const val CARDINAL_DIRECTION_TEXT_SIZE_FACTOR = 0.08f
private const val DEGREE_TEXT_SIZE_FACTOR = 0.03f

class CompassView(context: Context, attributeSet: AttributeSet) : ConstraintLayout(context, attributeSet) {

    @IdRes
    private val center = R.id.compass_rose_image

    private lateinit var compassRoseImage: AppCompatImageView

    private lateinit var statusDegreesText: AppCompatTextView
    private lateinit var statusCardinalDirectionText: AppCompatTextView

    private lateinit var cardinalDirectionNorthText: AppCompatTextView
    private lateinit var cardinalDirectionEastText: AppCompatTextView
    private lateinit var cardinalDirectionSouthText: AppCompatTextView
    private lateinit var cardinalDirectionWestText: AppCompatTextView

    private lateinit var degree0Text: AppCompatTextView
    private lateinit var degree30Text: AppCompatTextView
    private lateinit var degree60Text: AppCompatTextView
    private lateinit var degree90Text: AppCompatTextView
    private lateinit var degree120Text: AppCompatTextView
    private lateinit var degree150Text: AppCompatTextView
    private lateinit var degree180Text: AppCompatTextView
    private lateinit var degree210Text: AppCompatTextView
    private lateinit var degree240Text: AppCompatTextView
    private lateinit var degree270Text: AppCompatTextView
    private lateinit var degree300Text: AppCompatTextView
    private lateinit var degree330Text: AppCompatTextView

    var azimuth = Azimuth(0f)

    init {
        inflate(context, R.layout.compass_view, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        compassRoseImage = findViewById(R.id.compass_rose_image)

        statusDegreesText = findViewById(R.id.status_degrees_text)
        statusCardinalDirectionText = findViewById(R.id.status_cardinal_direction_text)

        cardinalDirectionNorthText = findViewById(R.id.cardinal_direction_north_text)
        cardinalDirectionEastText = findViewById(R.id.cardinal_direction_east_text)
        cardinalDirectionSouthText = findViewById(R.id.cardinal_direction_south_text)
        cardinalDirectionWestText = findViewById(R.id.cardinal_direction_west_text)

        degree0Text = findViewById(R.id.degree_0_text)
        degree30Text = findViewById(R.id.degree_30_text)
        degree60Text = findViewById(R.id.degree_60_text)
        degree90Text = findViewById(R.id.degree_90_text)
        degree120Text = findViewById(R.id.degree_120_text)
        degree150Text = findViewById(R.id.degree_150_text)
        degree180Text = findViewById(R.id.degree_180_text)
        degree210Text = findViewById(R.id.degree_210_text)
        degree240Text = findViewById(R.id.degree_240_text)
        degree270Text = findViewById(R.id.degree_270_text)
        degree300Text = findViewById(R.id.degree_300_text)
        degree330Text = findViewById(R.id.degree_330_text)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        updateStatusTextSize(width * STATUS_TEXT_SIZE_FACTOR)
        updateCardinalDirectionTextSize(width * CARDINAL_DIRECTION_TEXT_SIZE_FACTOR)
        updateDegreeTextSize(width * DEGREE_TEXT_SIZE_FACTOR)
    }

    private fun updateStatusTextSize(textSize: Float) {
        statusDegreesText.setTextSize(COMPLEX_UNIT_PX, textSize)
        statusCardinalDirectionText.setTextSize(COMPLEX_UNIT_PX, textSize)
    }

    private fun updateCardinalDirectionTextSize(textSize: Float) {
        cardinalDirectionNorthText.setTextSize(COMPLEX_UNIT_PX, textSize)
        cardinalDirectionEastText.setTextSize(COMPLEX_UNIT_PX, textSize)
        cardinalDirectionSouthText.setTextSize(COMPLEX_UNIT_PX, textSize)
        cardinalDirectionWestText.setTextSize(COMPLEX_UNIT_PX, textSize)
    }

    private fun updateDegreeTextSize(textSize: Float) {
        degree0Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        degree30Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        degree60Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        degree90Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        degree120Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        degree150Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        degree180Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        degree210Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        degree240Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        degree270Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        degree300Text.setTextSize(COMPLEX_UNIT_PX, textSize)
        degree330Text.setTextSize(COMPLEX_UNIT_PX, textSize)
    }

    override fun dispatchDraw(canvas: Canvas?) {
        updateViews(azimuth + displayRotation())
        super.dispatchDraw(canvas)
    }

    private fun displayRotation(): Float {
        return when (display?.rotation) {
            Surface.ROTATION_90 -> 90f
            Surface.ROTATION_180 -> 180f
            Surface.ROTATION_270 -> 270f
            else -> 0f
        }
    }

    private fun updateViews(azimuth: Azimuth) {
        updateStatusDegreesText(azimuth)
        updateStatusDirectionText(azimuth)

        val rotation = azimuth.degrees.unaryMinus()
        rotateCompassRoseImage(rotation)
        rotateCompassRoseTexts(rotation)
    }

    private fun updateStatusDegreesText(azimuth: Azimuth) {
        statusDegreesText.text = context.getString(R.string.degrees, azimuth.degrees.roundToInt())
    }

    private fun updateStatusDirectionText(azimuth: Azimuth) {
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
