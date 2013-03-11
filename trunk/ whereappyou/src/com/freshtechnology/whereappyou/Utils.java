package com.freshtechnology.whereappyou;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.format.Time;
import android.util.Log;

public class Utils 
{
	/** 
     * Creates a Notification with several Parameters.
     * 
     * @return Notification.
     */
	public static Notification SetNotification(String message, 
												boolean notifyWhenLocked,
												boolean voiceNotifications, 
												TextToSpeechController tts )
    {
        // set up the notification and start foreground if not Incognito mode
		NotificationManager _NotificationManager = (NotificationManager) WhereAppYouApplication.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);
 
		_NotificationManager.cancelAll();
		        	    
        Context context = WhereAppYouApplication.getAppContext();
        
        Resources res = context.getResources();
        
		Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        
        CharSequence contentTitle = res.getString(R.string.app_name);
        CharSequence contentText = message;

        Builder builder = new NotificationCompat.Builder(context);

        builder.setContentIntent(contentIntent)
	            .setSmallIcon(R.drawable.ic_launcher)
	            //.setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.some_big_img))
        		//.setTicker(res.getString(R.string.ticker))
	            .setWhen(System.currentTimeMillis())
	            .setAutoCancel(true)
	            .setContentTitle(contentTitle)
	            .setContentText(contentText);
	
        Notification not = builder.build();

        _NotificationManager.notify(1, not);       
   
        if (voiceNotifications && null != tts)
        {
        	tts.speak(message);
        }

        //TODO : I don't like how this is done, we must re-check this feature before release.
	   	if (notifyWhenLocked)
	   	{
	   		try
	   		{
	   			Time today = new Time(Time.getCurrentTimezone());
	   			today.setToNow();    			   
	   			Settings.System.putString(context.getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED, String.format("%s %s", message, today.format("%k:%M:%S")));
	   		}
	   		catch (Exception e)
	   		{
	   			e.printStackTrace();
	   		}
	   	}
	   	
