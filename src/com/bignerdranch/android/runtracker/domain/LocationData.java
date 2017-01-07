package com.bignerdranch.android.runtracker.domain;

import java.util.Date;

import android.location.Location;

public class LocationData {

	private long mLocationDataId;
	
	private long mFKRunId;
	
	private Date mTimestamp;
	
	private double mLatitude;
	
	private double mLongitude;
	
	private double mAltitude;
	
	private String mProvider;

	public long getLocationDataId() {
		return mLocationDataId;
	}

	public void setLocationDataId(long locationDataId) {
		mLocationDataId = locationDataId;
	}

	public long getFKRunId() {
		return mFKRunId;
	}

	public void setFKRunId(long fKRunId) {
		mFKRunId = fKRunId;
	}

	public Date getTimestamp() {
		return mTimestamp;
	}

	public void setTimestamp(Date timestamp) {
		mTimestamp = timestamp;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public void setLatitude(double latitude) {
		mLatitude = latitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public void setLongitude(double longitude) {
		mLongitude = longitude;
	}

	public double getAltitude() {
		return mAltitude;
	}

	public void setAltitude(double altitude) {
		mAltitude = altitude;
	}

	public String getProvider() {
		return mProvider;
	}

	public void setProvider(String provider) {
		mProvider = provider;
	}

	public static LocationData parseLocation(Location location) {
		
		LocationData locationData = new LocationData();
		Date timestamp = new Date(location.getTime());
		locationData.setTimestamp(timestamp);
		locationData.setLatitude(location.getLatitude());
		locationData.setLongitude(location.getLongitude());
		locationData.setAltitude(location.getAltitude());
		locationData.setProvider(location.getProvider());
		return locationData;
	}
	
}
