package com.freshtechnology.whereappyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

public class PassiveLocationChangedReceiver extends BroadcastReceiver 
{
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		String key = LocationManager.KEY_LOCATION_CHANGED;
	    Location location = null;
	    
	    if (intent.hasExtra(key)) 
	    {
	    	location = (Location)intent.getExtras().get(key);
	    	
	    	//Start the service in case it's not already started.
            Intent service = new Intent(context, WhereAppYouService.class);
            service.putExtra("Location", location);
            context.startService(service);		                    
	    }
	}

}
