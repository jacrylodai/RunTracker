package com.bignerdranch.android.runtracker.utils.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

import com.bignerdranch.android.runtracker.utils.io.StreamTools;

/**
 * �������ӹ�����
 * @author jacrylodai
 *
 */
public class HttpConnectionUtil {
	
	private static final String TAG = "HttpConnectionUtil";
	
	//Ĭ�����ӳ�ʱʱ�䣬����
	private static final int DEFAULT_CONNECT_TIME_OUT = 5000;
	
	//Ĭ�϶�ȡ��ʱʱ�䣬����
	private static final int DEFAULT_READ_TIME_OUT = 5000;


	public static void connect(final String webUrl,final String encoding
			,final HttpCallbackListener callbackListener){
		
		connect(webUrl, encoding, DEFAULT_CONNECT_TIME_OUT, DEFAULT_READ_TIME_OUT
				, callbackListener);
	}
	
	/**
	 * ��������
	 * @param webUrl �����ַ
	 * @param encoding ���뷽ʽ
	 * @param connectTimeOut ���ӳ�ʱʱ��
	 * @param readTimeOut ��ȡ��ʱʱ��
	 * @param callbackListener �ص�����
	 */
	public static void connect(final String webUrl,final String encoding
			,final int connectTimeOut,final int readTimeOut
			,final HttpCallbackListener callbackListener){
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {

				HttpURLConnection connection = null;
				try {
					URL url = new URL(webUrl);
					connection = 
							(HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(connectTimeOut);
					connection.setReadTimeout(readTimeOut);

					connection.setDoInput(true);
					
					Log.i(TAG, "ready to get response code");
					
					int code = connection.getResponseCode();
					if(code != 200){
						throw new IOException("code:"+code
								+".message:"+connection.getResponseMessage());
					}
					
					Log.i(TAG, "response code:"+code);
					
					InputStream in = connection.getInputStream();
					
					String response = StreamTools.readFromStream(in, encoding);
					
					if(callbackListener != null){
						callbackListener.onFinish(response);
					}
					
				} catch (MalformedURLException e) {
					e.printStackTrace();
					if(callbackListener != null){
						callbackListener.onError(e);
					}
				} catch (IOException e) {
					e.printStackTrace();
					if(callbackListener != null){
						callbackListener.onError(e);
					}
				} finally {
					
					if(connection != null){
						connection.disconnect();
					}
				}
				
			}
		}).start();
	}
	
}
