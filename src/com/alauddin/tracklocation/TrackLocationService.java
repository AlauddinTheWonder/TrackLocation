package com.alauddin.tracklocation;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class TrackLocationService extends Service 
{
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		//Toast.makeText(this, "Service Created", Toast.LENGTH_SHORT).show();
		Log.i("TService", "Service Created");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		//Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
		Log.i("TService", "Service Started");
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() 
	{
		//Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
		Log.i("TService", "Service Stoped");
	}
}
