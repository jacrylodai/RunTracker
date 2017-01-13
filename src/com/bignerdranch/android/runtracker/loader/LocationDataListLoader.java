package com.bignerdranch.android.runtracker.loader;

import android.content.Context;
import android.database.Cursor;

import com.bignerdranch.android.runtracker.manager.RunManager;

public class LocationDataListLoader extends SQLiteCursorLoader{
	
	private long mRunId;

	public LocationDataListLoader(Context context,long runId) {
		super(context);
		mRunId = runId;
	}

	@Override
	protected Cursor loadCursor() {
		return RunManager.getInstance(getContext()).queryLocationDataListByRunId(mRunId);
	}
	
}