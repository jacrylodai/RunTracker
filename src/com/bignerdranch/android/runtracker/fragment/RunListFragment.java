package com.bignerdranch.android.runtracker.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bignerdranch.android.runtracker.R;
import com.bignerdranch.android.runtracker.db.RunDatabaseHelper.RunCursor;
import com.bignerdranch.android.runtracker.domain.Run;
import com.bignerdranch.android.runtracker.manager.RunManager;

public class RunListFragment extends ListFragment {

	private RunCursor mRunCursor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);		
		mRunCursor = RunManager.getInstance(getActivity()).queryRunList();
		
		RunCursorAdapter adapter = new RunCursorAdapter(getActivity(), mRunCursor);
		setListAdapter(adapter);
	}
	
	@Override
	public void onDestroy() {
		
		mRunCursor.close();
		super.onDestroy();
	}
	
	private class RunCursorAdapter extends CursorAdapter{

		private RunCursor mCursor;
		
		public RunCursorAdapter(Context context, RunCursor runCursor) {
			super(context, runCursor,0);
			mCursor = runCursor;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			Run run = mCursor.getRun();
			
			TextView tvStartDate = (TextView)view;
			String cellText = context.getString(R.string.run_desc, run.getStartDate());
			tvStartDate.setText(cellText);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			
			LayoutInflater layoutInflater = 
					(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = 
					layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
			return view;
		}
		
	}
	
}
