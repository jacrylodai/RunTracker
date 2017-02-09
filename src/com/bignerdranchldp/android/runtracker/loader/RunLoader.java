package com.bignerdranchldp.android.runtracker.loader;

import android.content.Context;

import com.bignerdranchldp.android.runtracker.domain.Run;
import com.bignerdranchldp.android.runtracker.manager.RunManager;

public class RunLoader extends DataLoader<Run>{
	
	private long mRunId;

	public RunLoader(Context context,long runId) {
		super(context);
		mRunId = runId;
	}

	@Override
	public Run loadInBackground() {
		
		return RunManager.getInstance(getContext()).queryRunById(mRunId);
	}

}
