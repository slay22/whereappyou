package com.freshtechnology.whereappyou;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

public class BootReceiver extends BroadcastReceiver 
{
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		try 
		{
			LocationManager _locationManager = (LocationManager)context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
			Location lastLoc = Utils.AdjustLocation(_locationManager, null);
			
			//Starting the service to check for non processed request.
	        Intent service = new Intent(context, WhereAppYouService.class);
	        service.putExtra("Location", lastLoc);
	        context.startService(service);		                    
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
	}
}
