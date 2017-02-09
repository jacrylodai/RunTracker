package com.bignerdranch.android.runtracker.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bignerdranch.android.runtracker.R;


public class ConfigItemSelectView extends LinearLayout {

	private static final String TAG = ConfigItemSelectView.class.getSimpleName();
	
	private static final String BIGNERDRANCH_NAMESPACE = 
			"http://schemas.android.com/apk/res/com.bignerdranch.android.runtracker";
	
	private static final String ATTR_NAME_CONFIG_TITLE = "config_title";

	private static final String ATTR_NAME_CONFIG_DESC = "config_desc";
	
	private TextView tvConfigTitle,tvConfigDesc,tvConfigValue;
	
	@TargetApi(11)
	public ConfigItemSelectView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initView(attrs);
	}

	public ConfigItemSelectView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(attrs);
	}

	public ConfigItemSelectView(Context context) {
		super(context);
		initView(null);
	}
	
	private void initView(AttributeSet attrs){
		
		View view = LayoutInflater.from(getContext()).inflate(R.layout.view_config_item_select
				, this, true);
		tvConfigTitle = (TextView) view.findViewById(R.id.tv_config_title);
		tvConfigDesc = (TextView) view.findViewById(R.id.tv_config_desc);
		tvConfigValue = (TextView) view.findViewById(R.id.tv_config_value);
		
		if(attrs != null){
			String configTitle = attrs.getAttributeValue(BIGNERDRANCH_NAMESPACE
					, ATTR_NAME_CONFIG_TITLE);
			Log.i(TAG, "title:"+configTitle);
			if(configTitle != null && configTitle.length()>0){
				int configTitleId = Integer.parseInt(configTitle.substring(1));
				tvConfigTitle.setText(configTitleId);
			}
			
			String configDesc = attrs.getAttributeValue(BIGNERDRANCH_NAMESPACE
					, ATTR_NAME_CONFIG_DESC);
			Log.i(TAG, "desc:"+configDesc);
			if(configDesc != null && configDesc.length()>0){
				int configDescId = Integer.parseInt(configDesc.substring(1));
				tvConfigDesc.setText(configDescId);				
			}
		}
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
