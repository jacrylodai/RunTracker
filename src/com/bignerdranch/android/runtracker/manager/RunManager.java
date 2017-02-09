package com.bignerdranch.android.runtracker.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.bignerdranch.android.runtracker.R;
import com.bignerdranch.android.runtracker.db.RunDatabaseHelper;
import com.bignerdranch.android.runtracker.db.RunDatabaseHelper.LocationDataCursor;
import com.bignerdranch.android.runtracker.db.RunDatabaseHelper.RunCursor;
import com.bignerdranch.android.runtracker.domain.LocationData;
import com.bignerdranch.android.runtracker.domain.Run;
import com.bignerdranch.android.runtracker.fragment.ConfigFragment;
import com.bignerdranch.android.runtracker.utils.date.DateUtils;
import com.bignerdranch.android.runtracker.utils.location.LocationUtils;

public class RunManager {
	
	private static final String TAG = RunManager.class.getSimpleName();
	
	public static final String ACTION_LOCATION = 
			"com.bignerdranch.android.runtracker.ACTION_LOCATION";
	
	//地理位置更新的最小间距
	private static final int MIN_DISTANCE = 0;

	//统计旅程记录点之间的最小间距
	public static final double MIN_TRIP_DISTANCE = 50;
	
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
		
		if(isTrackingRun() == true){
			Log.e(TAG, "software is tracking run,Can't start a new run.");			
			return null;
		}
		//初始化一个旅程
		Run run = new Run();
		run.setRunId(-1);
		run.setRunState(Run.STATE_CURRENT_TRACKING);
		
		Date startDate = new Date();
		String startDateStr = DateUtils.formatFullDate(mAppContext, startDate);
		String runName = mAppContext.getString(R.string.run_desc, startDateStr);
		
		run.setRunName(runName);
		run.setTotalMetre(0);
		run.setElapsedTime(0);
		run.setTotalTripPoint(0);
		run.setStartDate(startDate);
		
		run.setRunId(mDatabaseHelper.insertRun(run));
		
		mPref.edit()
			.putLong(PREF_CURRENT_RUN_ID, run.getRunId())
			.commit();
		
		startLocationUpdates();
		
		return run;
	}
		
	public boolean stopRun(){

		if(isTrackingRun() == false){
			Log.e(TAG, "There is no tracking run,can't stop.");
			return false;
		}
		//停止对GPS数据的接收
		stopLocationUpdates();
		
		//更新run 的状态
		long runId = mPref.getLong(PREF_CURRENT_RUN_ID, -1);
		Run run = queryRunById(runId);
		
		run.setRunState(Run.STATE_NORMAL);
		
		LocationDataCursor locationDataCursor = queryLocationDataListByRunId(runId);
		List<LatLng> pointList = new ArrayList<LatLng>();
		
		locationDataCursor.moveToFirst();
		while(!locationDataCursor.isAfterLast()){
			LocationData locationData = locationDataCursor.getLocationData();
			
			LatLng sourceLatLng = new LatLng(locationData.getLatitude()
					, locationData.getLongitude());
			
			LatLng destLatLng = LocationUtils.convertGPSToBaiduPoint(sourceLatLng);
			
			pointList.add(destLatLng);

			locationDataCursor.moveToNext();
		}
		
		//如果有最后一个节点就移到最后一个节点，取得elapsedTime
		if(locationDataCursor.moveToLast()){
			LocationData locationData = locationDataCursor.getLocationData();
			Date timestamp = locationData.getTimestamp();
			
			Date startDate = run.getStartDate();
			long elapsedTime = timestamp.getTime() - startDate.getTime();
			
			run.setElapsedTime(elapsedTime);
		}
		
		locationDataCursor.close();
		
		//如果记录的节点为0，或为1，就删除当前记录的地理数据及旅程，数据无效
		if(pointList.size() == 0){
			Toast.makeText(mAppContext,R.string.cant_show_total_metre_no_location_data
					,Toast.LENGTH_LONG).show();
			deleteRunById(run.getRunId());
			return false;
		}else
			if(pointList.size() == 1){

				Toast.makeText(mAppContext,R.string.cant_show_total_metre_need_more_location_data
						,Toast.LENGTH_LONG).show();
				deleteRunById(run.getRunId());
				return false;
			}
		
		//设置总记录节点数
		run.setTotalTripPoint(pointList.size());

		//去除重复的节点得到的最终旅程点
		List<LatLng> finalPointList = LocationUtils.getFinalTripPoint(pointList);
		
		//开始统计总里程
		double totalDistance = LocationUtils.caculateTotalDistance(finalPointList);
		Log.i(TAG, "total distance:"+totalDistance);
		
		long totalMetre = (long)totalDistance;
		run.setTotalMetre(totalMetre);
		
		updateRun(run);		
		
		mPref.edit()
			.remove(PREF_CURRENT_RUN_ID)
			.commit();	
		
		return true;
	}
	
	public void updateRun(Run run) {

		mDatabaseHelper.updateRun(run);
	}

	public void deleteRunById(long runId) {

		long affectedRow = mDatabaseHelper.deleteLocationDataByRunId(runId);
		Log.d(TAG, "deleteLocationDataListByRunId--affected row:"+affectedRow);
		
		mDatabaseHelper.deleteRunById(runId);
	}
	
	/**
	 * 获取最佳服务对象,只使用GPS
	 * @return
	 */
	public String getLocationProvider(){
		
//		Criteria criteria = new Criteria();
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);//高精度
//        criteria.setAltitudeRequired(false);//无海拔要求
//        criteria.setBearingRequired(false);//无方位要求
//        criteria.setCostAllowed(true);//允许产生资费
//        criteria.setPowerRequirement(Criteria.POWER_LOW);//低功耗
//      
//        String provider = mLocationManager.getBestProvider(criteria,true);        
//		Log.i(TAG, "provider:"+provider);
//		
//		if(provider.equals(LocationManager.PASSIVE_PROVIDER)){
//			return null;
//		}
		
        return LocationManager.GPS_PROVIDER;
	}

	public void startLocationUpdates(){
		
		String provider = getLocationProvider();
        boolean isProviderEnabled = mLocationManager.isProviderEnabled(provider);
        if(isProviderEnabled == false){
        	Toast.makeText(mAppContext, R.string.cant_start_tracking_gps_not_enabled
        			, Toast.LENGTH_LONG).show();
        	return;
        }
        
//        Toast.makeText(mAppContext, "Using location provider:"+provider
//        		, Toast.LENGTH_SHORT).show();
				
		PendingIntent intent = getLocationPendingIntent(true);
		
		int recordTime = mPref.getInt(ConfigFragment.PREF_RECORD_TIME
				, ConfigFragment.DEFAULT_RECORD_TIME);
		int recordTimeMill = recordTime*1000;
		mLocationManager.requestLocationUpdates(provider, recordTimeMill, MIN_DISTANCE, intent);
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
			mDatabaseHelper.insertLocationData(locationData);
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
	
	public boolean checkIsProviderEnabled(String provider){

		if(provider == null){
			return false;
		}
		return mLocationManager.isProviderEnabled(provider);
	}
	
}
