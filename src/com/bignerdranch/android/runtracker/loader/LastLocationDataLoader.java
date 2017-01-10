package com.bignerdranch.android.runtracker.loader;

import android.content.Context;

import com.bignerdranch.android.runtracker.domain.LocationData;
import com.bignerdranch.android.runtracker.manager.RunManager;

public class LastLocationDataLoader extends DataLoader<LocationData>{

	private long mRunId;
	
	public LastLocationDataLoader(Context context,long runId) {
		super(context);
		mRunId = runId;
	}
	
	@Override
	public LocationData loadInBackground() {
		
		return RunManager.getInstance(getContext()).queryLatestLocationDataByRunId(mRunId);
	}

}
