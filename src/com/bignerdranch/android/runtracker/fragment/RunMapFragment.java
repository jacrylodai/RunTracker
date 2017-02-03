package com.bignerdranch.android.runtracker.fragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bignerdranch.android.runtracker.R;
import com.bignerdranch.android.runtracker.db.RunDatabaseHelper.LocationDataCursor;
import com.bignerdranch.android.runtracker.domain.LocationData;
import com.bignerdranch.android.runtracker.loader.LocationDataListLoader;

public class RunMapFragment extends SupportMapFragment{

	private static final String TAG = RunMapFragment.class.getSimpleName();
	
	//���������µ�Ƶ�ʣ�����
	private static final long FREQ_ORIENTATION_UPDATE = 60;
	
	private static final int SENSOR_FREQ_RATE = SensorManager.SENSOR_DELAY_UI;
	
	private static final String ARG_RUN_ID = "runId";
	
	private static final String ARG_MARKER_INFO = "markerInfo";
	
	private static final int LOADER_LOAD_LOCATION_DATA_LIST = 1;
	
	//Ĭ�ϵĵ�ͼ�Ŵ󼶱�
	private static final float DEFAULT_MAP_ZOOM_LEVEL = 17f;
	
	//Ĭ�ϵĵ�ͼ���¶���ʱ�䣺����
	private static final int DEFAULT_MAP_UPDATE_ANIMATION_TIME = 600;
	
	//�ó̼�¼��֮�����С���
	private static final double MIN_TRIP_DISTANCE = 40;
	
	private BaiduMap mBaiduMap;
	
	private LocationDataCursor mLocationDataCursor;
	
	private LocationClient mLocationClient;
	
	private MyLocationListener mMyLocationListener;
	
	//�Ƿ�λ���ҵ�λ��
	private boolean hasLocateToMyLocation;
	
	//�Ƿ����ڸ����ҵ�λ��
	private boolean isTrackingMyLocation;
	
	private BDLocation mMyBDLocation;

	private SensorManager mSensorManager;
	
	//���ٶȴ��������شŴ����������߽�ϼ����ֻ�����ת�Ƕ�
	private Sensor mAcceSensor,mMagnSensor;
	
	private OrientationSensorListener mOriSensorListener;
	
	//�ֻ���Z���ϵ���ת�Ƕ�
	private double mZDegree;

	private BitmapDescriptor mCustomMarker,mPointMarker,mStartPointMarker,mEndPointMarker;
	
	//ȥ���ظ��Ľڵ�õ��������ó̵�
	private List<LatLng> mFinalPointList;
	
