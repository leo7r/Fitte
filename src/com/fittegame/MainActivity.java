package com.fittegame;

import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.fittegame.GameService.LocalBinder;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class MainActivity extends FragmentActivity {

	/* ALERTA DRY RUN */
	/* ALERTA DRY RUN */
	/* ALERTA DRY RUN */
	
	public final static boolean DRY_RUN = false;
	
	/* ALERTA DRY RUN */
	/* ALERTA DRY RUN */
	/* ALERTA DRY RUN */
	
	boolean mBound;
	GameService gameS;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
	/* Facebook */
	private static final String TAG = "Signup";
	private UiLifecycleHelper uiHelper;
    boolean fb_info_ready = false;
	ProgressDialog dialog;
    
    public final static String USER_ID = "user_id";
    public final static String USER_NAME = "user_name";
    public final static String USER_POINTS = "user_points";
    public final static String EGG_TYPE = "egg_type";
    public final static String EGG_POINTS = "egg_points";
    public final static String MONSTER_LEVEL = "monster_egg";
    
    public final static int FIRE = 1;
    public final static int WATER = 2;
    public final static int EARTH = 3;
    public final static int AIR = 4;
    
    public final static int MAX_PROGRESS_EGG = 122*7;
    
    private boolean mAutoIncrement = false;
    private Handler repeatUpdateHandler = new Handler();
    public final static int REP_DELAY = 100;
    MediaPlayer mp;
    
	ImageLoader imageLoader;
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        if (servicesConnected()) {

        	Intent intent = new Intent(this, GameService.class);
            startService(intent);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
        else{
        	Log.e("Error", "Play services");
        }
        
        /* Login con Facebook */
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
        LoginButton authButton = (LoginButton) findViewById(R.id.authButton);
        authButton.setReadPermissions(Arrays.asList("email","public_profile","user_friends"));

    	LinearLayout welcome_cont = (LinearLayout)findViewById(R.id.welcome_container);
    	LinearLayout logged_cont = (LinearLayout)findViewById(R.id.logged_container);
    	
        if ( getId().equals("-1") ){
        	welcome_cont.setVisibility(View.VISIBLE);
        	logged_cont.setVisibility(View.GONE);
        }
        else{
        	welcome_cont.setVisibility(View.GONE);
        	logged_cont.setVisibility(View.VISIBLE);
        }
                
        // GAnalytics
        
        ((Fitte) getApplication()).getTracker(Fitte.TrackerName.APP_TRACKER);
        
        GoogleAnalytics ga = GoogleAnalytics.getInstance(getApplicationContext());//.enableAutoActivityReports(getApplication());
        ga.setDryRun(DRY_RUN);
        ga.reportActivityStart(this);

        setEggType(-1);
        setMonsterLevel(-1);

	    /*
    	SharedPreferences prefs;    	
	    prefs = this.getSharedPreferences("com.fittegame", Context.MODE_PRIVATE);
	    prefs.edit().putInt(EGG_POINTS, 121*7).commit();
	    
	    prefs.edit().putInt(USER_POINTS, 3000).commit();*/
	}
	
	public void onStop(){
		super.onStop();
		
		GoogleAnalytics.getInstance(getApplicationContext()).reportActivityStop(this);
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    
	    // For scenarios where the main activity is launched and user
	    // session is not null, the session state change notification
	    // may not be triggered. Trigger it if it's open/closed.
	    Session session = Session.getActiveSession();
	    if (session != null &&
	           (session.isOpened() || session.isClosed()) ) {
	        onSessionStateChange(session, session.getState(), null);
	    }
	    
	    uiHelper.onResume();
	}
	
	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}
	
	public void onDestroy(){
		super.onDestroy();
		
		if ( mBound ){
			this.unbindService(mConnection);
		}
	    uiHelper.onDestroy();
	}
	
	public ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
        	
            LocalBinder binder = (LocalBinder) service;
            gameS = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    
    // Define a DialogFragment that displays the error dialog
    /*
    public static class ErrorDialogFragment extends DialogFragment {
    	
        private Dialog mDialog;
        
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
    */
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
        
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK :
                    /*
                     * Try the request again
                     */
                    break;
                }
        }

	    uiHelper.onActivityResult(requestCode, resultCode, data);
    }
    
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Activity Recognition",
                    "Google Play services is available.");
            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                //ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                //errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
            }
            return false;
        }
    }
    
    public void goRanking( View v ){
    	Uri uri = Uri.parse("http://instagram.com/p/uMp2PkGzHQ/");
    	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    	startActivity(intent);
    	
    	Utils.ga_goRanking((Fitte)getApplication());
    }
    
    public void resetStats( View v ){
    	
    	new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle("Puntos")
        .setMessage("¿Reiniciar conteo de puntos?")
        .setPositiveButton("Sí", new DialogInterface.OnClickListener(){
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            
	        	SharedPreferences prefs;
	            prefs = getSharedPreferences("com.fittegame", Context.MODE_PRIVATE);
	            
	            prefs.edit().putInt(USER_POINTS, 0).commit();         
	            setGame();

	        	Utils.ga_resetPoints((Fitte)getApplication());
	        }
        })
	    .setNegativeButton("No", null)
	    .show();
    	
    }
    
    /* Facebook integration */
    
    public void setUsername( String name ){
    	//http://graph.facebook.com/10203353130442263/picture

    	SharedPreferences prefs;
        prefs = this.getSharedPreferences("com.fittegame", Context.MODE_PRIVATE);
        
        prefs.edit().putString(USER_NAME, name).commit();
    }
    
    public String getUsername( ){
    	SharedPreferences prefs;
        prefs = this.getSharedPreferences("com.fittegame", Context.MODE_PRIVATE);
    	
        return prefs.getString(USER_NAME, "Desconocido");
    }
    
    public void setId( String id ){

    	SharedPreferences prefs;
        prefs = this.getSharedPreferences("com.fittegame", Context.MODE_PRIVATE);
        
        prefs.edit().putString(USER_ID, id).commit();
    }
    
    public String getId(){
    	SharedPreferences prefs;
        prefs = this.getSharedPreferences("com.fittegame", Context.MODE_PRIVATE);
    	
        return prefs.getString(USER_ID, "-1");
    }
    
    public int getPoints(){
    	SharedPreferences prefs;
        prefs = this.getSharedPreferences("com.fittegame", Context.MODE_PRIVATE);
    	
        return prefs.getInt(USER_POINTS, 0);
    }

    public int getEggPoints(){
    	SharedPreferences prefs;
        prefs = this.getSharedPreferences("com.fittegame", Context.MODE_PRIVATE);
    	
        return prefs.getInt(EGG_POINTS, 0);
    }
    
    public void addEggPoint(){
    	
    	if ( getMonsterLevel() != -1 ){
    		return;
    	}
    	
    	SharedPreferences prefs;    	
	    prefs = this.getSharedPreferences("com.fittegame", Context.MODE_PRIVATE);
	    
    	int old_user_points = prefs.getInt(USER_POINTS, 0);
    	int egg_points = prefs.getInt(EGG_POINTS, 0);
	    
    	if ( old_user_points > 0 ){
    		prefs.edit().putInt(USER_POINTS, old_user_points-1).commit();
    		prefs.edit().putInt(EGG_POINTS, egg_points+1).commit();
    		
            ProgressBar progress = (ProgressBar) findViewById(R.id.progress);
            progress.setVisibility(View.INVISIBLE);
            
            if ( egg_points+1 >= MAX_PROGRESS_EGG ){
            	setMonsterLevel(1);
            }

    		ImageView monster = (ImageView) findViewById(R.id.monster);
			final AnimationDrawable monster_anim = (AnimationDrawable) monster.getBackground();
			
			if ( ( !monster_anim.isRunning() && monster_anim.getCurrent() == monster_anim.getFrame(0) ) || monster_anim.getCurrent() == monster_anim.getFrame(monster_anim.getNumberOfFrames() - 1) ){
				monster_anim.stop();
				monster_anim.start();
			}
            
            new Handler().postDelayed(new Runnable(){

				@Override
				public void run() {
		    		setGame(true);
				}
			}, 30);
    	}
    	
    }
    
    /* Metodo que se llama en el onClick de Add */
    public void addEggPoint( View v ){
    	addEggPoint();
    }
    
    public int getEggType(){
    	SharedPreferences prefs;
        prefs = this.getSharedPreferences("com.fittegame", Context.MODE_PRIVATE);
    	
        return prefs.getInt(EGG_TYPE, -1);
    }
    
    public void setEggType( int type ){
    	SharedPreferences prefs;    	
	    prefs = this.getSharedPreferences("com.fittegame", Context.MODE_PRIVATE);
	    
    	prefs.edit().putInt(EGG_TYPE, type).commit();	
    }
    
    public int getMonsterLevel(){
    	SharedPreferences prefs;
        prefs = this.getSharedPreferences("com.fittegame", Context.MODE_PRIVATE);
    	
        return prefs.getInt(MONSTER_LEVEL, -1);
    }
    
    public void setMonsterLevel( int level ){
    	SharedPreferences prefs;    	
	    prefs = this.getSharedPreferences("com.fittegame", Context.MODE_PRIVATE);
	    
    	prefs.edit().putInt(MONSTER_LEVEL, level).commit();	
    }
        
    public void setGame(){
    	setGame(false);
    }
    
    public void setGame( boolean only_points ){
    	
    	LinearLayout welcome_cont = (LinearLayout)findViewById(R.id.welcome_container);
    	LinearLayout logged_cont = (LinearLayout)findViewById(R.id.logged_container);
    	LinearLayout selection = (LinearLayout)findViewById(R.id.egg_selection);
    	RelativeLayout monster_content = (RelativeLayout)findViewById(R.id.monster_content);
    	welcome_cont.setVisibility(View.GONE);
    	logged_cont.setVisibility(View.VISIBLE);
    	
    	final int egg_type = getEggType();
    	
    	if ( egg_type == -1 ){
    		selection.setVisibility(View.VISIBLE);
    		monster_content.setVisibility(View.GONE);
    	}
    	else{
    		selection.setVisibility(View.GONE);
    		monster_content.setVisibility(View.VISIBLE);
    	}
    	
    	selection.setVisibility(View.GONE);
        monster_content.setVisibility(View.GONE);
    	
    	ImageView image = (ImageView) findViewById(R.id.image);
    	TextView name = (TextView) findViewById(R.id.name);
    	TextView points = (TextView) findViewById(R.id.points);
    	
    	if ( !only_points ){
	    	name.setText(getUsername());
	    	setUserImage( this , image , getId() );
    	}
    	
    	points.setText(getPoints()+"");
		ImageView monster = (ImageView) findViewById(R.id.monster);
		int monster_level = getMonsterLevel();
		
    	if ( egg_type != -1 ){
/*
    		TextView ptext = (TextView) findViewById(R.id.progress_text);
    		ptext.setVisibility(View.GONE);*/
    		
    		if ( monster_level != -1 ){
    			
    			switch( egg_type ){
        		
        		case FIRE:
                	monster.setBackgroundResource(R.anim.fire_monster_anim1);
        			break;
        		case WATER:
                	monster.setBackgroundResource(R.anim.water_monster_anim1);
        			break;
        		case EARTH:
                	monster.setBackgroundResource(R.anim.earth_monster_anim1);
        			break;
        		case AIR:
                	monster.setBackgroundResource(R.anim.air_monster_anim1);
        			break;
        		
        		}

    			final AnimationDrawable monster_anim = (AnimationDrawable) monster.getBackground();
    			monster_anim.start();
    			

            	monster.setOnTouchListener(new OnTouchListener(){

    				@Override
    				public boolean onTouch(View arg0, MotionEvent event) {
    					
    					if ( event.getAction() == MotionEvent.ACTION_DOWN ){
    						
    						if ( mp == null){
    							mp = MediaPlayer.create(getApplicationContext(), R.raw.funny);
    							mp.setVolume(0.3f, 0.3f);
    						}
    						
    						mp.start();
    						
    	    		    	monster_anim.stop();
    	    		    	
    	    				ImageView monster = (ImageView) findViewById(R.id.monster);
    	    				
    	    				switch( egg_type ){
    	            		
    	            		case FIRE:
        	                	monster.setBackgroundResource(R.anim.fire_monster_anim2);
    	            			break;
    	            		case WATER:
    	                    	monster.setBackgroundResource(R.anim.water_monster_anim2);
    	            			break;
    	            		case EARTH:
    	                    	monster.setBackgroundResource(R.anim.earth_monster_anim2);
    	            			break;
    	            		case AIR:
    	                    	monster.setBackgroundResource(R.anim.air_monster_anim2);
    	            			break;
    	            		
    	            		}
    	    				

    	        			AnimationDrawable monster_anim = (AnimationDrawable) monster.getBackground();
    	                	monster_anim = (AnimationDrawable) monster.getBackground();
    	    		    	monster_anim.start();
    	    		    	
    	    		    	checkIfAnimationDone(monster_anim,new Runnable(){

								@Override
								public void run() {
									setGame(true);
									/*ImageView monster = (ImageView) findViewById(R.id.monster);
									
					    			switch( egg_type ){
					        		
					        		case FIRE:
					                	monster.setBackgroundResource(R.anim.fire_monster_anim1);
					        			break;
					        		case WATER:
					                	monster.setBackgroundResource(R.anim.water_monster_anim1);
					        			break;
					        		case EARTH:
					                	monster.setBackgroundResource(R.anim.earth_monster_anim1);
					        			break;
					        		case AIR:
					                	monster.setBackgroundResource(R.anim.air_monster_anim1);
					        			break;
					        		
					        		}

		    	        			AnimationDrawable monster_anim = (AnimationDrawable) monster.getBackground();
		    	    		    	monster_anim.start();*/
								}

							});
    	    		    	
    	    		    	
    	    		    	return true;
    					}
    					
    					return false;
    				}
    			});
    		
    		}
    		else{
    			switch( egg_type ){
        		
        		case FIRE:
                	monster.setBackgroundResource(R.anim.fire_egg_anim);
        			break;
        		case WATER:
                	monster.setBackgroundResource(R.anim.water_egg_anim);
        			break;
        		case EARTH:
                	monster.setBackgroundResource(R.anim.earth_egg_anim);
        			break;
        		case AIR:
                	monster.setBackgroundResource(R.anim.air_egg_anim);
        			break;
        		
        		}
    			final AnimationDrawable monster_anim = (AnimationDrawable) monster.getBackground();
            	
            	monster.setOnTouchListener(new OnTouchListener(){

    				@Override
    				public boolean onTouch(View arg0, MotionEvent event) {
    					
    					if ( event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE ){
    						
    	    		    	monster_anim.stop();
    	    		    	monster_anim.start();
    	    		    	
    	    		    	return true;
    					}
    					
    					return false;
    				}
    			});
    		}
    		
        	/*monster.setOnClickListener(new OnClickListener(){
        		
    			@Override
    			public void onClick(View arg0) {
    				
    		    	monster_anim.stop();
    		    	monster_anim.start();
    		    	//checkIfAnimationDone(monster_anim);
    			}
    		});*/
        	
            ProgressBar progress = (ProgressBar) findViewById(R.id.progress);
            
            if ( monster_level == -1 ){
                progress.setVisibility(View.VISIBLE);
                progress.setMax(MAX_PROGRESS_EGG);
                progress.setProgress( getEggPoints() );
                
                ImageButton add_b = (ImageButton) findViewById(R.id.add_button);
                
                add_b.setOnLongClickListener( 
	                new View.OnLongClickListener(){
	                    public boolean onLongClick(View arg0) {
	                        mAutoIncrement = true;
	                        repeatUpdateHandler.post( new RptUpdater() );
	                        return false;
	                    }
	                }
		        );   
	
	            add_b.setOnTouchListener( new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        if( (event.getAction()==MotionEvent.ACTION_UP || event.getAction()==MotionEvent.ACTION_CANCEL) 
                                && mAutoIncrement ){
                            mAutoIncrement = false;
                        }
                        else if ( event.getAction() == MotionEvent.ACTION_DOWN ){
                        	addEggPoint();
                        }
                        return false;
                    }
                }); 
                
            }
            else{
            	progress.setVisibility(View.GONE);
            }
            
    	}
    	
    }
    
    class RptUpdater implements Runnable {
        public void run() {
        	
            if( mAutoIncrement ){
            	addEggPoint();
                repeatUpdateHandler.postDelayed( new RptUpdater(), REP_DELAY );
            }
        }
    }
    
    /* Egg selection */

    public void selectFire( View v ){
    	setEggType( FIRE );
    	setGame();
    }
    
    public void selectWater( View v ){
    	setEggType( WATER );
    	setGame();
    }
    
    public void selectEarth( View v ){
    	setEggType( EARTH );
    	setGame();
    }
    
    public void selectAir( View v ){
    	setEggType( AIR );
    	setGame();
    }
    
    private void checkIfAnimationDone(AnimationDrawable anim , final Runnable r ){
        final AnimationDrawable a = anim;
        int timeBetweenChecks = 300;
        Handler h = new Handler();
        h.postDelayed(new Runnable(){
            public void run(){
                if (a.getCurrent() != a.getFrame(a.getNumberOfFrames() - 1)){
                    checkIfAnimationDone(a,r);
                } else{
                    r.run();
                }
            }
        }, timeBetweenChecks);
    };
    
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    
		if (state.isOpened()) {
	        Log.i(TAG, "Logged in...");
	        
	        // Si no estoy logeado
	        if ( getId().equals("-1")  ){
	        	
	        	if ( dialog == null ){
	        		dialog = ProgressDialog.show(this, "Fitte","Configurando...", true);
	        	}
				
		        Request.newMeRequest(session, new Request.GraphUserCallback() {
					
					@Override
					public void onCompleted(GraphUser user, Response response) {
						
						if ( fb_info_ready )
							return;
						
						String name = user.getFirstName() + " " + user.getLastName();
	                    String id = user.getId();
	                    
	                    Log.i("facebookid", id);
	                    Log.i("Name", name);
	
	                    setId(id);
	                    setUsername(name);
	                    
	                    if ( dialog != null && dialog.isShowing() ){
	                    	dialog.dismiss();
	                    }
	                    
	                    new InsertUser( getApplicationContext() , id ,name ).execute();
	                    
	    	        	setGame();
	                    
	                    fb_info_ready = true;
					}
				}).executeAsync();
	        }
	        else{
	        	
	        	setGame();
	        	
	        }
	        
	    } else if (state.isClosed()) {
	        Log.i(TAG, "Logged out...");
	    }
	}
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
	class InsertUser extends ServerConnection{
		
		public InsertUser( Context c , String id , String name ){
			super();
			
			String image = "http://graph.facebook.com/"+id+"/picture";
			init(c, "insert_user" , new String[]{ id , name , image } );			
		}
		
		@Override
		public void onComplete(String result) {
			Log.i("Server", "Listo");
		}
		
	}
	
	public static int dpToPx(float dp, Context context){
		
		if ( context != null){
			Resources resources = context.getResources();
		    DisplayMetrics metrics = resources.getDisplayMetrics();
		    float px = dp * (metrics.densityDpi / 160f);
		    return (int)px;
		}
	    
		return 0;
	}
	
	public ImageLoader getImageLoader( Context con ){
		
		if ( imageLoader == null ){
			imageLoader = ImageLoader.getInstance();
			imageLoader.init(ImageLoaderConfiguration.createDefault(con));
				        
	        com.nostra13.universalimageloader.utils.L.disableLogging();
		}
		
		return imageLoader;
	}

	public void setUserImage( Context con , ImageView iv , String id_user ){
		
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.user)
		.cacheOnDisk(true)
        //.showStubImage(R.drawable.user)
        .showImageForEmptyUri(R.drawable.user)
        .showImageOnFail(R.drawable.user)
        //.cacheOnDisc()
        .displayer(new RoundedBitmapDisplayer(dpToPx(80, con)))
        .build();
		
		imageLoader = getImageLoader(con);
		
		String image = "http://graph.facebook.com/"+id_user+"/picture?height=200&width=200";
		imageLoader.displayImage( image , iv, options );
	} 
    
}

	