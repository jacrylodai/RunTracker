package com.bignerdranch.android.runtracker.fragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.bignerdranch.android.runtracker.R;
import com.bignerdranch.android.runtracker.db.RunDatabaseHelper.LocationDataCursor;
import com.bignerdranch.android.runtracker.domain.LocationData;
import com.bignerdranch.android.runtracker.loader.LocationDataListLoader;

public class RunMapFragment extends SupportMapFragment{

	private static final String TAG = RunMapFragment.class.getSimpleName();
	
	private static final long FREQ_ORIENTATION_UPDATE = 60;
	
	private static final int SENSOR_FREQ_RATE = SensorManager.SENSOR_DELAY_UI;
	
	private static final String ARG_RUN_ID = "runId";
	
	private static final int LOADER_LOAD_LOCATION_DATA_LIST = 1;
	
	private BaiduMap mBaiduMap;
	
	private LocationDataCursor mLocationDataCursor;
	
	private LocationClient mLocationClient;
	
	private MyLocationListener mMyLocationListener;
	
	private boolean hasLocateToMyLocation,isTrackingMyLocation;
	
	private BDLocation mMyBDLocation;

	private SensorManager mSensorManager;
	
	private Sensor mAcceSensor,mMagnSensor;
	
	private OrientationSensorListener mOriSensorListener;
	
	private double mZDegree;

	private BitmapDescriptor mCustomMarker;
	
	private LoaderCallbacks<Cursor> mLocationDataListLoaderCallbacks = 
			new LoaderCallbacks<Cursor>() {

				@Override
				public Loader<Cursor> onCreateLoader(int id, Bundle args) {
					
					long runId = args.getLong(ARG_RUN_ID, -1);
					return new LocationDataListLoader(getActivity(), runId);
				}

				@Override
				public void onLoadFinished(Loader<Cursor> loader,
						Cursor cursor) {
					mLocationDataCursor = (LocationDataCursor) cursor;
					updateMap();
				}

				@Override
				public void onLoaderReset(Loader<Cursor> loader) {
					mLocationDataCursor.close();
					mLocationDataCursor = null;
				}
			};
	
	public static RunMapFragment newInstance(long runId){
		
		Bundle args = new Bundle();
		args.putLong(ARG_RUN_ID, runId);
		
		RunMapFragment runMapFragment = new RunMapFragment();
		runMapFragment.setArguments(args);
		return runMapFragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getActivity().getApplicationContext());
		
		setHasOptionsMenu(true);
		
		isTrackingMyLocation = false;
		initialMap();
		initialMyLocation();
		initialSensor();
		
		Bundle args = getArguments();
		long runId = args.getLong(ARG_RUN_ID, -1);
		if(runId != -1){
			getLoaderManager().initLoader(LOADER_LOAD_LOCATION_DATA_LIST, args
					, mLocationDataListLoaderCallbacks);
		}else{
			Log.e(TAG, "runId have no value");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent
			, Bundle savedInstanceState) {
		
		View view = super.onCreateView(inflater, parent, savedInstanceState);
		mBaiduMap = getBaiduMap();
		return view;
	}
	
	@Override
	public void onStart() {
		
		super.onStart();
		if(isTrackingMyLocation){
			mBaiduMap.setMyLocationEnabled(true);
			mLocationClient.registerLocationListener(mMyLocationListener);
			mLocationClient.start();

			if(mSensorManager != null && mAcceSensor != null && mMagnSensor != null){
				
				mOriSensorListener = new OrientationSensorListener();
				
		        mSensorManager.registerListener(mOriSensorListener, mAcceSensor
		        		, SENSOR_FREQ_RATE);
		        mSensorManager.registerListener(mOriSensorListener, mMagnSensor
		        		, SENSOR_FREQ_RATE);
			}
		}
	}
	
