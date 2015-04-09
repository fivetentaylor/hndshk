package com.example.tsather.hndshk;

import android.app.Service;
import android.content.Intent;
import android.hardware.SensorEventListener;
import android.os.IBinder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.Iterator;


public class MeasureHandshake extends Service implements SensorEventListener {

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    // Random number generator
    private final Random mGenerator = new Random();

    private SensorManager mSensorManager;
    private Sensor mSensor;
    double ax,ay,az;   // these are the acceleration in x,y and z axis
    double[][] signal;
    double[][] window;
    double[][] knuckles;
    double[][] trad;
    double[][] five;
    double [][] slap;
    int BUFFER_SIZE = 1024;
    int WINDOW_SIZE = 512;
    int index = 0;

    private Handler handler;

    private Runnable runnable;

    public String readTextFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {

        }
        return outputStream.toString();
    }

    public void onCreate() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), 20000);
        signal = new double[3][BUFFER_SIZE];
        window = new double[3][WINDOW_SIZE];
        knuckles = new double[3][WINDOW_SIZE];
        trad = new double[3][WINDOW_SIZE];
        five = new double[3][WINDOW_SIZE];
        slap = new double[3][WINDOW_SIZE];

        runnable = new Runnable() {
            @Override
            public void run() {
                /* do what you need to do */
                sendMessage( "slap" );
                /* and here comes the "trick" */
                handler.postDelayed(this, 1000);
            }
        };

        handler = new Handler();
        handler.postDelayed(runnable, 1000);

        loadGesture(knuckles, getCsv("fistbump__2"));
        loadGesture(trad, getCsv("handshake__2"));
        loadGesture(five, getCsv("hifive__2"));
        loadGesture(slap, getCsv("slap__2"));
    }

    private String getCsv( String name ) {
        InputStream ins = getResources().openRawResource(
                getResources().getIdentifier("raw/" + name,
                        name, getPackageName()));
        return readTextFile(ins);
    }

    private void loadGesture( double[][] gesture, String csv ) {
        String[] lines = csv.split("\n");
        int i = 0;
        for( String record: lines ) {
            String[] fields = record.split(",");
            gesture[0][i] = Double.parseDouble(fields[0]);
            gesture[1][i] = Double.parseDouble(fields[1]);
            gesture[2][i] = Double.parseDouble(fields[2]);
            i++;
        }
    }

    // Send an Intent with an action named "my-event".
    private void sendMessage( String message ) {
        Intent intent = new Intent("my-event");
        // add data
        intent.putExtra("message", message);
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


    private double[] variance( double[] data ) {
        int n = 0;
        double mean = 0.0;
        double delta = 0.0;
        double M2 = 0.0;
        double variance = 0.0;

        for( double x: data ) {
            n++;
            delta = x - mean;
            mean = mean + (delta / n);
            M2 = M2 + delta * (x - mean);
        }

        if ( n < 2 ) {
            return new double[] {data[0], 0.0};
        }
        variance = M2/(n - 1);
        return new double[] {mean, variance};
    }

    private void normalize( double[] data ) {
        double[] ms = variance(data);
        for( int i = 0; i < data.length; i++ ) {
            data[i] = (data[i] - ms[0]) / ms[1];
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** method for clients */
    public double[][] getRandomNumber() {
        sendMessage( "five" );
        int start = ((index - WINDOW_SIZE) + BUFFER_SIZE) % BUFFER_SIZE;
        int j = 0;
        for(int i = 0; i < WINDOW_SIZE; i++) {
            j = (start+i) % BUFFER_SIZE;
            double x = signal[0][j];
            double y = signal[1][j];
            double z = signal[2][j];
            window[0][i] = signal[0][j];
            window[1][i] = signal[1][j];
            window[2][i] = signal[2][j];
        }
        normalize(window[0]);
        normalize(window[1]);
        normalize(window[2]);
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
