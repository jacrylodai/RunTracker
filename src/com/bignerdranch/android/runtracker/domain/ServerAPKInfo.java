package com.bignerdranch.android.runtracker.domain;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * ������������Ϣ
 * @author jacrylodai
 *
 */
public class ServerAPKInfo {

	//�汾����
	private String versionName;

	//���¹�������
	private String description;

	//���³������ص�ַ
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
