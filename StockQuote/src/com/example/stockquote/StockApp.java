package com.example.stockquote;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class StockApp extends Application implements OnSharedPreferenceChangeListener{
	static final String TAG= "StockApp";
	SharedPreferences prefs;
	boolean picture;
	String pictureSize;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		picture = prefs.getBoolean("picture_toggle", true);
		pictureSize = prefs.getString("picture_size_prefs", "Standard");
		Log.d(TAG, pictureSize);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		picture= prefs.getBoolean("picture_toggle", true);
		pictureSize = prefs.getString("picture_size_prefs", "Standard");
		Log.d(TAG, pictureSize);
	}

}
