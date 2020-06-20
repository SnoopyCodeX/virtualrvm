package com.cdph.virtualrvm.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

public class InternetConnection extends BroadcastReceiver
{
	private static ConnectivityManager conn;
	private OnInternetConnectionChangedListener listener;
	
    @Override
	public void onReceive(Context ctx, Intent intent)
	{
		if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
		{
			if(conn == null)
				conn = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
				
			NetworkInfo info = conn.getActiveNetworkInfo();
			if(listener != null)
				listener.onInternetConnectionChanged((info != null && info.isAvailable() && info.isConnected()));
		}
	}
	
	public static final boolean isConnected(Context ctx)
	{
		ConnectivityManager conn = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = conn.getActiveNetworkInfo();
		
		return (info != null && info.isAvailable() && info.isConnected());
	}
	
	public InternetConnection addOnInternetConnectionChangedListener(OnInternetConnectionChangedListener listener)
	{
		this.listener = listener;
		return this;
	}
	
	public interface OnInternetConnectionChangedListener
	{
		public void onInternetConnectionChanged(boolean isConnected);
	}
}
