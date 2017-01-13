package com.bignerdranch.android.runtracker.activity;

import com.bignerdranch.android.runtracker.fragment.RunMapFragment;

import android.support.v4.app.Fragment;

public class RunMapActivity extends SingleFragmentActivity {
	
	public static final String ARG_RUN_ID = "runId";

	@Override
	protected Fragment createFragment() {
		
		long runId = getIntent().getExtras().getLong(ARG_RUN_ID, -1);
		return RunMapFragment.newInstance(runId);
	}

}
