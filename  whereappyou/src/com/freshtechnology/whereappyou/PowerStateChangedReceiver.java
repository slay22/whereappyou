package com.freshtechnology.whereappyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PowerStateChangedReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		boolean batteryLow = intent.getAction().equals(Intent.ACTION_BATTERY_LOW);
		
        Intent service = new Intent(context, WhereAppYouService.class);
        service.putExtra("BatteryLow", batteryLow);
        context.startService(service);		                    
	}
}
