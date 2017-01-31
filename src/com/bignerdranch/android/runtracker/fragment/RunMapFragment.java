package com.bignerdranch.android.runtracker.fragment;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
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
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.bignerdranch.android.runtracker.R;
import com.bignerdranch.android.runtracker.db.RunDatabaseHelper.LocationDataCursor;
import com.bignerdranch.android.runtracker.domain.LocationData;
import com.bignerdranch.android.runtracker.loader.LocationDataListLoader;

public class RunMapFragment extends SupportMapFragment{

	private static final String TAG = RunMapFragment.class.getSimpleName();
	
	private static final String ARG_RUN_ID = "runId";
	
	private static final int LOADER_LOAD_LOCATION_DATA_LIST = 1;
	
	private BaiduMap mBaiduMap;
	
	private LocationDataCursor mLocationDataCursor;
	
	private LocationClient mLocationClient;
	
	private MyLocationListener mMyLocationListener;
	
	private boolean hasLocateToMyLocation,isTrackingMyLocation;
	
	private BDLocation mMyBDLocation;
	
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
		initialMyLocation();
		
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
		}
	}
	
	@Override
	public void onStop() {
		
		if(isTrackingMyLocation){
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
			showMyLocation();
			return true;
			
		case R.id.menu_item_do_not_show_my_location:
			
			doNotShowMyLocation();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
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
	
	private void showMyLocation(){
		
		isTrackingMyLocation = true;
		
		hasLocateToMyLocation = false;		
		
		mBaiduMap.setMyLocationEnabled(true);
		mLocationClient.registerLocationListener(mMyLocationListener);
		mLocationClient.start();
	}
	
	private void doNotShowMyLocation(){
		
		isTrackingMyLocation = false;
		
		mLocationClient.stop();
		mLocationClient.unRegisterLocationListener(mMyLocationListener);
		mBaiduMap.setMyLocationEnabled(false);
	}
	
	private void locateToPosition(LatLng latLng){
		
		MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
		mBaiduMap.animateMapStatus(update);
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
						.direction((float)0)
						.latitude(mMyBDLocation.getLatitude())
						.longitude(mMyBDLocation.getLongitude())
						.build();
			
			mBaiduMap.setMyLocationData(myLocationData);
		}
	}
	
	private void updateMap(){

		List<LatLng> pointList = new ArrayList<LatLng>();
		
		LatLng firstLatLng = null;
		
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
	
}
