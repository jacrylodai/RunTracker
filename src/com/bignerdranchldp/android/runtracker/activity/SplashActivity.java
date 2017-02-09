package com.bignerdranchldp.android.runtracker.activity;

import java.io.File;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import org.json.JSONException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bignerdranchldp.android.runtracker.R;
import com.bignerdranchldp.android.runtracker.domain.ServerAPKInfo;
import com.bignerdranchldp.android.runtracker.utils.http.HttpCallbackListener;
import com.bignerdranchldp.android.runtracker.utils.http.HttpConnectionUtil;

public class SplashActivity extends ActionBarActivity{
	
	private static final String TAG = SplashActivity.class.getSimpleName();

	protected static final int NETWORK_ERROR = 1;

	protected static final int JSON_ERROR = 2;

	protected static final int ENTER_HOME = 3;

	protected static final int UPDATE_DIALOG = 4;
	
	private SharedPreferences mPref;
	
	private ServerAPKInfo serverAPKInfo;

	private ProgressBar pbSplashDownload;
	
	private Handler handler = new Handler(){
		
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			
			case NETWORK_ERROR:

				Log.i(TAG, "网络异常");		
//				Toast.makeText(SplashActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
				enterHome();
				break;

			case JSON_ERROR:

				Log.i(TAG, "JSON解析异常");		
				Toast.makeText(SplashActivity.this
						, "JSON解析异常", Toast.LENGTH_SHORT).show();
				enterHome();
				break;

			case ENTER_HOME:
				
				Log.i(TAG, "进入主界面");				
				enterHome();
				break;

			case UPDATE_DIALOG:

				Log.i(TAG, "弹出升级对话框");
				showUpdateDialog();
				break;
				
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);

        pbSplashDownload = (ProgressBar) findViewById(R.id.pb_splash_download);
		pbSplashDownload.setProgress(0);
        pbSplashDownload.setVisibility(View.INVISIBLE);
        
        checkVersion();
        
	}
	
	private void enterHome(){
		
		Intent intent = new Intent(this,RunListActivity.class);
		startActivity(intent);
		finish();
	}
	
	/**
	 * 取得服务器的版本名称，并和服务器的版本名称进行比对
	 */
	private void checkVersion() {

		final long startTime = System.currentTimeMillis();
		
    	String infoUrl = getString(R.string.serverApkInfoUrl);
    	HttpConnectionUtil.connect(infoUrl,"UTF-8",2000,3000, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {

				long endTime = System.currentTimeMillis();
				long elapsedTime = endTime - startTime;
				//展示2秒钟的logo
				if(elapsedTime<2000){
					try {
						Thread.sleep(2000-elapsedTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				Log.i(TAG, response);
				try {
					serverAPKInfo = ServerAPKInfo.buildFromJson(response);
				} catch (JSONException e) {
					e.printStackTrace();
					handler.sendEmptyMessage(JSON_ERROR);
					return;
				}
				
				String currVersionName = getVersionName();
				if(currVersionName.equals(serverAPKInfo.getVersionName())){
					
					handler.sendEmptyMessage(ENTER_HOME);
				}else{
					
					handler.sendEmptyMessage(UPDATE_DIALOG);
				}
			}
			
			@Override
			public void onError(Exception ex) {
				
				Log.e(TAG, "", ex);
				
				handler.sendEmptyMessage(NETWORK_ERROR);
			}
		});
	}

	/**
	 * 取得版本名称
	 * @return
	 */
	public String getVersionName(){
    	
    	PackageManager pm = getPackageManager();
    	try {
			PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
    }

    /**
     * 弹出升级对话框
     */
    private void showUpdateDialog() {

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("发现新版本，是否升级");
    	builder.setMessage(serverAPKInfo.getDescription());
    	builder.setCancelable(true);
    	builder.setPositiveButton("升级", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
				updateCurrentProgram();
			}
		});
    	
    	builder.setNegativeButton("暂不升级", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
				enterHome();
			}
		});
    	
    	//当用户取消时进入主页面
    	builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {

				Log.i(TAG, "onCancel");
				dialog.dismiss();
				enterHome();
			}
		});
    	
    	builder.show();
	}

    /**
     * 升级当前应用
     */
	private void updateCurrentProgram() {

		//取得外置SD卡的状态
		String externalStorageState = Environment.getExternalStorageState();
		if(externalStorageState.equals(Environment.MEDIA_MOUNTED)){
			
			String externalStoragePath = 
					Environment.getExternalStorageDirectory().getAbsolutePath();
			String newApkPath = externalStoragePath + "/download/runTracker.apk";
			File newApkFile = new File(newApkPath);
			newApkFile.delete();
			
			pbSplashDownload.setVisibility(View.VISIBLE);
			
			FinalHttp finalHttp = new FinalHttp();
			finalHttp.download(
					serverAPKInfo.getApkUrl()
					, newApkPath
					, new AjaxCallBack<File>() {

						@Override
						public void onLoading(long count, long current) {

							super.onLoading(count, current);
							int progress = (int) (current*100/count);
							pbSplashDownload.setProgress(progress);
						}

						@Override
						public void onFailure(Throwable t, int errorNo,
								String strMsg) {
							
							super.onFailure(t, errorNo, strMsg);
							Log.e(TAG, "", t);
							Toast.makeText(SplashActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
							
							enterHome();
						}

						@Override
						public void onSuccess(File t) {
							
							super.onSuccess(t);
							
							Intent intent = new Intent();
							intent.setAction("android.intent.action.VIEW");
							intent.addCategory("android.intent.category.DEFAULT");
							intent.setDataAndType(Uri.fromFile(t)
									, "application/vnd.android.package-archive");     
							startActivity(intent);
							SplashActivity.this.finish();
						}
						
			});
			
		}else{
			//sd存储卡不可用
			Toast.makeText(this, "SD卡无法使用，无法升级", Toast.LENGTH_SHORT).show();

			enterHome();
		}
	}

}
