package com.bignerdranch.android.runtracker.fragment;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
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
	
}
