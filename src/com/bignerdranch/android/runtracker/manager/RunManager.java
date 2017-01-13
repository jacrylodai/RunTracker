package com.bignerdranch.android.runtracker.manager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.bignerdranch.android.runtracker.db.RunDatabaseHelper;
import com.bignerdranch.android.runtracker.db.RunDatabaseHelper.LocationDataCursor;
import com.bignerdranch.android.runtracker.db.RunDatabaseHelper.RunCursor;
import com.bignerdranch.android.runtracker.domain.LocationData;
import com.bignerdranch.android.runtracker.domain.Run;

public class RunManager {
	
	private static final String TAG = "RunManager";
	
	public static final String ACTION_LOCATION = 
			"com.bignerdranch.android.runtracker.ACTION_LOCATION";
	
	private static final String TEST_PROVIDER = "TEST_PROVIDER";
	
	private static final int MIN_TIME = 1*1000;
	
	private static final int MIN_DISTANCE = 2;
	
	private static final String PREF_CURRENT_RUN_ID = "currentRunId";

	private static RunManager sInstance;
	
	private Context mAppContext;
	
	private LocationManager mLocationManager;
	
	private RunDatabaseHelper mDatabaseHelper;
	
	private SharedPreferences mPref;
	
	private RunManager(Context appContext){
		
		mAppContext = appContext;
		mLocationManager = 
				(LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
		mDatabaseHelper = new RunDatabaseHelper(appContext);
		mPref = PreferenceManager.getDefaultSharedPreferences(appContext);
	}
	
	public static RunManager getInstance(Context context){
		
		if(sInstance == null){
			sInstance = new RunManager(context.getApplicationContext());
		}
		return sInstance;
	}
	
	public Run startNewRun(){
		
		Run run = new Run();
		run.setRunId(mDatabaseHelper.insertRun(run));
		
		mPref.edit()
			.putLong(PREF_CURRENT_RUN_ID, run.getRunId())
			.commit();
		
		startLocationUpdates();
		
		return run;
	}
	
	public void startCurrentRun(Run run){
		
		mPref.edit()
			.putLong(PREF_CURRENT_RUN_ID, run.getRunId())
			.commit();
		
		startLocationUpdates();		
	}
	
	public void stopRun(){

		stopLocationUpdates();
		
		mPref.edit()
			.remove(PREF_CURRENT_RUN_ID)
			.commit();		
	}
	
	public void startLocationUpdates(){
		
		String provider = LocationManager.GPS_PROVIDER;
		
		//if you have the test provider and it is enabled.use it.
		if(mLocationManager.getProvider(TEST_PROVIDER)!= null &&
				mLocationManager.isProviderEnabled(TEST_PROVIDER)){
			provider = TEST_PROVIDER;
		}
		
		Log.i(TAG, "using gps:"+provider);
		Toast.makeText(mAppContext, "using gps:"+provider, Toast.LENGTH_LONG).show();
		
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
	
	public void insertLocationData(LocationData locationData){
		
		long currentRunId = mPref.getLong(PREF_CURRENT_RUN_ID, -1);
		if(currentRunId != -1){
			locationData.setFKRunId(currentRunId);
			mDatabaseHelper.insertLocation(locationData);
		}else{
			Log.e(TAG, "Location received with no tracking run: ignore");
		}
	}
	
	public RunCursor queryRunList(){
		return mDatabaseHelper.queryRunList();
	}
	
	public Run queryRunById(long runId){
		
		Run run = null;
		RunCursor runCursor = mDatabaseHelper.queryRunById(runId);
		if(runCursor.moveToNext()){
			run = runCursor.getRun();
		}
		runCursor.close();
		return run;
	}
	
	public long getCurrentTrackingRunId(){
		if(isTrackingRun()){
			return mPref.getLong(PREF_CURRENT_RUN_ID, -1);
		}else{
			return -1;
		}
	}
	
	public LocationData queryLatestLocationDataByRunId(long runId){
		
		LocationData locationData = null;
		LocationDataCursor locaDataCursor = 
				mDatabaseHelper.queryLatestLocationDataByRunId(runId);
		if(locaDataCursor.moveToNext()){
			locationData = locaDataCursor.getLocationData();
		}
		locaDataCursor.close();
		return locationData;
	}
	
	public LocationDataCursor queryLocationDataListByRunId(long runId){
		
		return mDatabaseHelper.queryLocationDataListByRunId(runId);
	}
	
}
