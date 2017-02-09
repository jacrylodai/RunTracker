package com.bignerdranchldp.android.runtracker.activity;

import com.bignerdranchldp.android.runtracker.R;
import com.bignerdranchldp.android.runtracker.fragment.DeleteRunItemsFragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class DeleteRunItemsActivity extends SingleFragmentActivity {
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			getActionBar().setSubtitle(R.string.dri_delete_run);
		}
	}

	@Override
	protected Fragment createFragment() {
		return new DeleteRunItemsFragment();
	}

}
