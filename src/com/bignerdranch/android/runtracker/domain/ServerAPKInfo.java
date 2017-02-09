package com.bignerdranch.android.runtracker.domain;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 服务器程序信息
 * @author jacrylodai
 *
 */
public class ServerAPKInfo {

	//版本名称
	private String versionName;

	//最新功能描述
	private String description;

	//最新程序下载地址
	private String apkUrl;

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getApkUrl() {
		return apkUrl;
	}

	public void setApkUrl(String apkUrl) {
		this.apkUrl = apkUrl;
	}

	public static ServerAPKInfo buildFromJson(String response) throws JSONException {
		
		ServerAPKInfo serverAPKInfo = new ServerAPKInfo();
		JSONObject jsonObject = new JSONObject(response);
		serverAPKInfo.setVersionName(jsonObject.getString("versionName"));
		serverAPKInfo.setDescription(jsonObject.getString("description"));
		serverAPKInfo.setApkUrl(jsonObject.getString("apkUrl"));
		
		return serverAPKInfo;
	}
	
}
