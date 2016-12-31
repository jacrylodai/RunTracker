package com.bignerdranch.android.runtracker.receiver;

import java.util.Date;

import android.content.Context;
import android.location.Location;

import com.bignerdranch.android.runtracker.domain.LocationData;
import com.bignerdranch.android.runtracker.manager.RunManager;

public class TrackingLocationReceiver extends LocationReceiver {

	@Override
	protected void onLocationReceived(Context context, Location location) {
		
		super.onLocationReceived(context, location);
		
		LocationData locationData = new LocationData();
		
		locationData.setTimestamp(new Date(location.getTime()));
		locationData.setLatitude(location.getLatitude());
		locationData.setLongitude(location.getLongitude());
		locationData.setAltitude(location.getAltitude());
		locationData.setProvider(location.getProvider());
		
		RunManager.getInstance(context).insertLocationData(locationData);
	}
	
}
