package com.fittegame;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognitionClient;

public class GameService extends Service implements ConnectionCallbacks, OnConnectionFailedListener{

	private final IBinder mBinder = new LocalBinder();
	
	// Constants that define the activity detection interval
    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int DETECTION_INTERVAL_SECONDS = 50;
    public static final int DETECTION_INTERVAL_MILLISECONDS =
            MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS;
    
    private PendingIntent mActivityRecognitionPendingIntent;
    // Store the current activity recognition client
    private ActivityRecognitionClient mActivityRecognitionClient;

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
    private boolean mInProgress;

	public void onCreate(){
		

        // Start with the request flag set to false
        mInProgress = false;
		
		/*
         * Instantiate a new activity recognition client. Since the
         * parent Activity implements the connection listener and
         * connection failure listener, the constructor uses "this"
         * to specify the values of those parameters.
         */
        mActivityRecognitionClient =
                new ActivityRecognitionClient(getApplicationContext(), this, this);
        /*
         * Create the PendingIntent that Location Services uses
         * to send activity recognition updates back to this app.
         */
        Intent intent = new Intent(
                getApplicationContext(), ActivityRecognitionIntentService.class);
        /*
         * Return a PendingIntent that starts the IntentService.
         */
        mActivityRecognitionPendingIntent =
                PendingIntent.getService(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        
        startUpdates();
        Log.i("SERVICE", "Starting updated");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent,flags,startId);
	}
	
	public class LocalBinder extends Binder {
        public GameService getService() {
            return GameService.this;
        }
    }
	
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    /**
     * Request activity recognition updates based on the current
     * detection interval.
     *
     */
     public void startUpdates() {
        
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is in progress
            mInProgress = true;
            // Request a connection to Location Services
            mActivityRecognitionClient.connect();
        //
        } else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
    }

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		
		Log.e("Error", "Play services");
	}

	@Override
	public void onConnected(Bundle arg0) {
		
		/*
         * Request activity recognition updates using the preset
         * detection interval and PendingIntent. This call is
         * synchronous.
         */
        mActivityRecognitionClient.requestActivityUpdates(
                DETECTION_INTERVAL_MILLISECONDS,
                mActivityRecognitionPendingIntent);
        /*
         * Since the preceding call is synchronous, turn off the
         * in progress flag and disconnect the client
         */
        mInProgress = false;
        mActivityRecognitionClient.disconnect();
		
	}

	@Override
	public void onDisconnected() {
		
		// Turn off the request flag
        mInProgress = false;
        // Delete the client
        mActivityRecognitionClient = null;
	}
	
    /* Activity request methods */
    
    
}