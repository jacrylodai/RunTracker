package com.bignerdranchldp.android.runtracker.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

import com.bignerdranchldp.android.runtracker.R;

public abstract class SingleFragmentActivity extends ActionBarActivity {
	
	protected abstract Fragment createFragment();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_single_fragment);
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment fragment = 
				fragmentManager.findFragmentById(R.id.fl_single_fragment_fragment_container);
		if(fragment == null){
			fragment = createFragment();
			fragmentManager
				.beginTransaction()
				.add(R.id.fl_single_fragment_fragment_container, fragment)
				.commit();
		}
		
	}
	
}
