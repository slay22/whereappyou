package com.freshtechnology.whereappyou;

import java.util.List;

import android.location.Location;
import android.location.LocationManager;

public class Utils 
{
	/** 
     * Finds the most recent and most accurate locations
     * 
     * @param LocationManager  The instantiated location manager
     * @return Location found or null when not
     */
	public static Location AdjustLocation(LocationManager locationManager, Location lastLoc) 
	{
	   List<String> providers = locationManager.getAllProviders();
	   Location tempLoc = null;
	   
	   try 
	   {
		   // Iterate through all the providers on the system to get a quick fix
		   // keeping note of the most accurate result within the acceptable time limit.
		   for (String provider: providers) 					   
		   {
			   tempLoc = locationManager.getLastKnownLocation(provider);
	
			   if (tempLoc != null) 
			   {
				   if (Utils.isBetterLocation(tempLoc, lastLoc, WhereAppYouService.TWO_MINUTES))
				   {
					   lastLoc = tempLoc;
				   }
			   }
		   }
		   
		   //Last check, if its anyway null, doesn't matter.
		   if (null == lastLoc) lastLoc = tempLoc;
	   } 
	   catch (Exception e) 
	   {
		   e.printStackTrace();
	   }
	   
	   return lastLoc;
	}
	
	
	/** 
     * From the SDK documentation. Determines whether one Location reading is better than the current Location fix
     * 
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     * @return true if the location is better then the currentBestLocation
     */
   public static boolean isBetterLocation(Location location, Location currentBestLocation, int maxTimeDelta) 
   {
	   boolean result = false;
	   
	   if (null != currentBestLocation) 
       {
			long timeDelta = location.getTime() - currentBestLocation.getTime();
			boolean isSignificantlyNewer = (timeDelta > maxTimeDelta);
			boolean isSignificantlyOlder = (timeDelta < -maxTimeDelta);
			boolean isNewer = (timeDelta > 0);
			
			if (isSignificantlyNewer) 
			{
				result = true;
			} 
			else if (isSignificantlyOlder) 
			{
				result =  false;
			}
			
			int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
			boolean isLessAccurate = (accuracyDelta > 0);
			boolean isMoreAccurate = (accuracyDelta < 0);
			boolean isSignificantlyLessAccurate = (accuracyDelta > 200);
			boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());
			
			if (isMoreAccurate) 
			{
				result = true;
			} 
			else if (isNewer && !isLessAccurate) 
			{
				result = true;
			} 
			else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) 
			{
				result = true;
			}
			else result = false;
       }
        
       return result;
    }	
   
    private static boolean isSameProvider(String provider1, String provider2) 
    {
        if (null == provider1) 
        {
            return (null == provider2);
        }
        return provider1.equals(provider2);
    }

}
