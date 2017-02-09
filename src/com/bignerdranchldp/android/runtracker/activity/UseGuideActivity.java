package com.bignerdranchldp.android.runtracker.activity;

import com.bignerdranchldp.android.runtracker.fragment.UseGuideFragment;

import android.support.v4.app.Fragment;

public class UseGuideActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new UseGuideFragment();
	}

}
