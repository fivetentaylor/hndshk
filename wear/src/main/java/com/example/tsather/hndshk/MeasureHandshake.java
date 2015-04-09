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
//import org.apache.commons.collections4.Q
import android.support.v4.content.LocalBroadcastManager;
import android.os.Handler;


public class MeasureHandshake extends Service implements SensorEventListener {

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    // Random number generator
    private final Random mGenerator = new Random();

    private SensorManager mSensorManager;
    private Sensor mSensor;
    double ax,ay,az;   // these are the acceleration in x,y and z axis
    float[][] signal;
    float[][] window;
    int BUFFER_SIZE = 1024;
    int WINDOW_SIZE = 512;
    int index = 0;

    private Handler handler;

    private Runnable runnable;

    public void onCreate() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), 20000);
        signal = new float[3][BUFFER_SIZE];
        window = new float[3][WINDOW_SIZE];

        runnable = new Runnable() {
            @Override
            public void run() {
                /* do what you need to do */
                sendMessage();
                /* and here comes the "trick" */
                handler.postDelayed(this, 1000);
            }
        };

        handler = new Handler();
        handler.postDelayed(runnable, 1000);
    }

    // Send an Intent with an action named "my-event".
    private void sendMessage() {
        Intent intent = new Intent("my-event");
        // add data
        intent.putExtra("message", "data");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
    public float[][] getRandomNumber() {
        sendMessage();
        int start = ((index - WINDOW_SIZE) + BUFFER_SIZE) % BUFFER_SIZE;
        int j = 0;
        for(int i = 0; i < WINDOW_SIZE; i++) {
            j = (start+i) % BUFFER_SIZE;
            float x = signal[0][j];
            float y = signal[1][j];
            float z = signal[2][j];
            window[0][i] = signal[0][j];
            window[1][i] = signal[1][j];
            window[2][i] = signal[2][j];
            //window[0][i] = signal[0][i];
            //window[1][i] = signal[1][i];
            //window[2][i] = signal[2][i];
        }
        return window;
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION){
            signal[0][index] = event.values[0];
            signal[1][index] = event.values[1];
            signal[2][index] = event.values[2];
            index = (index + 1) % BUFFER_SIZE;
        }
    }
}
