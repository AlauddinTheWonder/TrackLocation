package com.alauddin.tracklocation;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity 
{
	Context context;
	SharedPreferences sp;
	
	ToggleButton trackBtn;
	TextView logger;
	
	LocationManager locationManager;
	LocationListener locationListener;
	Timer timer;
	int gpsTimeout = 40000, networkTimeout = 10000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		context = this;
		sp = getSharedPreferences("TrackLocation", Context.MODE_PRIVATE);
		
		logger = (TextView) findViewById(R.id.logger);
		trackBtn = (ToggleButton) findViewById(R.id.trackBtn);
		trackBtn.setChecked(sp.getBoolean("TrackServiceStatus", false));
		trackBtn.setOnCheckedChangeListener(new TrackButtonClickListener());
		
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LocationTrackerListener();
		this.getLocation(true);
	}
	
	private void getLocation(boolean highAccuracy)
	{
		try
		{
			boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			
			if(!isGPSEnabled && !isNetworkEnabled)
			{
				/*Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
				intent.putExtra("enabled", true);
				context.sendBroadcast(intent);*/
				
				// TO-DO testing
				turnGPSOn();
				
				log("no location provider enabled");
			}
			else
			{
				if(highAccuracy)
				{
					if(isGPSEnabled)
					{
						log("Searching over gps provider");
						
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
						final Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						
						if(this.timer == null) {
				    		this.timer = new Timer();
				    	}
						this.timer.schedule(new TimerTask()
						{
							@Override
							public void run() 
							{
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										log("GPS execution finished");
										locationManager.removeUpdates(locationListener);
										
										if(lastKnownLocation != null)
											updateLocation(lastKnownLocation);
										else
										{
											getLocation(false);
										}
									}
								});
							}
						}, this.gpsTimeout);
					}
					else
					{
						/*Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
						intent.putExtra("enabled", true);
						context.sendBroadcast(intent);*/
						turnGPSOn();
						
						log("GPS not enabled");
					}
				}
				else
				{
					if(isNetworkEnabled)
					{
						log("Searching over network provider");
						
						locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
						final Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						
						if(this.timer == null) {
				    		this.timer = new Timer();
				    	}
						this.timer.schedule(new TimerTask()
						{
							@Override
							public void run() 
							{
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										log("Network execution finished");
										locationManager.removeUpdates(locationListener);
										
										if(lastKnownLocation != null)
											updateLocation(lastKnownLocation);
										else
											log("Location not found");
									}
								});
							}
						}, this.networkTimeout);
					}
					else
					{
						log("Network not enabled");
					}
				}
				
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void turnGPSOn()
	{
	     Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
	     intent.putExtra("enabled", true);
	     this.context.sendBroadcast(intent);

	    String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
	    if(!provider.contains("gps")){
	        final Intent poke = new Intent();
	        poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider"); 
	        poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
	        poke.setData(Uri.parse("3")); 
	        this.context.sendBroadcast(poke);
	    }
	}
	
	private void updateLocation(Location location)
	{
		this.cancelTimer();
		
		double lat = location.getLatitude();
		double lng = location.getLongitude();
		
		log("Lat: "+lat+", Lng: "+lng);
	}
	
	private void cancelTimer()
	{
    	if(this.timer != null)
    	{
    		this.timer.cancel();
        	this.timer.purge();
        	this.timer = null;
    	}
    }
	
	private void log(final String str)
	{
		Log.e("LocationTracker", str);
		logger.append(str+"\n");
	}

	class LocationTrackerListener implements LocationListener
	{
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) 
		{
			log("(On Status Change)- "+provider+", "+status);
		}
		
		@Override
		public void onProviderEnabled(String provider) {
			log("(On Provider Enabled)- "+provider);
		}
		
		@Override
		public void onProviderDisabled(String provider) {
			log("(On Provider Disabled)- "+provider);
		}
		
		@Override
		public void onLocationChanged(Location location) {
			log("Location Found");
			updateLocation(location);
			locationManager.removeUpdates(locationListener);
		}
	}
	
	class TrackButtonClickListener implements CompoundButton.OnCheckedChangeListener
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(isChecked)
			{
				Intent service = new Intent(context, TrackLocationService.class);
				context.startService(service);
				
				Editor edit = sp.edit();
				edit.putBoolean("TrackServiceStatus", true);
				edit.commit();
			}
			else
			{
				Intent service = new Intent(context, TrackLocationService.class);
				context.stopService(service);
				
				Editor edit = sp.edit();
				edit.putBoolean("TrackServiceStatus", true);
				edit.commit();
			}
		}
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
