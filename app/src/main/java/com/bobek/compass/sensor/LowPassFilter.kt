package com.bobek.compass.sensor

class LowPassFilter(private val alpha: Float) {

    private var previousValues: SensorValues? = null

    init {
        check(alpha > 0.0f && alpha < 1.0f) { "Alpha must be between 0 and 1" }
    }

    fun filter(values: SensorValues): SensorValues {
        val filteredValues = previousValues?.let { previousValues -> filter(values, previousValues) } ?: values
        previousValues = filteredValues
        return filteredValues
    }

    private fun filter(newValues: SensorValues, lastValues: SensorValues): SensorValues {
        val x = filter(newValues.x, lastValues.x)
        val y = filter(newValues.y, lastValues.y)
        val z = filter(newValues.z, lastValues.z)

        return SensorValues(x, y, z)
    }

    private fun filter(newValue: Float, lastValue: Float): Float {
        return lastValue + alpha * (newValue - lastValue)
    }

    fun reset() {
        previousValues = null
    }
}