	   	return not;
    }
	
	
	/** 
     * Loads all Requests not Processed.
     * 
     * @return List<Request>.
     */
	public static List<Request> getNotProcessedRequests()
	{
		String[] columns = new String[] { WhereAppYouDatabaseHelper.KEY_ROWID, 
										  WhereAppYouDatabaseHelper.KEY_DATE, 
										  WhereAppYouDatabaseHelper.KEY_NUMBER, 
										  WhereAppYouDatabaseHelper.KEY_PROCESSED 
										 };
		
		String[] values = new String[] { String.valueOf(0) }; 
		List<Request> requests = new ArrayList<Request>();
		
		Cursor cursor = null;
		
		try
		{
			   ContentResolver contentResolver = WhereAppYouApplication.getAppContext().getContentResolver();
			   
			   cursor = contentResolver.query(RequestsContentProvider.CONTENT_URI, columns, WhereAppYouDatabaseHelper.KEY_PROCESSED + " = ?", values, null);
			   
				if (cursor.moveToFirst()) 
				{
					do 
					{
						int _ID = 0;
						Date _DateReceived = null;
						String _Contact = "";
						boolean _Processed = false;
						try
						{
							_ID = cursor.getInt(cursor.getColumnIndex(WhereAppYouDatabaseHelper.KEY_ROWID));
							_DateReceived = DateFormat.getDateTimeInstance().parse(cursor.getString(cursor.getColumnIndex(WhereAppYouDatabaseHelper.KEY_DATE)));
							_Contact = cursor.getString(cursor.getColumnIndex(WhereAppYouDatabaseHelper.KEY_NUMBER));
							_Processed = (cursor.getInt(cursor.getColumnIndex(WhereAppYouDatabaseHelper.KEY_PROCESSED)) == 1);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
						
						requests.add(new Request(_ID, _DateReceived, _Contact, _Processed));
						
					} while (cursor.moveToNext());
				}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
		  if (null != cursor)
			  cursor.close();
		}
		
		return requests; 
	}
	
	public static List<Request> getProcessedRequests()
	{
		String[] columns = new String[] { WhereAppYouDatabaseHelper.KEY_ROWID, 
										  WhereAppYouDatabaseHelper.KEY_DATE, 
										  WhereAppYouDatabaseHelper.KEY_NUMBER, 
										  WhereAppYouDatabaseHelper.KEY_PROCESSED 
										 };
		
		String[] values = new String[] { String.valueOf(1) }; 
		List<Request> requests = new ArrayList<Request>();
		
		try
		{
			   ContentResolver contentResolver = WhereAppYouApplication.getAppContext().getContentResolver();
			   
			   Cursor cursor = contentResolver.query(RequestsContentProvider.CONTENT_URI, columns, WhereAppYouDatabaseHelper.KEY_PROCESSED + " = ?", values, null);
			   
				if (cursor.moveToFirst()) 
				{
					do 
					{
						int _ID = 0;
						Date _DateReceived = null;
						String _Contact = "";
						boolean _Processed = false;
						try
						{
							_ID = cursor.getInt(cursor.getColumnIndex(WhereAppYouDatabaseHelper.KEY_ROWID));
							_DateReceived = DateFormat.getDateTimeInstance().parse(cursor.getString(cursor.getColumnIndex(WhereAppYouDatabaseHelper.KEY_DATE)));
							_Contact = cursor.getString(cursor.getColumnIndex(WhereAppYouDatabaseHelper.KEY_NUMBER));
							_Processed = (cursor.getInt(cursor.getColumnIndex(WhereAppYouDatabaseHelper.KEY_PROCESSED)) == 1);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
						
						requests.add(new Request(_ID, _DateReceived, _Contact, _Processed));
						
					} while (cursor.moveToNext());
				}
				cursor.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return requests; 
	}
	
	public static List<Request> getAllRequests()
	{
		String[] columns = new String[] { WhereAppYouDatabaseHelper.KEY_ROWID, 
										  WhereAppYouDatabaseHelper.KEY_DATE, 
										  WhereAppYouDatabaseHelper.KEY_NUMBER, 
										  WhereAppYouDatabaseHelper.KEY_PROCESSED 
										 };
		
		List<Request> requests = new ArrayList<Request>();
		
		try
		{
			   ContentResolver contentResolver = WhereAppYouApplication.getAppContext().getContentResolver();
			   
			   Cursor cursor = contentResolver.query(RequestsContentProvider.CONTENT_URI, columns, null, null, null);
			   
				if (cursor.moveToFirst()) 
				{
					do 
					{
						int _ID = 0;
						Date _DateReceived = null;
						String _Contact = "";
						boolean _Processed = false;
						try
						{
							_ID = cursor.getInt(cursor.getColumnIndex(WhereAppYouDatabaseHelper.KEY_ROWID));
							_DateReceived = DateFormat.getDateTimeInstance().parse(cursor.getString(cursor.getColumnIndex(WhereAppYouDatabaseHelper.KEY_DATE)));
							_Contact = cursor.getString(cursor.getColumnIndex(WhereAppYouDatabaseHelper.KEY_NUMBER));
							_Processed = (cursor.getInt(cursor.getColumnIndex(WhereAppYouDatabaseHelper.KEY_PROCESSED)) == 1);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
						
						requests.add(new Request(_ID, _DateReceived, _Contact, _Processed));
						
					} while (cursor.moveToNext());
				}
				cursor.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return requests; 
	}
	
	/** 
     * Finds the caller's name, looking on the phone contact list
     * 
     * @param phonenumber to look for
     * @return String name if found, the phone number in case not.
     */
	public static String GetName(String number) 
	{
		String result = number;
		
		Cursor cursor = null;
		
		try
		{
		   ContentResolver contentResolver = WhereAppYouApplication.getAppContext().getContentResolver();

		   Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		   cursor = contentResolver.query(uri, new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
		
		   if (cursor.moveToFirst()) 
		   {
			   String name = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
			   result = name;
		   }       
        }
		catch(Exception e) 
	    {
			e.printStackTrace();
	    }
		finally
		{
		  if (null != cursor)
			  cursor.close();
		}
		
        return result;
	}
	   
	/** 
     * Finds if the caller belongs to the favourite's list/starred.
     * 
     * @param phonenumber to look for
     * @return boolean yes/no
     */
	public static boolean isFavContact(String number) 
	{
		boolean isStarred = false;
		Cursor cursor = null;
		
		try
		{
			ContentResolver contentResolver = WhereAppYouApplication.getAppContext().getContentResolver();
	
			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
			cursor = contentResolver.query(uri, new String[] { PhoneLookup.STARRED }, null, null, null);
	
	        if (cursor.moveToFirst()) 
	        {
	        	isStarred = (cursor.getInt(cursor.getColumnIndex(PhoneLookup.STARRED)) == 1); 
	        }       
        }
		catch(Exception e) 
	    {
			e.printStackTrace();
	    }
		finally
		{
			if (null != cursor)
				cursor.close();
		}
		
        return isStarred;
	}
	
	/** 
     * Finds the Distance between current location and the requested one.
     * 
     * @param Location current
     * @param latitude coordinate from where it needs the calculation
     * @param longitude coordinate from where it needs the calculation
     * @return String distance
     */
	public static String getDistanceString(Location location, String Lat, String Lon) 
	{
		Location pointLocation = new Location("POINT_LOCATION");
  	  	pointLocation.setLatitude(Float.valueOf(Lat));
  	  	pointLocation.setLongitude(Float.valueOf(Lon));
		
  	  	float distance = location.distanceTo(pointLocation);
		
  	  	String unit = " m";
  	  	if (distance > 1000)
  	  	{
  	  		distance = Math.abs(distance / 1000); 
  	  		unit = " km";
  	  	}
		
  	  	return String.valueOf(distance) + unit;
	}
    
	/** 
     * Returns a Geocoded Address from coordinates.
     * 
     * @param latitude coordinate from where it needs a geocoded address
     * @param longitude coordinate from where it needs a geocoded address
     * @return String geocoded address
     */
    public static String getAddress(double latitude, double longitude)
    {
  	  	String result = "";
        List<Address> address = null;
        Geocoder gc = new Geocoder(WhereAppYouApplication.getAppContext());
        
        try 
        {
            address = gc.getFromLocation(latitude, longitude, 1);
        } 
        catch (IOException e) 
        {
            Log.v("WhereAppYouService", System.currentTimeMillis() + ": getAddress unable to get address");              
        }

        if (address == null || address.size() == 0) {
            Log.v("WhereAppYouService", System.currentTimeMillis() + ": getAddress unable to parse address");              
            result = "";
        }
        else
        {
	          Address a = address.get(0);
	
	          StringBuilder b = new StringBuilder();
	          for (int i = 0; i < a.getMaxAddressLineIndex(); i++) 
	          {
	              b.append(a.getAddressLine(i));
	              if (i < (a.getMaxAddressLineIndex() - 1)) 
	              {
	                  b.append(" ");
	              }
	          }
	          result = b.toString();
        }
        
        return result;
    }
	
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
	
			   if (null != tempLoc && null != lastLoc) 
			   {
				   if (Utils.isBetterLocation(tempLoc, lastLoc, WhereAppYouService.TWO_MINUTES))
				   {
					   lastLoc = tempLoc;
				   }
			   }
		   }
		   
		   //Last check, if it's anyway null, doesn't matter.
		   if (null == lastLoc) lastLoc = tempLoc;
	   } 
	   catch (Exception e) 
	   {
		   e.printStackTrace();
	   }
	   
	   return lastLoc;
	}

	/** 
     * Copy streams
     * 
     * @param inputStream 
     * @param outpuStream 
     */
    public static void CopyStream(InputStream inputStream, OutputStream outpuStream)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
              int count=inputStream.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              outpuStream.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
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
