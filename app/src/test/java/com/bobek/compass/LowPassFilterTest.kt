package com.bobek.compass

import org.junit.Assert.assertEquals
import org.junit.Test

class LowPassFilterTest {

    @Test
    fun filter() {
        val lowPassFilter = LowPassFilter(0.5f)
        assertEquals(lowPassFilter.filter(SensorValues(0.0f, 1.0f, 2.0f)), SensorValues(0.0f, 1.0f, 2.0f))
        assertEquals(lowPassFilter.filter(SensorValues(1.0f, 1.0f, 1.0f)), SensorValues(0.5f, 1.0f, 1.5f))
        assertEquals(lowPassFilter.filter(SensorValues(1.0f, 1.0f, 1.0f)), SensorValues(0.75f, 1.0f, 1.25f))
        assertEquals(lowPassFilter.filter(SensorValues(1.0f, 1.0f, 1.0f)), SensorValues(0.875f, 1.0f, 1.125f))
        assertEquals(lowPassFilter.filter(SensorValues(1.0f, 1.0f, 1.0f)), SensorValues(0.9375f, 1.0f, 1.0625f))
        assertEquals(lowPassFilter.filter(SensorValues(1.0f, 1.0f, 1.0f)), SensorValues(0.96875f, 1.0f, 1.03125f))
    }

    @Test(expected = IllegalStateException::class)
    fun filterThrowsExceptionWhenAlphaIsTooBig() {
        LowPassFilter(1.0f)
    }

    @Test(expected = IllegalStateException::class)
    fun filterThrowsExceptionWhenAlphaIsTooSmall() {
        LowPassFilter(0.0f)
    }

    @Test
    fun reset() {
        val lowPassFilter = LowPassFilter(0.5f)
        assertEquals(lowPassFilter.filter(SensorValues(0.0f, 0.0f, 0.0f)), SensorValues(0.0f, 0.0f, 0.0f))
        assertEquals(lowPassFilter.filter(SensorValues(1.0f, 1.0f, 1.0f)), SensorValues(0.5f, 0.5f, 0.5f))
        lowPassFilter.reset()
        assertEquals(lowPassFilter.filter(SensorValues(0.0f, 0.0f, 0.0f)), SensorValues(0.0f, 0.0f, 0.0f))
        assertEquals(lowPassFilter.filter(SensorValues(1.0f, 1.0f, 1.0f)), SensorValues(0.5f, 0.5f, 0.5f))
    }
}
