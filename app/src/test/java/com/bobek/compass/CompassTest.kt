package com.bobek.compass

import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_MAGNETIC_FIELD
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_UI
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CompassTest {

    @InjectMocks
    lateinit var compass: Compass

    @Mock
    lateinit var sensorManager: SensorManager

    @Mock
    lateinit var compassListener: CompassListener

    @Mock
    lateinit var accelerometer: Sensor

    @Mock
    lateinit var magnetometer: Sensor

    @Test
    fun startRegistersListeners() {
        `when`(sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)).thenReturn(accelerometer)
        `when`(sensorManager.registerListener(compass, accelerometer, SENSOR_DELAY_UI)).thenReturn(true)
        `when`(sensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD)).thenReturn(magnetometer)
        `when`(sensorManager.registerListener(compass, magnetometer, SENSOR_DELAY_UI)).thenReturn(true)

        compass.start()

        verify(sensorManager).registerListener(compass, accelerometer, SENSOR_DELAY_UI)
        verify(sensorManager).registerListener(compass, magnetometer, SENSOR_DELAY_UI)
    }

    @Test(expected = AccelerometerNotAvailableException::class)
    fun startThrowsExceptionWhenAccelerometerIsNotAvailable() {
        `when`(sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)).thenReturn(null)

        compass.start()
    }

    @Test(expected = AccelerometerNotAvailableException::class)
    fun startThrowsExceptionWhenListeningToAccelerometerEventsFails() {
        `when`(sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)).thenReturn(accelerometer)
        `when`(sensorManager.registerListener(compass, accelerometer, SENSOR_DELAY_UI)).thenReturn(false)

        compass.start()
    }

    @Test(expected = MagnetometerNotAvailableException::class)
    fun startThrowsExceptionWhenMagnetometerIsNotAvailable() {
        `when`(sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)).thenReturn(accelerometer)
        `when`(sensorManager.registerListener(compass, accelerometer, SENSOR_DELAY_UI)).thenReturn(true)
        `when`(sensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD)).thenReturn(null)

        compass.start()
    }

    @Test(expected = MagnetometerNotAvailableException::class)
    fun startThrowsExceptionWhenListeningToMagnetometerEventsFails() {
        `when`(sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)).thenReturn(accelerometer)
        `when`(sensorManager.registerListener(compass, accelerometer, SENSOR_DELAY_UI)).thenReturn(true)
        `when`(sensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD)).thenReturn(magnetometer)
        `when`(sensorManager.registerListener(compass, magnetometer, SENSOR_DELAY_UI)).thenReturn(false)

        compass.start()
    }

    @Test
    fun stopUnregistersListeners() {
        compass.stop()

        verify(sensorManager).unregisterListener(compass)
    }
}
