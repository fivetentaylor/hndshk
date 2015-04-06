package com.example.tsather.hndshk;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import java.util.Random;
import android.os.Binder;
import org.apache.commons.math3.transform.FastFourierTransformer;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.content.Context;

public class MeasureHandshake extends Service {

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    // Random number generator
    private final Random mGenerator = new Random();

    private SensorManager mSensorManager;
    private Sensor mSensor;



    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        MeasureHandshake getService() {
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

            // Return this instance of LocalService so clients can call public methods
            return MeasureHandshake.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** method for clients */
    public int getRandomNumber() {
        //return mSensorManager.registerListener();
        return mGenerator.nextInt(100);
    }
}
