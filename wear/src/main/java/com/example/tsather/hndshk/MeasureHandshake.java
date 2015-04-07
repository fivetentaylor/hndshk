package com.example.tsather.hndshk;

import android.app.Service;
import android.content.Intent;
import android.hardware.SensorEventListener;
import android.os.IBinder;
import java.util.Random;
import android.os.Binder;
import org.apache.commons.math3.transform.FastFourierTransformer;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.content.Context;

public class MeasureHandshake extends Service implements SensorEventListener {

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    // Random number generator
    private final Random mGenerator = new Random();

    private SensorManager mSensorManager;
    private Sensor mSensor;
    double ax,ay,az;   // these are the acceleration in x,y and z axis

    public void onCreate() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        MeasureHandshake getService() {
            // Return this instance of LocalService so clients can call public methods
            return MeasureHandshake.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** method for clients */
    public double getRandomNumber() {
        //return mSensorManager.registerListener();
        //return mGenerator.nextInt(100);
        return ax;
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION){
            ax=event.values[0];
            ay=event.values[1];
            az=event.values[2];
        }
    }
}
