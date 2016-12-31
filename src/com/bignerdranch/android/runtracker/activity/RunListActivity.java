package com.bignerdranch.android.runtracker.activity;

import com.bignerdranch.android.runtracker.fragment.RunListFragment;

import android.support.v4.app.Fragment;

public class RunListActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new RunListFragment();
	}

}
