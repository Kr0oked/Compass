package com.bobek.compass.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.support.constraint.Guideline
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import com.bobek.compass.R
import kotlin.math.roundToInt

private const val NORTH_OFFSET = 0
private const val EAST_OFFSET = 90
private const val SOUTH_OFFSET = 180
private const val WEST_OFFSET = 270

class Compass(context: Context, attributeSet: AttributeSet) : ConstraintLayout(context, attributeSet) {

    private var compassRoseRootLayout: ConstraintLayout
    private var statusDegreesText: AppCompatTextView
    private var statusCardinalDirectionText: AppCompatTextView
    private var compassRoseImage: AppCompatImageView
    private var degreeGuideline: Guideline
    private var cardinalDirectionGuideline: Guideline

    init {
        inflate(context, R.layout.compass, this)

        compassRoseRootLayout = findViewById(R.id.compass_root_layout)
        statusDegreesText = findViewById(R.id.status_degrees_text)
        statusCardinalDirectionText = findViewById(R.id.status_cardinal_direction_text)
        compassRoseImage = findViewById(R.id.compass_rose_image)
        degreeGuideline = findViewById(R.id.degree_guideline)
        cardinalDirectionGuideline = findViewById(R.id.cardinal_direction_guideline)
    }

    fun update(azimuth: Float) {
        statusDegreesText.text = context.getString(R.string.degrees, azimuth.roundToInt())

        val cardinalDirection = CompassUtils.determineCardinalDirection(azimuth)
        statusCardinalDirectionText.text = context.getString(cardinalDirection.abbreviationResourceId)

        val angle = azimuth.unaryMinus()
        compassRoseImage.rotation = angle

        rotateTexts(angle)
    }

    private fun rotateTexts(angle: Float) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(compassRoseRootLayout)

        rotateCardinalDirectionTexts(constraintSet, angle)
        rotateDegreeTexts(constraintSet, angle)

        constraintSet.applyTo(compassRoseRootLayout)
    }

    private fun rotateCardinalDirectionTexts(constraintSet: ConstraintSet, angle: Float) {
        val radius = calculateTextRadius(cardinalDirectionGuideline)
        val northAngle = angle + NORTH_OFFSET
        val eastAngle = angle + EAST_OFFSET
        val southAngle = angle + SOUTH_OFFSET
        val westAngle = angle + WEST_OFFSET

        constraintSet.constrainCircle(R.id.cardinal_direction_north_text, R.id.compass_rose_image, radius, northAngle)
        constraintSet.constrainCircle(R.id.cardinal_direction_east_text, R.id.compass_rose_image, radius, eastAngle)
        constraintSet.constrainCircle(R.id.cardinal_direction_south_text, R.id.compass_rose_image, radius, southAngle)
        constraintSet.constrainCircle(R.id.cardinal_direction_west_text, R.id.compass_rose_image, radius, westAngle)
    }

    private fun rotateDegreeTexts(constraintSet: ConstraintSet, angle: Float) {
        val radius = calculateTextRadius(degreeGuideline)

        constraintSet.constrainCircle(R.id.degree_0_text, R.id.compass_rose_image, radius, angle)
        constraintSet.constrainCircle(R.id.degree_30_text, R.id.compass_rose_image, radius, angle + 30)
        constraintSet.constrainCircle(R.id.degree_60_text, R.id.compass_rose_image, radius, angle + 60)
        constraintSet.constrainCircle(R.id.degree_90_text, R.id.compass_rose_image, radius, angle + 90)
        constraintSet.constrainCircle(R.id.degree_120_text, R.id.compass_rose_image, radius, angle + 120)
        constraintSet.constrainCircle(R.id.degree_150_text, R.id.compass_rose_image, radius, angle + 150)
        constraintSet.constrainCircle(R.id.degree_180_text, R.id.compass_rose_image, radius, angle + 180)
        constraintSet.constrainCircle(R.id.degree_210_text, R.id.compass_rose_image, radius, angle + 210)
        constraintSet.constrainCircle(R.id.degree_240_text, R.id.compass_rose_image, radius, angle + 240)
        constraintSet.constrainCircle(R.id.degree_270_text, R.id.compass_rose_image, radius, angle + 270)
        constraintSet.constrainCircle(R.id.degree_300_text, R.id.compass_rose_image, radius, angle + 300)
        constraintSet.constrainCircle(R.id.degree_330_text, R.id.compass_rose_image, radius, angle + 330)
    }

    private fun calculateTextRadius(guideline: Guideline): Int {
        val guidelineLocation = IntArray(2)
        guideline.getLocationInWindow(guidelineLocation)

        return width / 2 - guidelineLocation.first()
    }
}
