package com.bignerdranch.android.runtracker.utils.http;

/**
 * 网络连接回调函数接口
 * @author jacrylodai
 *
 */
public interface HttpCallbackListener {

	/**
	 * 如果成功则调用
	 * @param response 服务器返回的数据
	 */
	public void onFinish(String response);
	
	/**
	 * 如果抛出异常则调用
	 * @param ex
	 */
	public void onError(Exception ex);
	
}
