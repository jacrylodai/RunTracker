package com.bignerdranch.android.runtracker.fragment;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.bignerdranch.android.runtracker.R;
import com.bignerdranch.android.runtracker.db.RunDatabaseHelper.RunCursor;
import com.bignerdranch.android.runtracker.domain.Run;
import com.bignerdranch.android.runtracker.loader.RunListCursorLoader;
import com.bignerdranch.android.runtracker.manager.RunManager;

public class DeleteRunItemsFragment extends Fragment {
	
	private static final String TAG = DeleteRunItemsFragment.class.getSimpleName();

	private static final int LOADER_LOAD_RUN_LIST = 1;

	private RunManager mRunManager;
	
	private ListView mLVRunList;
	
	private Button mButtonDelete,mButtonCancel;

	private LoaderCallbacks<Cursor> mRunListLoaderCallbacks = new LoaderCallbacks<Cursor>() {

				@Override
				public Loader<Cursor> onCreateLoader(int id, Bundle args) {

					return new RunListCursorLoader(getActivity());
				}

				@Override
				public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

					RunCursorAdapter adapter = 
							new RunCursorAdapter(getActivity(), (RunCursor)cursor);
					mLVRunList.setAdapter(adapter);
				}

				@Override
				public void onLoaderReset(Loader<Cursor> loader) {

					mLVRunList.setAdapter(null);
				}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mRunManager = RunManager.getInstance(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_delete_run_items, container, false);
		
		mLVRunList = (ListView) view.findViewById(R.id.lv_run_list);
		mButtonDelete = (Button) view.findViewById(R.id.button_delete);
		mButtonCancel = (Button) view.findViewById(R.id.button_cancel);
		
		mLVRunList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		mButtonDelete.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

				long[] checkedIdArr = mLVRunList.getCheckedItemIds();
				
				if(checkedIdArr.length ==0){
					
					Toast.makeText(getActivity(), R.string.no_items_are_been_selected
							, Toast.LENGTH_SHORT).show();
					return;
				}else{
					
					for(long checkedId:checkedIdArr){
						mRunManager.deleteRunById(checkedId);
					}
					
					Toast.makeText(getActivity(), R.string.delete_success
							, Toast.LENGTH_SHORT).show();
					getActivity().setResult(Activity.RESULT_OK);
					getActivity().finish();
				}
			}
		});
		
		mButtonCancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

				getActivity().setResult(Activity.RESULT_CANCELED);
				getActivity().finish();
			}
		});

		getLoaderManager().initLoader(LOADER_LOAD_RUN_LIST, null, mRunListLoaderCallbacks);
		
		return view;
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
			
			CheckedTextView textView = (CheckedTextView) view.findViewById(android.R.id.text1);
			textView.setText(run.getRunName());
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			
			LayoutInflater layoutInflater = 
					(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = 
					layoutInflater.inflate(android.R.layout.simple_list_item_multiple_choice
							, parent, false);
			return view;
		}
		
	}
	
}
