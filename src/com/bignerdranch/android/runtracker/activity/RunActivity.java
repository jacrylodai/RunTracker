package com.bignerdranch.android.runtracker.activity;

import android.support.v4.app.Fragment;

import com.bignerdranch.android.runtracker.fragment.RunFragment;

public class RunActivity extends SingleFragmentActivity {
	
	public static final String EXTRA_RUN_ID = 
			"com.bignerdranch.android.runtracker.runId";

	@Override
	protected Fragment createFragment() {

		if(getIntent().hasExtra(EXTRA_RUN_ID)){
			long runId = getIntent().getLongExtra(EXTRA_RUN_ID, -1);
			return RunFragment.newInstance(runId);
		}else{
			return new RunFragment();
		}
	}

}
