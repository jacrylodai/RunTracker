package com.bignerdranch.android.runtracker.utils.http;

/**
 * �������ӻص������ӿ�
 * @author jacrylodai
 *
 */
public interface HttpCallbackListener {

	/**
	 * ����ɹ������
	 * @param response ���������ص�����
	 */
	public void onFinish(String response);
	
	/**
	 * ����׳��쳣�����
	 * @param ex
	 */
	public void onError(Exception ex);
	
}
