package com.freshtechnology.whereappyou;

import android.content.Context;
import android.os.PowerManager;

public class WakeLocker 
{
	private static PowerManager.WakeLock m_WakeLock;
	 
	public static void acquire(Context context) 
	{
		release();
	 
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		m_WakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WhereappyouWakeLock");
		
		m_WakeLock.acquire();
	}
	 
	public static void release() 
	{
		if (m_WakeLock != null) 
			m_WakeLock.release(); 
		
		m_WakeLock = null;
	}
}
