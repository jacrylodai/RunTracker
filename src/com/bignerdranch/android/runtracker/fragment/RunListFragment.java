package com.bignerdranch.android.runtracker.fragment;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.bignerdranch.android.runtracker.R;
import com.bignerdranch.android.runtracker.activity.RunActivity;
import com.bignerdranch.android.runtracker.db.RunDatabaseHelper.RunCursor;
import com.bignerdranch.android.runtracker.domain.Run;
import com.bignerdranch.android.runtracker.loader.SQLiteCursorLoader;
import com.bignerdranch.android.runtracker.manager.RunManager;

public class RunListFragment extends ListFragment {
	
	private static final String TAG = "RunListFragment";
	
	private static final int REQUEST_CODE_NEW_RUN = 1;
	
	private static final int LOADER_LOAD_RUN_LIST = 1;
	
	private LoaderCallbacks<Cursor> mRunListLoaderCallbacks = 
			new LoaderCallbacks<Cursor>() {

				@Override
				public Loader<Cursor> onCreateLoader(int id, Bundle args) {

					return new RunListCursorLoader(getActivity());
				}

				@Override
				public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

					RunCursorAdapter adapter = 
							new RunCursorAdapter(getActivity(), (RunCursor)cursor);
					setListAdapter(adapter);
				}

				@Override
				public void onLoaderReset(Loader<Cursor> cursor) {

					setListAdapter(null);
				}
			};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);		
		setHasOptionsMenu(true);
		
		getLoaderManager().initLoader(LOADER_LOAD_RUN_LIST, null, mRunListLoaderCallbacks);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		
		Log.i(TAG, "onListItemClick");
		Log.i(TAG,"runId:"+id);
		
		Intent intent = new Intent(getActivity(),RunActivity.class);
		intent.putExtra(RunActivity.EXTRA_RUN_ID, id);
		startActivity(intent);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.run_list_options, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.menu_item_new_run:
			
			Intent intent = new Intent(getActivity(),RunActivity.class);
			startActivityForResult(intent, REQUEST_CODE_NEW_RUN);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		Log.i(TAG,"onActivityResult");
		Log.i(TAG,"resultCode:"+resultCode);
		switch (requestCode) {
		case REQUEST_CODE_NEW_RUN:
			
			Log.i(TAG, "requery");
			getLoaderManager().restartLoader(LOADER_LOAD_RUN_LIST
					, null, mRunListLoaderCallbacks);
			break;

		default:
			break;
		}
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
			Date startDate = run.getStartDate();
			String startDateStr = 
					DateFormat.getMediumDateFormat(context).format(startDate) + " " +
					DateFormat.getTimeFormat(context).format(startDate);
			String cellText = context.getString(R.string.run_desc, startDateStr);
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
	
	private static class RunListCursorLoader extends SQLiteCursorLoader{

		public RunListCursorLoader(Context context) {
			super(context);
		}

		@Override
		protected Cursor loadCursor() {
			return RunManager.getInstance(getContext()).queryRunList();
		}
		
	}
	
}
