package com.bignerdranchldp.android.runtracker.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.bignerdranchldp.android.runtracker.R;
import com.bignerdranchldp.android.runtracker.fragment.DeleteRunItemsFragment;

public class DeleteRunItemsActivity extends SingleFragmentActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getSupportActionBar().setSubtitle(R.string.dri_delete_run);
	}

	@Override
	protected Fragment createFragment() {
		return new DeleteRunItemsFragment();
	}

}
