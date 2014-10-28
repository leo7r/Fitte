package com.fittegame;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * Service that receives ActivityRecognition updates. It receives
 * updates in the background, even if the main Activity is not visible.
 */
public class ActivityRecognitionIntentService extends IntentService {
	
	public ActivityRecognitionIntentService() {
		super("fru");
		// TODO Auto-generated constructor stub
	}

	/**
     * Called when a new activity detection update is available.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
    	
    	// If the incoming intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {
            // Get the update
            ActivityRecognitionResult result =
                    ActivityRecognitionResult.extractResult(intent);
            // Get the most probable activity
            DetectedActivity mostProbableActivity =
                    result.getMostProbableActivity();
            /*
             * Get the probability that this activity is the
             * the user's actual activity
             */
            int confidence = mostProbableActivity.getConfidence();
            /*
             * Get an integer describing the type of activity
             */
            int activityType = mostProbableActivity.getType();
            String activityName = getNameFromType(activityType);
            
            /*
             * At this point, you have retrieved all the information
             * for the current update. You can display this
             * information to the user in a notification, or
             * send it to an Activity or Service in a broadcast
             * Intent.
             */
            
            if ( confidence > 80 ){
            	
            	SharedPreferences prefs;
                prefs = this.getSharedPreferences("com.fittegame", Context.MODE_PRIVATE);
            	
            	if ( activityName.equals("on_foot") ){
            		
            		int last_points = prefs.getInt(MainActivity.USER_POINTS, 0);
            		int new_points = last_points+4;
            		prefs.edit().putInt(MainActivity.USER_POINTS,new_points ).commit();
            		
            		Utils.ga_addPoints((Fitte)getApplication());
            		
            		//String id = prefs.getString(MainActivity.USER_ID, "-1");
            		
            		//new UpdatePoins( getApplicationContext() , id , 4 ).execute();
            		
            		/*if ( new_points >= minPointsForNextLevel() ){
            			sendNotification();
            		}  */          		
            	}
            }
            
        } else {
            /*
             * This implementation ignores intents that don't contain
             * an activity update. If you wish, you can report them as
             * errors.
             */
        }
    	
    }
    
    private String getNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.WALKING:
            	return "walking";
            case DetectedActivity.RUNNING:
            	return "running";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
        }
        return "unknown";
    }
        
    public void sendNotification(){
    	
    	NotificationCompat.Builder mBuilder =
    		    new NotificationCompat.Builder(this)
    		    .setSmallIcon(R.drawable.logo)
    		    .setContentTitle("Puedes mejorar tu heroe!")
    		    .setContentText("Toca para subir algun atributo");
    	
    	Intent resultIntent = new Intent(this, MainActivity.class);
    	
    	PendingIntent resultPendingIntent =
    	    PendingIntent.getActivity(
    	    this,
    	    0,
    	    resultIntent,
    	    PendingIntent.FLAG_UPDATE_CURRENT
    	);
    	
    	mBuilder.setContentIntent(resultPendingIntent);
    	
    	// Sets an ID for the notification
    	int mNotificationId = 001;
    	// Gets an instance of the NotificationManager service
    	NotificationManager mNotifyMgr = 
    	        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    	// Builds the notification and issues it.
    	
    	Notification notif = mBuilder.build();
    	notif.flags |= Notification.FLAG_AUTO_CANCEL;
    	
    	mNotifyMgr.notify(mNotificationId, notif );    	
    }
    
    class UpdatePoins extends ServerConnection{
		
		public UpdatePoins( Context c , String id , int points ){
			super();
			
			init(c, "raise_points" , new String[]{ id , points+"" } );
		}
		
		@Override
		public void onComplete(String result) {
			
			try{
				int points = Integer.parseInt(result);
				
	        	SharedPreferences prefs;
	            prefs = getSharedPreferences("com.fittegame", Context.MODE_PRIVATE);
	            
	            prefs.edit().putInt(MainActivity.USER_POINTS, points).commit();
			}
			catch( Exception e ){
				e.printStackTrace();
			}
		}
		
	}

}
