package com.fittegame;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class Utils {

    public static String getUsername( Context c ){
    	SharedPreferences prefs;
        prefs = c.getSharedPreferences("com.fittegame", Context.MODE_PRIVATE);
    	
        return prefs.getString(MainActivity.USER_NAME, "Desconocido");
    }
    
    public static int getPoints( Context c ){
    	SharedPreferences prefs;
        prefs = c.getSharedPreferences("com.fittegame", Context.MODE_PRIVATE);
    	
        return prefs.getInt(MainActivity.USER_POINTS, 0);
    }
    
	
	public static void ga_goRanking( Fitte app ){
		
        Tracker tracker = app.getTracker(Fitte.TrackerName.APP_TRACKER);
        String name = getUsername( app.getApplicationContext() );
        
		tracker.send(new HitBuilders.EventBuilder()
	    .setCategory("Ranking")
	    .setAction("Toco el link")
	    .setLabel(name)
	    .setValue(1)
	    .build());
	}
	
	public static void ga_resetPoints( Fitte app ){

        Tracker tracker = app.getTracker(Fitte.TrackerName.APP_TRACKER);
        String name = getUsername( app.getApplicationContext() );
        
		tracker.send(new HitBuilders.EventBuilder()
	    .setCategory("Puntos")
	    .setAction("Reset")
	    .setLabel(name)
	    .setValue(1)
	    .build());
	}
	
	public static void ga_addPoints( Fitte app ){

        Tracker tracker = app.getTracker(Fitte.TrackerName.APP_TRACKER);
        String name = getUsername( app.getApplicationContext() );
        
		tracker.send(new HitBuilders.EventBuilder()
	    .setCategory("Puntos")
	    .setAction("subio")
	    .setLabel(name)
	    .setValue(4)
	    .build());
	}
	
}