	private Handler mHandler = new Handler();
	
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
		initialResource();
		initialMyLocation();
		initialSensor();
				
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent
			, Bundle savedInstanceState) {
		
		View view = super.onCreateView(inflater, parent, savedInstanceState);
		mBaiduMap = getBaiduMap();
		
		initialMap();
		
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
	public void onDestroy() {
		
		mCustomMarker.recycle();
		mPointMarker.recycle();
		mStartPointMarker.recycle();
		mEndPointMarker.recycle();
		
		super.onDestroy();
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
		
		case R.id.menu_item_locate_to_my_location:
			
			if(mMyBDLocation != null){
				LatLng myLL = new LatLng(mMyBDLocation.getLatitude(), mMyBDLocation.getLongitude());
				locateToPositionAndZoomTo(myLL, DEFAULT_MAP_ZOOM_LEVEL);
			}
			return true;
			
		case R.id.menu_item_show_all_trip_point:
			showAllTripPoint();
			return true;
		
		case R.id.menu_item_locate_to_start_point:
			locateToPositionAndZoomTo(mFinalPointList.get(0), DEFAULT_MAP_ZOOM_LEVEL);
			return true;
			
		case R.id.menu_item_locate_to_dest_point:
			int lastIndex = mFinalPointList.size()-1;
			locateToPositionAndZoomTo(mFinalPointList.get(lastIndex), DEFAULT_MAP_ZOOM_LEVEL);
			return true;
		
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
		
		MenuItem locateToMyLocation = menu.findItem(R.id.menu_item_locate_to_my_location);
		
		if(isTrackingMyLocation){
			locationModeNormal.setEnabled(true);
			locationModeFollowing.setEnabled(true);
			locationModeCompass.setEnabled(true);
			
			locateToMyLocation.setEnabled(true);
		}else{
			locationModeNormal.setEnabled(false);
			locationModeFollowing.setEnabled(false);
			locationModeCompass.setEnabled(false);
			
			locateToMyLocation.setEnabled(false);
		}
	}
	
	/**
	 * ��ʼ����Դ
	 */
	private void initialResource(){

		mCustomMarker = BitmapDescriptorFactory
				.fromResource(R.drawable.navi_map_gps_locked);
		mPointMarker = BitmapDescriptorFactory
				.fromResource(R.drawable.trip_marker);
		mStartPointMarker = BitmapDescriptorFactory.fromResource(R.drawable.start_point_marker);
		mEndPointMarker = BitmapDescriptorFactory.fromResource(R.drawable.end_point_marker);
	}
	
	/**
	 * ��ʼ����ͼ
	 * �ڵ�ͼ������ɺ�����ó̽ڵ㣬֮�����õ���ͼ��
	 */
	private void initialMap(){
				
		mBaiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
			
			@Override
			public void onMapLoaded() {

				Bundle args = getArguments();
				long runId = args.getLong(ARG_RUN_ID, -1);
				if(runId != -1){
					getLoaderManager().initLoader(LOADER_LOAD_LOCATION_DATA_LIST, args
							, mLocationDataListLoaderCallbacks);
				}else{
					Log.e(TAG, "runId have no value");
				}
			}
		});
		
		mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
			
			@Override
			public boolean onMarkerClick(Marker marker) {
				
				final LatLng markerPoint = marker.getPosition();
				Point p = mBaiduMap.getProjection().toScreenLocation(markerPoint);
				p.y -= 57;
				LatLng infoPoint = mBaiduMap.getProjection().fromScreenLocation(p);
				
				LayoutInflater layoutInflater = 
						(LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				TextView tvInfoWindowInfo = (TextView) layoutInflater.inflate(R.layout.view_map_info_window, null);
				
				Bundle args = marker.getExtraInfo();
				String markerInfo = args.getString(ARG_MARKER_INFO);
				
				tvInfoWindowInfo.setText(markerInfo);
				
				InfoWindow infoWindow = new InfoWindow(tvInfoWindowInfo, infoPoint
						, new InfoWindow.OnInfoWindowClickListener() {
					
					@Override
					public void onInfoWindowClick() {
						
						mBaiduMap.hideInfoWindow();
					}
				});
				
				mBaiduMap.showInfoWindow(infoWindow);
				
				return true;
			}
		});
		
		mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
			
			@Override
			public boolean onMapPoiClick(MapPoi arg0) {
				return false;
			}
			
			@Override
			public void onMapClick(LatLng arg0) {
				
				mBaiduMap.hideInfoWindow();
			}
		});
		
	}
	
	/**
	 * ��ʼ���ҵ�λ��
	 */
	private void initialMyLocation() {
		
		mLocationClient = new LocationClient(getActivity());
		mMyLocationListener = new MyLocationListener();
		
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setCoorType("bd09ll");
		option.setScanSpan(2*1000);
		
		mLocationClient.setLocOption(option);
	}

	/**
	 * ��ʼ��������
	 */
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
		
		mMyBDLocation = null;
		
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
	
	/**
	 * 
	 * @param latLng
	 */
	private void locateToPosition(LatLng latLng){
		
		MapStatusUpdate locationUpdate = MapStatusUpdateFactory.newLatLng(latLng);
		mBaiduMap.animateMapStatus(locationUpdate,DEFAULT_MAP_UPDATE_ANIMATION_TIME);
		
	}
	
	/**
	 * ��λ��ָ����λ�ã����ѵ�ͼ�Ŵ�
	 * @param latLng
	 * @param zoomLevel
	 */
	private void locateToPositionAndZoomTo(LatLng latLng,final float zoomLevel){
		
		MapStatusUpdate locationUpdate = MapStatusUpdateFactory.newLatLng(latLng);
		mBaiduMap.animateMapStatus(locationUpdate,DEFAULT_MAP_UPDATE_ANIMATION_TIME);
		
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {

				MapStatusUpdate zoomUpdate = MapStatusUpdateFactory.zoomTo(zoomLevel);
				mBaiduMap.animateMapStatus(zoomUpdate);				
			}
		}, DEFAULT_MAP_UPDATE_ANIMATION_TIME);
	}
	
	/**
	 * �����ҵ�λ��
	 * ��Ϊʹ���˷��򴫸�������ҪƵ�������ҵ�λ��
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
	
	/**
	 * �����ҵ�λ�ö�λ��
	 * ���ö�λģʽ
	 * @param locationMode
	 */
	private void updateMapLocationMode(LocationMode locationMode){
		
		MyLocationConfiguration configuration = 
				new MyLocationConfiguration(locationMode, true, mCustomMarker);
		mBaiduMap.setMyLocationConfigeration(configuration);
	}
	
	/**
	 * ���ó̽ڵ���µ���ͼ���棬���ó̽ڵ������ɺ����
	 */
	private void updateMap(){

		List<LatLng> pointList = new ArrayList<LatLng>();
		
		mLocationDataCursor.moveToFirst();
		while(!mLocationDataCursor.isAfterLast()){
			LocationData locationData = mLocationDataCursor.getLocationData();
			
			LatLng sourceLatLng = new LatLng(locationData.getLatitude()
					, locationData.getLongitude());
			
			// ��GPS�豸�ɼ���ԭʼGPS����ת���ɰٶ�����  
			CoordinateConverter converter  = new CoordinateConverter();  
			converter.from(CoordType.GPS);  
			// sourceLatLng��ת������  
			converter.coord(sourceLatLng);  
			LatLng desLatLng = converter.convert();
			
			pointList.add(desLatLng);

			mLocationDataCursor.moveToNext();
		}
		
		
		if(pointList.size() == 0){
			Toast.makeText(getActivity(),R.string.no_location_data
					,Toast.LENGTH_SHORT).show();
			getActivity().finish();
			return;
		}else
			if(pointList.size() == 1){

				Toast.makeText(getActivity(),R.string.need_more_location_data
						,Toast.LENGTH_SHORT).show();
				getActivity().finish();
				return;
			}

		//ȥ���ظ��Ľڵ�õ��������ó̵�
		//������ͣ����һ��λ��ʱ���ͻ�����ܶ��ظ��Ľڵ㣬ȥ����Щ�ظ��Ľڵ�
		mFinalPointList = new ArrayList<LatLng>();
		LatLng lastLL = pointList.get(0);
		mFinalPointList.add(lastLL);
		for(int i=1;i<pointList.size();i++){
			LatLng pointLL = pointList.get(i);
			double distance = DistanceUtil.getDistance(lastLL, pointLL);
			Log.i(TAG, "i:"+i+"-- distance:"+distance);
						
			if(distance > MIN_TRIP_DISTANCE){
				lastLL = pointLL;
				mFinalPointList.add(lastLL);
			}else{
				
				if(i == pointList.size()-1){
					lastLL = pointLL;
					mFinalPointList.add(lastLL);
				}
			}
		}
		
		//���߰��ó̵���������
		OverlayOptions ooPolyline = new PolylineOptions().width(6)
				.color(0xFF41A6F0).points(mFinalPointList);
		mBaiduMap.addOverlay(ooPolyline);
		
		//�ڵ�ͼ�ϱ�ע������
		for(int i=0;i<mFinalPointList.size();i++){

			LatLng pointLL = mFinalPointList.get(i);
			
			//��Ӹ�����
			MarkerOptions markerOptions =
					new MarkerOptions()
						.position(pointLL)
						.zIndex(5);
			
			if(0 == i){
				markerOptions.icon(mStartPointMarker);
			}else
				if(mFinalPointList.size()-1 == i){
					markerOptions.icon(mEndPointMarker);
				}else{
					markerOptions.icon(mPointMarker);
				}
			
			Marker marker = (Marker) mBaiduMap.addOverlay(markerOptions);
			
			//ȡ�ø�������Ϣ
			int currentIndex = i+1;
			String markerInfo = String.format(getString(R.string.trip_point), currentIndex);
			if(0 == i){
				markerInfo = getString(R.string.start_point);
			}else
				if(mFinalPointList.size()-1 == i){
					markerInfo = getString(R.string.dest_point);
				}
			
			Bundle args = new Bundle();
			args.putString(ARG_MARKER_INFO, markerInfo);
			
			marker.setExtraInfo(args);			
		}
		
		//�������е��ó̵����õ�ͼ�ı߽�
		showAllTripPoint();		
	}
	
	/**
	 * �������е��ó̵����õ�ͼ�ı߽�
	 */
	private void showAllTripPoint(){
		
		if(mFinalPointList == null || mFinalPointList.size() == 0){
			return;
		}
		
		LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
		for(LatLng pointLL:mFinalPointList){
			boundsBuilder.include(pointLL);
		}
		LatLngBounds bounds = boundsBuilder.build();
		
		MapStatusUpdate update = MapStatusUpdateFactory.newLatLngBounds(
				bounds);
		mBaiduMap.animateMapStatus(update,DEFAULT_MAP_UPDATE_ANIMATION_TIME);
	}
	
	private class MyLocationListener implements BDLocationListener{

		@Override
		public void onReceiveLocation(BDLocation bdLocation) {

			if(hasLocateToMyLocation == false){
				hasLocateToMyLocation = true;
				LatLng latLng = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
				
				locateToPositionAndZoomTo(latLng, DEFAULT_MAP_ZOOM_LEVEL);
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
	    		
	    		//ÿ���һ��ʱ�����һ�ε�ͼ�ϵķ���
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
