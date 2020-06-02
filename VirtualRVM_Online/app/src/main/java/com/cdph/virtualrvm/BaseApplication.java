package com.cdph.virtualrvm;

import android.app.Application;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import com.cdph.virtualrvm.net.InternetConnection;

public class BaseApplication extends Application
{
	public static InternetConnection conn;
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
		
		registerInternetConnectionChecker();
	}
	
	private void registerInternetConnectionChecker()
	{
		if(conn == null)
			conn = new InternetConnection();
		
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(conn, filter);
	}
}
