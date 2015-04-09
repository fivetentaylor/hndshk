package com.example.tsather.hndshk;

import android.app.Activity;
import android.os.Bundle;
import android.os.IBinder;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.commons.math3.transform.FastFourierTransformer;
import com.example.tsather.hndshk.MeasureHandshake;
import com.example.tsather.hndshk.MeasureHandshake.LocalBinder;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.view.View;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.util.Log;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class MainActivity extends Activity {

    MeasureHandshake mService;
    boolean mBound = false;
    private TextView mTextView;
    private MediaPlayer mPlayer;
    private MediaPlayer knuckles;
    private MediaPlayer trad;
    private MediaPlayer slap;
    private MediaPlayer five;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });
        knuckles = MediaPlayer.create(this, R.raw.knuckles);
        trad = MediaPlayer.create(this, R.raw.trad);
        slap = MediaPlayer.create(this, R.raw.slap);
        five = MediaPlayer.create(this, R.raw.five);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, MeasureHandshake.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        knuckles.release();
        trad.release();
        slap.release();
        five.release();
    }

    /** Called when a button is clicked (the button in the layout file attaches to
     * this method with the android:onClick attribute) */
    public void onButtonClick(View v) {
        if (mBound) {
            // Call a method from the LocalService.
            // However, if this call were something that might hang, then this request should
            // occur in a separate thread to avoid slowing down the activity performance.
            double[][] signal = mService.getRandomNumber();
            Toast.makeText(this, "number: " + signal[0][511], Toast.LENGTH_SHORT).show();
        }
    }

    public void playSoundOnClick(View V) {
        knuckles.start();
        //Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        //v.vibrate(500);
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("my-event"));
    }

    // handler for received Intents for the "my-event" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("receiver", "Got message: " + message);
            if( message == "five") {
                five.start();
            } else if ( message == "knuckles" ) {
                knuckles.start();
            } else if ( message == "slap" ) {
                slap.start();
            } else if ( message == "trad" ) {
                trad.start();
            } else {
                trad.start();
            }
        }
    };

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }
}
