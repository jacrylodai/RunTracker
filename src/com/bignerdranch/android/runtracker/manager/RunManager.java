package com.bignerdranch.android.runtracker.manager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

public class RunManager {
	
	private static final String TAG = "RunManager";
	
	public static final String ACTION_LOCATION = 
			"com.bignerdranch.android.runtracker.ACTION_LOCATION";
	
	private static final int MIN_TIME = 1*1000;
	
	private static final int MIN_DISTANCE = 2;

	private static RunManager sInstance;
	
	private Context mAppContext;
	
	private LocationManager mLocationManager;
	
	private RunManager(Context appContext){
		
		mAppContext = appContext;
		mLocationManager = 
				(LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
	}
	
	public static RunManager getInstance(Context context){
		
		if(sInstance == null){
			sInstance = new RunManager(context.getApplicationContext());
		}
		return sInstance;
	}
	
	public void startLocationUpdates(){
		
		String provider = LocationManager.GPS_PROVIDER;
		
		Location lastLocation = mLocationManager.getLastKnownLocation(provider);
		if(lastLocation != null){
			lastLocation.setTime(System.currentTimeMillis());
			broadcastLocation(lastLocation);
		}
		
		PendingIntent intent = getLocationPendingIntent(true);
		mLocationManager.requestLocationUpdates(provider, MIN_TIME, MIN_DISTANCE, intent);
	}

	private void broadcastLocation(Location lastLocation) {

		Intent intent = new Intent(ACTION_LOCATION);
		intent.putExtra(LocationManager.KEY_LOCATION_CHANGED, lastLocation);
		mAppContext.sendBroadcast(intent);
	}

	private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
		
		int flags = shouldCreate ? 0:PendingIntent.FLAG_NO_CREATE;
		Intent intent = new Intent(ACTION_LOCATION);
		PendingIntent pendingIntent = 
				PendingIntent.getBroadcast(mAppContext, 0, intent, flags);
		return pendingIntent;
	}
	
	public boolean isTrackingRun(){
		
		PendingIntent pendingIntent = getLocationPendingIntent(false);
		boolean running = false;
		if(pendingIntent != null){
			running = true;
		}else{
			running = false;
		}
		return running;
	}
	
	public void stopLocationUpdates(){
		
		PendingIntent pendingIntent = getLocationPendingIntent(false);
		if(pendingIntent != null){
			mLocationManager.removeUpdates(pendingIntent);
			pendingIntent.cancel();
		}
	}
	
}
