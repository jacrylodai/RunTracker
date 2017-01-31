package com.bignerdranch.android.runtracker.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bignerdranch.android.runtracker.R;


public class ConfigItemSelectView extends LinearLayout {
	
	private TextView tvConfigTitle,tvConfigDesc,tvConfigValue;
	
	@TargetApi(11)
	public ConfigItemSelectView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	public ConfigItemSelectView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public ConfigItemSelectView(Context context) {
		super(context);
		initView();
	}
	
	private void initView(){
		
		View view = LayoutInflater.from(getContext()).inflate(R.layout.view_config_item_select
				, this, true);
		tvConfigTitle = (TextView) view.findViewById(R.id.tv_config_title);
		tvConfigDesc = (TextView) view.findViewById(R.id.tv_config_desc);
		tvConfigValue = (TextView) view.findViewById(R.id.tv_config_value);
	}
	
	public void setTitleId(int titleId){
		tvConfigTitle.setText(titleId);
	}
	
	public void setTitle(String title){
		tvConfigTitle.setText(title);
	}
	
	public void setDesc(String desc){
		tvConfigDesc.setText(desc);
	}
	
	public void setValue(String value){
		tvConfigValue.setText(value);
	}

}
