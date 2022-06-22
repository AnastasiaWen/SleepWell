package com.example.navigate.ui.others;

import android.app.Application;
import android.content.SharedPreferences;


public class BluetoothApplication extends Application {
	/**
	 * Application实例
	 */
	private static BluetoothApplication application;
	
	/**
	 * 
	 */
	private AFE.AdapterManager mAdapterManager;
	
	/**
	 * 当前操作的对�?
	 */
	private TouchObject mTouchObject;

	@Override
	public void onCreate() {
		super.onCreate();
		//System.out.println("123"+application);
		if(null == application){
			application = this;
		}
		mTouchObject = new TouchObject();

		pref = getSharedPreferences("SharedPreferencesInfo", 0);
	}
	
	/**
	 * 获取Application实例
	 * @return
	 */
	public static BluetoothApplication getInstance(){
		return application;
	}

	public AFE.AdapterManager getAdapterManager() {
		return mAdapterManager;
	}

	public void setAdapterManager(AFE.AdapterManager adapterManager) {
		this.mAdapterManager = adapterManager;
	}

	public TouchObject getTouchObject() {
		return mTouchObject;
	}

	public void setTouchObject(TouchObject touchObject) {
		this.mTouchObject = touchObject;
	}

	private SharedPreferences pref  = null;
	public void saveValueBySharedPreferences(String strKey,String strValue) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(strKey, strValue);
		editor.commit();
	}

	public String getValueBySharedPreferences(String strKey) {
		String string = pref.getString(strKey,"");
		return string;
	}

}