	@Override
	public void onStop() {
		
		if(isTrackingMyLocation){

			if(mSensorManager != null && mAcceSensor != null && mMagnSensor != null){
				mSensorManager.unregisterListener(mOriSensorListener);
			}
			
			mLocationClient.stop();
			mLocationClient.unRegisterLocationListener(mMyLocationListener);
			mBaiduMap.setMyLocationEnabled(false);
		}
		super.onStop();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.run_map_options, menu);
	}	
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		
		case R.id.menu_item_normal_map:
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
			return true;
			
		case R.id.menu_item_satellite_map:
			mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
			return true;
		
		case R.id.menu_item_show_my_location:
			
			if(isTrackingMyLocation){
				isTrackingMyLocation = false;
				doNotShowMyLocation();
			}else{
				isTrackingMyLocation = true;
				showMyLocation();
			}
			
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
				getActivity().invalidateOptionsMenu();
			}
			return true;

		case R.id.menu_item_location_mode_normal:
			
			updateMapLocationMode(LocationMode.NORMAL);
			return true;

		case R.id.menu_item_location_mode_following:
			
			updateMapLocationMode(LocationMode.FOLLOWING);
			return true;

		case R.id.menu_item_location_mode_compass:
			
			updateMapLocationMode(LocationMode.COMPASS);
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		MenuItem showMyLocationMenuItem = menu.findItem(R.id.menu_item_show_my_location);
		if(isTrackingMyLocation){
			showMyLocationMenuItem.setTitle(R.string.do_not_show_my_location);
		}else{
			showMyLocationMenuItem.setTitle(R.string.show_my_location);
		}
		
		MenuItem locationModeNormal = menu.findItem(R.id.menu_item_location_mode_normal);
		MenuItem locationModeFollowing = menu.findItem(R.id.menu_item_location_mode_following);
		MenuItem locationModeCompass = menu.findItem(R.id.menu_item_location_mode_compass);
		if(isTrackingMyLocation){
			locationModeNormal.setEnabled(true);
			locationModeFollowing.setEnabled(true);
			locationModeCompass.setEnabled(true);
		}else{
			locationModeNormal.setEnabled(false);
			locationModeFollowing.setEnabled(false);
			locationModeCompass.setEnabled(false);
		}
	}
	
	private void initialMap(){

		mCustomMarker = BitmapDescriptorFactory
				.fromResource(R.drawable.navi_map_gps_locked);

	}
	
	private void initialMyLocation() {
		
		mLocationClient = new LocationClient(getActivity());
		mMyLocationListener = new MyLocationListener();
		
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setCoorType("bd09ll");
		option.setScanSpan(2*1000);
		
		mLocationClient.setLocOption(option);
	}

	private void initialSensor() {
		
		mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
		
		if(mSensorManager != null){

	        Sensor acceSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	        if(acceSensor != null){
	        	mAcceSensor = acceSensor;
	        }
	        Sensor magnSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	        if(magnSensor != null){
	        	mMagnSensor = magnSensor;
	        }
	        
	        if(mAcceSensor == null || mMagnSensor == null){
	        	
	        	Toast.makeText(getActivity(), R.string.cant_get_sensor, Toast.LENGTH_LONG).show();
	        }
		}else{
			
			Toast.makeText(getActivity(), R.string.cant_get_sensor, Toast.LENGTH_LONG).show();
		}
	}

	private void showMyLocation(){
		
		hasLocateToMyLocation = false;		
		
		updateMapLocationMode(LocationMode.NORMAL);
		
		mBaiduMap.setMyLocationEnabled(true);
		mLocationClient.registerLocationListener(mMyLocationListener);
		mLocationClient.start();

		if(mSensorManager != null && mAcceSensor != null && mMagnSensor != null){
			
			mOriSensorListener = new OrientationSensorListener();
			
	        mSensorManager.registerListener(mOriSensorListener, mAcceSensor
	        		, SENSOR_FREQ_RATE);
	        mSensorManager.registerListener(mOriSensorListener, mMagnSensor
	        		, SENSOR_FREQ_RATE);
		}
	}
	
	private void doNotShowMyLocation(){
		
		if(mSensorManager != null && mAcceSensor != null && mMagnSensor != null){
			mSensorManager.unregisterListener(mOriSensorListener);
		}
		
		mLocationClient.stop();
		mLocationClient.unRegisterLocationListener(mMyLocationListener);
		mBaiduMap.setMyLocationEnabled(false);
		
	}
	
	private void locateToPosition(LatLng latLng){
		
		MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
		mBaiduMap.animateMapStatus(update);
	}
	
	/**
	 * 更新我的位置
	 * 因为使用了方向传感器，需要频繁更新我的位置
	 */
	private void updateMyLocation(){
		
		if(mMyBDLocation != null){
			MyLocationData myLocationData = 
					new MyLocationData.Builder()
						.accuracy(mMyBDLocation.getRadius())
						.direction((float)mZDegree)
						.latitude(mMyBDLocation.getLatitude())
						.longitude(mMyBDLocation.getLongitude())
						.build();
			
			mBaiduMap.setMyLocationData(myLocationData);
		}
	}
	
	private void updateMapLocationMode(LocationMode locationMode){
		
		MyLocationConfiguration configuration = 
				new MyLocationConfiguration(locationMode, true, mCustomMarker);
		mBaiduMap.setMyLocationConfigeration(configuration);
	}
	
	private void updateMap(){

		List<LatLng> pointList = new ArrayList<LatLng>();
		
		LatLng firstLatLng = null;
		
		mLocationDataCursor.moveToFirst();
		while(!mLocationDataCursor.isAfterLast()){
			LocationData locationData = mLocationDataCursor.getLocationData();
			
			LatLng sourceLatLng = new LatLng(locationData.getLatitude()
					, locationData.getLongitude());
			
			// 将GPS设备采集的原始GPS坐标转换成百度坐标  
			CoordinateConverter converter  = new CoordinateConverter();  
			converter.from(CoordType.GPS);  
			// sourceLatLng待转换坐标  
			converter.coord(sourceLatLng);  
			LatLng desLatLng = converter.convert();
			
			if(firstLatLng == null){
				firstLatLng = desLatLng;
			}
			
			pointList.add(desLatLng);
			mLocationDataCursor.moveToNext();
		}
		
		OverlayOptions ooPolyline = new PolylineOptions().width(6)
				.color(0xFF41A6F0).points(pointList);
		mBaiduMap.addOverlay(ooPolyline);
		
		if(firstLatLng != null){
			MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(firstLatLng);
			mBaiduMap.animateMapStatus(mapStatusUpdate);
			
			mapStatusUpdate = MapStatusUpdateFactory.zoomTo(14f);
			mBaiduMap.animateMapStatus(mapStatusUpdate);
		}else{
			Toast.makeText(getActivity(), R.string.no_location_data, Toast.LENGTH_SHORT).show();
		}
	}
	
	private class MyLocationListener implements BDLocationListener{

		@Override
		public void onReceiveLocation(BDLocation bdLocation) {

			if(hasLocateToMyLocation == false){
				hasLocateToMyLocation = true;
				LatLng latLng = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
				locateToPosition(latLng);
			}

			mMyBDLocation = bdLocation;
			
			updateMyLocation();
		}

		@Override
		public void onReceivePoi(BDLocation arg0) {
			
		}
		
	}

	private class OrientationSensorListener implements SensorEventListener{

		private float[] acceValues,magnValues;
		
		private long lastMagnTime = System.currentTimeMillis();
		
		private long lastRecordTime = System.currentTimeMillis();
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			
			if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
				acceValues = event.values.clone();
			}else
				if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
					magnValues = event.values.clone();		
					
					long currentMagnTime = System.currentTimeMillis();
					long intervalMagnTime = currentMagnTime-lastMagnTime;
					Log.d(TAG, "magn sensor freq:(ms)"+intervalMagnTime);
					
					lastMagnTime = currentMagnTime;
				}
					
			if(acceValues != null && magnValues != null){
	    		
	    		float[] rValues = new float[9];
	    		SensorManager.getRotationMatrix(rValues, null, acceValues, magnValues);
	    		
	    		float[] orieValues = new float[3];
	    		SensorManager.getOrientation(rValues, orieValues);
	    			    		
	    		double zDegree = Math.toDegrees(orieValues[0]);
	    		
	    		//每间隔一段时间更新一次地图上的方向
	    		long currentTime = System.currentTimeMillis();	    		
	    		if(currentTime - lastRecordTime > FREQ_ORIENTATION_UPDATE){
	    			mZDegree = zDegree;
	    			updateMyLocation();
	    			lastRecordTime = currentTime;
	    		}
	    	}
	    }

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			
		}
		
	}
	
}
