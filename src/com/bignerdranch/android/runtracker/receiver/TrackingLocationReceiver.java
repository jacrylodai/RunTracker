package com.bignerdranch.android.runtracker.receiver;

import android.content.Context;
import android.location.Location;

import com.bignerdranch.android.runtracker.domain.LocationData;
import com.bignerdranch.android.runtracker.manager.RunManager;

public class TrackingLocationReceiver extends LocationReceiver {

	@Override
	protected void onLocationReceived(Context context, Location location) {
		
		super.onLocationReceived(context, location);
		
		LocationData locationData = LocationData.parseLocation(location);		
		
		RunManager.getInstance(context).insertLocationData(locationData);
	}
	
}
