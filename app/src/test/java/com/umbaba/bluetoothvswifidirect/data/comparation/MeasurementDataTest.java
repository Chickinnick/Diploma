package com.umbaba.bluetoothvswifidirect.data.comparation;

import junit.framework.Assert;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Nick on 21.05.2017.
 */
public class MeasurementDataTest {

    @Test
    public void calcData() throws Exception {
        MeasurementData measurementData = new MeasurementData(5000, 12);
        measurementData.calcSpeed(10000, 9000);
        double speed = measurementData.getSpeed();
        Assert.assertEquals(0.5, speed);
    }
}