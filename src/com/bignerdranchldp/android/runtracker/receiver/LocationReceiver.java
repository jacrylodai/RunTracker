package com.bignerdranchldp.android.runtracker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class LocationReceiver extends BroadcastReceiver {
	
	private static final String TAG = "LocationReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {

		Location location = 
				intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
		if(location != null){
			onLocationReceived(context,location);
			return;
		}else{
			
			if(intent.hasExtra(LocationManager.KEY_PROVIDER_ENABLED)){
				boolean providerEnabled = 
						intent.getBooleanExtra(LocationManager.KEY_PROVIDER_ENABLED, false);
				onProviderEnabledChange(providerEnabled);
			}
		}
		
	}
	
	protected void onLocationReceived(Context context,Location location){
		
		Log.i(TAG,"onLocationReceived");
		Log.i(TAG, "get location from:"+location.getProvider());
		Log.i(TAG, "latitude="+location.getLatitude()+" ; longitude="+location.getLongitude());
	}
	
	protected void onProviderEnabledChange(boolean providerEnabled){
		
		Log.i(TAG, "onProviderEnabledChange");
		Log.i(TAG,"provider " + (providerEnabled ? "enabled":"disabled"));		
	}

}
