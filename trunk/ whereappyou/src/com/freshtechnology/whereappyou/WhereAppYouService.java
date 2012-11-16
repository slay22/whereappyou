package com.freshtechnology.whereappyou;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//import java.util.concurrent.Executors;

import android.app.Notification;
//import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
//import android.location.Criteria;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
//import android.os.AsyncTask.Status;
import android.os.Bundle;
//import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsManager;
import android.util.Log; 
//import android.widget.Toast;

public class WhereAppYouService extends Service implements LocationListener, SharedPreferences.OnSharedPreferenceChangeListener
{
	   private String m_Contact = "";
	   private String m_PayLoad = "";
	   //private String m_Provider = "";
	   private LocationManager m_LocManager;
	   private static Location m_Location;
	   //private boolean m_GpsEnabled = false;
	   private boolean m_UseNet = false;
	   private boolean m_UsePass = false;
	   private boolean m_IncognitoMode = false;
	   private boolean m_VoiceNotifications = false;
	   private boolean m_OnlyFavourites = true;
	   private TaskTest m_processSMS = null; 
	   
	   private long m_MinTime = 5 * 60 * 1000; //5 minutes default
	   private float m_MinDistance = 10;
	   
//	   private WhereAppYouApplication m_Application = null;
	   private SharedPreferences m_Preferences = null;
	   private NotificationManager m_NotificationManager = null;
//	   private Builder m_builder = null;
   
	   
	   public IBinder onBind(Intent intent) 
	   {
	        return null;
	   }
	   
	   @Override
	   public void onCreate() 
	   {
	        Log.v("WhereAppYouService", System.currentTimeMillis() + ": WhereAppYouService created.");
	        
	        try 
	        {
		        //m_Application = (WhereAppYouApplication)getApplication();
	        	m_Preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	        	m_Preferences.registerOnSharedPreferenceChangeListener(this);
	        	
                //toasts = m_Preferences.getBoolean("toasts", true);
	        	m_UsePass = m_Preferences.getBoolean("usePassive", true);
                m_UseNet = m_Preferences.getBoolean("useNetwork", false);
                m_IncognitoMode = m_Preferences.getBoolean("incognitoMode", false);      
                m_VoiceNotifications = m_Preferences.getBoolean("voiceNotifications", false);
                m_OnlyFavourites = m_Preferences.getBoolean("onlyFavs", true);
                
         	   	m_MinTime = m_Preferences.getLong("locnMinTime", 5 * 60 * 1000); 
         	   	m_MinDistance = m_Preferences.getFloat("locMinDistance", 10);
	        } 
	        catch (Exception e) 
	        {
                e.printStackTrace();
                Log.e("WhereAppYouService", "prefs failed to load " + e.getMessage());
	        }

	        m_LocManager = (LocationManager) getBaseContext().getSystemService(LOCATION_SERVICE);
	        registerListeners();	    
	        
	        SetNotification("Service is Running");	        
	   }
	   
	   @Override
	   public int onStartCommand(Intent intent, int flags, int startId) 
	   {
	        Log.v("WhereAppYouService", System.currentTimeMillis() + ": WhereAppYouService started.");

	        Bundle bundle = intent.getExtras();
	        if (null != bundle)
	        {
	        	m_Contact = (String)bundle.get("PhoneNumber");
	        	m_PayLoad = (String)bundle.get("PayLoad");
	        	
	        	// TODO : Check here to answer the sms depending on the options to allow all, allow only favourites, allow predetermined list
	        	
	        	boolean answer = true;
	        	if (m_OnlyFavourites)
	        	{
	        		answer = isFavContactFav(m_Contact);
	        	}
	        	
	        	if (answer)
	        	{
		        	processData();
		        	signalTask();
	        	}
	        	
//	        	CountDownTimer procData = new CountDownTimer(21000, 7000) 
//	        	{
//				  @Override
//				  public void onFinish() 
//				  {
//					  try 
//					  {
//						  processData();
//				      } 
//					  catch (Exception e) 
//				      {
//						  e.printStackTrace();
//				        //  Log.e(LOG_TAG, "Error " + e.getMessage());
//				      }
//				  }
//
//				  @Override
//                  public void onTick(long arg0) 
//				  {
//					  try 
//					  {
//						  Thread.yield();
//					  } 
//					  catch (Exception e) 
//					  {
//						  e.printStackTrace();
//						//  Log.e(LOG_TAG, "Error " + e.getMessage());
//					  }
//                  }
//	        	};
//	        	procData.start();
				
	        }

	        //stopSelf();
	        
	        return START_STICKY;
	   }

	@Override
	   public void onDestroy() 
	   {
	        Log.v("WhereAppYouService", System.currentTimeMillis() + ": WhereAppYouService dead.");

	        m_LocManager.removeUpdates(this);
        	m_Preferences.unregisterOnSharedPreferenceChangeListener(this);
        	m_Preferences = null;
	        
	        stopForeground(true);
	        
	        super.onDestroy();
	   } 
	   
       @Override
       protected void finalize() throws Throwable 
       {
	       if(m_LocManager != null)
	    	   m_LocManager.removeUpdates(this);

	       if (null != m_Preferences)
	       {
	    	   m_Preferences.unregisterOnSharedPreferenceChangeListener(this);
	       }
	       
	       stopForeground(true);
	       
	       super.finalize();
       }
	   
	   
//   	   private Criteria GetFineCriteria()
//       {
//    	   Criteria criteria = new Criteria();
//    	   criteria.setAccuracy(Criteria.ACCURACY_FINE);
//    	   criteria.setAltitudeRequired(false);
//    	   criteria.setBearingRequired(false);
//    	   criteria.setSpeedRequired(false);
//    	   criteria.setCostAllowed(true);
//    	   criteria.setPowerRequirement(Criteria.POWER_HIGH);
//    	   
//    	   return criteria;
//       }
//
//       private Criteria GetCoarseCriteria(boolean lowPower)
//       {
//    	   Criteria criteria = new Criteria();
//    	   criteria.setAccuracy(Criteria.ACCURACY_COARSE);
//    	   criteria.setAltitudeRequired(false);
//    	   criteria.setBearingRequired(false);
//    	   criteria.setSpeedRequired(false);
//    	   criteria.setCostAllowed(true);
//    	   
//    	   if (lowPower)
//    		   criteria.setPowerRequirement(Criteria.POWER_LOW);
//    	   else
//    		   criteria.setPowerRequirement(Criteria.POWER_HIGH);
//    	   
//    	   return criteria;
//       }
	   
       
       @SuppressWarnings("deprecation")
       private void SetNotification(String message)
       {
    		if (!m_IncognitoMode)    	
    		{
    		   	
    		   if (m_VoiceNotifications)
    		   {
    			   // TODO : Implement TTS here
    		   }
    		   
	           // set up the notification and start foreground
	           String ns = Context.NOTIFICATION_SERVICE;
	           if (null == m_NotificationManager)
	        	   m_NotificationManager = (NotificationManager) getBaseContext().getSystemService(ns);
	
	           Intent notificationIntent = new Intent(this, MainActivity.class);
	           PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
	           
	           Notification not = new Notification(R.drawable.ic_launcher, "WhereAppYou", System.currentTimeMillis());
	           Context context = getApplicationContext();
	           CharSequence contentTitle = "WhereAppYou"; //getResources().getString(R.string.app_name);
	           CharSequence contentText = message;
	           not.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
	
	           m_NotificationManager.notify(1, not);
	           this.startForeground(1, not);
    		}
    		else 
    			stopForeground(true);
    		
//           m_builder = new Notification.Builder(getBaseContext());
//           m_builder.setContentTitle(getResources().getString(R.string.app_name));
//           m_builder.setContentText("Service Started");
//           m_builder.setSmallIcon(R.drawable.ic_launcher);
//           m_builder.setWhen(System.currentTimeMillis());
//           m_builder.setContentIntent(contentIntent);
//           
//           m_NotificationManager.notify(1, m_builder.build());
       }
    
      private class TaskTest extends AsyncTask<Object, Object, Boolean>
      {
    	  private StringBuilder message = null;
    	  private String ToWhom = "";
//    	  private String PayLoad = "";
//    	  private Context mainContext = null;

		@Override
		protected Boolean doInBackground(Object... params) 
		{
//			mainContext = (Context)params[0];
//			ToWhom = (String)params[1];
//			PayLoad = (String)params[2];
			
			Log.v("WhereAppYouService", System.currentTimeMillis() + ": ProcessData Runnable running");
			
			ToWhom = GetName(m_Contact);			
			boolean result = false;
			
			if (null == m_Location)
			{
				synchronized (this) 
				{
					try 
					{
						Log.v("WhereAppYouService", System.currentTimeMillis() + ": ProcessData Waiting");
						wait();
					} catch (InterruptedException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			  
			try 
			{
				
				message = new StringBuilder();
					
				//message.append("Hola ");
				//message.append(ToWhom);
				   
				if (null == m_Location)
				{
					message.append("Hi ");
					message.append(ToWhom);
					message.append("\n möglicherweise der Arschloch, hat das GPS ausgeschaltet, so kann ich dir nicht sagen, wo er ist");			   
				}
				else
				{
					double lat = m_Location.getLatitude();
					double lon = m_Location.getLongitude();
					
					StringBuilder uri = new StringBuilder("http://maps.google.com/maps");
					uri.append("?q=");
					uri.append(String.valueOf(lat));
					uri.append(",");
					uri.append(String.valueOf(lon));					
					uri.append("+(Here)&z=14&ll=");
					uri.append(String.valueOf(lat));
					uri.append(",");
					uri.append(String.valueOf(lon));					
				        
					message.append("bin gerade hier ");

					String addressgeocoded = getAddress(lat, lon);
					
					if ("" != addressgeocoded)
					{
						message.append(addressgeocoded);
					}

					if ( "" != m_PayLoad)
					{
						// TODO : Add here extra information requested
						/*------------------------------------------------
						 * Possible Extra Info : 
						 * ETA
						 * Distance to point  -> Implemented here not yet in protocol
						 * Lost/Stolen phone send answer back and shutdown/lock sim/phone?
						 ---------------------------------------------- */
						
						//Distance To Point.
						if (m_PayLoad.toUpperCase().contains("DTP"))
						{
							int index = m_PayLoad.toUpperCase().indexOf("DTP="); 
							String[] dtpParams = m_PayLoad.toUpperCase().substring(index).split(",");
							message.append(getDistanceString(dtpParams[0], dtpParams[1]));
							message.append(" entfernt");
						}
					}
					
					//message.append("\n");
					message.append(uri.toString());
				}
				
				Log.v("WhereAppYouService", System.currentTimeMillis() + ": ProcessData Message built");
				
				result = true;
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		
			return result;
		}

		private String getDistanceString(String Lat, String Lon) 
		{
			Location pointLocation = new Location("POINT_LOCATION");
			pointLocation.setLatitude(Float.valueOf(Lat));
			pointLocation.setLongitude(Float.valueOf(Lon));
			
			float distance = m_Location.distanceTo(pointLocation);
			
			String unit = " m";
			if (distance > 1000)
			{
				distance = Math.abs(distance / 1000); 
				unit = " km";
			}
			
			return String.valueOf(distance) + unit;
		}
		
        @Override
        protected void onPostExecute(Boolean result) 
        {
			Log.v("WhereAppYouService", System.currentTimeMillis() + ": ProcessData posting message");
        	
        	if (result)
        	{
        		try
        		{
					SmsManager sms = SmsManager.getDefault();
					
					ArrayList<String> parts = sms.divideMessage(message.toString());
					
					if (parts.size() > 1)
						sms.sendMultipartTextMessage(m_Contact, null, parts, null, null);	
					else
						sms.sendTextMessage(m_Contact, null, message.toString(), null, null);						
					
//					 String SENT = "SMS_SENT";
//					 String DELIVERED = "SMS_DELIVERED";

//				     PendingIntent sentPI = PendingIntent.getBroadcast( mainContext, 0, new Intent(SENT), 0);
//				     PendingIntent deliveredPI = PendingIntent.getBroadcast( mainContext, 0, new Intent(DELIVERED), 0);					
					
					//sms.sendMultipartTextMessage(m_Contact, null, parts, sentPI, deliveredPI);
					
					
//					sms.sendTextMessage(m_Contact, null, message.toString(), null, null);
					
					SetNotification("Message Sent to " + ToWhom);
        		}
        		catch (Exception e) 
        		{
        			e.printStackTrace();
        			SetNotification("Message has not been delivered to " + ToWhom);        			
				}
        	}
        	else
        		SetNotification("Problems creating Message to deliver");
        }
      }
      
      public String getAddress(double latitude, double longitude)
      {
          List<Address> address = null;
          Geocoder gc = new Geocoder(getBaseContext());
          try 
          {
              address = gc.getFromLocation(latitude, longitude, 1);
          } 
          catch (IOException e) 
          {
              Log.v("WhereAppYouService", System.currentTimeMillis() + ": getAddress unable to get address");              
              return "";
          }

          if (address == null || address.size() == 0) {
              Log.v("WhereAppYouService", System.currentTimeMillis() + ": getAddress unable to parse address");              
              return "";
          }

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

          return b.toString();
      }
      
      
	   private void processData()
	   {
		   if (null != m_processSMS)
		   {
			   if (m_processSMS.getStatus() == Status.RUNNING || m_processSMS.getStatus() == Status.PENDING)
				   m_processSMS.cancel(true);
			   
			   m_processSMS = null;
		   }
		   
		   m_processSMS = new TaskTest();
		   m_processSMS.execute("");

		   //m_processSMS.execute(getBaseContext(), GetName(m_Contact), m_PayLoad);
//		   	String ToWhom = GetName(m_Contact);
//		   
//			try 
//			{
//				StringBuilder message = new StringBuilder();
//				
//				message.append("Hola ");
//				message.append(ToWhom);
//				
//				if (null == m_Location)
//				{
//					message.append("\n möglicherweise der Arschloch, hat das GPS ausgeschaltet, so kann ich dir nicht sagen, wo er ist");			   
//				}
//				else
//				{
//					message.append("\n bin gerade hier ");
//					message.append("LAT: ");
//					message.append(String.valueOf(m_Location.getLatitude()));
//					message.append(", ");
//					message.append("LONG: ");
//					message.append(String.valueOf(m_Location.getLongitude()));
//				   
//					if ( "" != m_PayLoad)
//					{
//						// TODO 
//					}
//				}
//				
//				message.append("\n\n Mfg, \n WhereAppYouService Service");
//				
//				Log.v("WhereAppYouService", System.currentTimeMillis() + ": ProcessData Message built");
//				
//				SmsManager sms = SmsManager.getDefault();
//				
////				ArrayList<String> parts = sms.divideMessage( message.toString());
//				
////				 String SENT = "SMS_SENT";
////				 String DELIVERED = "SMS_DELIVERED";
//
////			     PendingIntent sentPI = PendingIntent.getBroadcast( mainContext, 0, new Intent(SENT), 0);
////			     PendingIntent deliveredPI = PendingIntent.getBroadcast( mainContext, 0, new Intent(DELIVERED), 0);					
//				
//				//sms.sendMultipartTextMessage(ToWhom, null, parts, sentPI, deliveredPI);
//				
//				sms.sendTextMessage(m_Contact, null, message.toString(), null, null);
//				
////				String sent = "android.telephony.SmsManager.STATUS_ON_ICC_SENT";
////				PendingIntent piSent = PendingIntent.getBroadcast(mainContext, 0, new Intent(sent), 0);
////
////				sms.sendTextMessage(m_Contact, null, message.toString(), piSent, null);				
//				
//				
//				SetNotification("Message Sent to " + ToWhom);
//			}
//			catch (Exception e) 
//			{
//				e.printStackTrace();
//				SetNotification("Message has not been delivered to " + ToWhom);
//			}
	   }
	   
	   private String GetName(String number) 
	   {
		   String result = number;
		   try
		   {
	           ContentResolver cr = getContentResolver();
	
	           Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
	           Cursor c = cr.query(uri, new String[] { PhoneLookup.DISPLAY_NAME /*, PhoneLookup.STARRED*/ }, null, null, null);
	
	           if (c.moveToFirst()) 
	           {
                   String name = c.getString(c.getColumnIndex(PhoneLookup.DISPLAY_NAME));
                   //boolean isStarred = Boolean.getBoolean(c.getString(c.getColumnIndex(PhoneLookup.STARRED))); 
                   result = name;
	           }       
           }
		   catch(Exception e) 
	       {
			   e.printStackTrace();
	       }
           return result;
	   }
	   
	   private boolean isFavContactFav(String number) 
	   {
		   boolean isStarred = false;
		   try
		   {
	           ContentResolver cr = getContentResolver();
	
	           Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
	           Cursor c = cr.query(uri, new String[] { PhoneLookup.STARRED }, null, null, null);
	
	           if (c.moveToFirst()) 
	           {
                   isStarred = (c.getInt(c.getColumnIndex(PhoneLookup.STARRED)) == 1); 
	           }       
           }
		   catch(Exception e) 
	       {
			   e.printStackTrace();
	       }
           return isStarred;
	   }

	   private void registerListeners() 
	   {
           // start location provider GPS
           // Register the listener with the Location Manager to
           // receive location updates
           if (m_LocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) 
           {
        	   m_Location = m_LocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);        	   
        	   m_LocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, m_MinTime, m_MinDistance, this);
           } 
           
           if (m_UseNet && m_LocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) 
           {
        	   m_Location = m_LocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        	   m_LocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, m_MinTime, m_MinDistance, this);
           }
           
           if (m_UsePass && m_LocManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) 
           {
        	   m_Location = m_LocManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        	   m_LocManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, m_MinTime, m_MinDistance, this);
           }
	   }
	   
	   // finds the most recent and most accurate locations
	   private void AdjustLocation() 
	   {
		   float oldacc = 0f;
		   
		   if (null != m_Location)
		   {
			   if (m_Location.hasAccuracy())
				   oldacc = m_Location.getAccuracy();
		   }
		   
		   List<String> providers = m_LocManager.getProviders(true);
		   Location _tempLoc = null;
		   try 
		   {
			   if (!providers.isEmpty()) 
			   {
				   for (int i = providers.size() - 1; i >= 0; i--) 
				   {
					   _tempLoc = m_LocManager.getLastKnownLocation(providers.get(i));

					   if (_tempLoc != null) 
					   {
						   if (_tempLoc.hasAccuracy()) // if we have accuracy, capture the best
						   {
							   float acc = _tempLoc.getAccuracy();
							   if (acc > oldacc) 
							   {
								   m_Location = _tempLoc;
							   }
						   }
					   }
				   }
			   }
		   } 
		   catch (Exception e) 
		   {
			   e.printStackTrace();
		   }
	   }

	   private void signalTask() 
	   {
		   if (null != m_Location)
		   {
			   //If the system restart the Service then Task its null.
			   if (null != m_processSMS)
			   {
				   synchronized (m_processSMS)
				   {
					   m_processSMS.notifyAll();
				   }
			   }
		   }
	   }
	   
	   @Override
	   public void onLocationChanged(Location location) 
	   {
		   m_Location = location;
		   AdjustLocation();

		   signalTask();
	   }
		
	   @Override
	   public void onProviderDisabled(String provider) 
	   {
	   }
		
	   @Override
	   public void onProviderEnabled(String provider) 
	   {
	   }
		
	   @Override
	   public void onStatusChanged(String provider, int status, Bundle extras) 
	   {
	   }
	   
		@Override
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) 
		{
			if (key.contains("usePassive"))
				m_UsePass = prefs.getBoolean(key, true);
			else if  (key.contains("useNetwork"))
				m_UseNet = prefs.getBoolean(key, false);
			else if  (key.contains("incognitoMode"))
				m_IncognitoMode = prefs.getBoolean(key, false);
			else if  (key.contains("voiceNotifications"))
				m_VoiceNotifications = prefs.getBoolean(key, false);
			else if  (key.contains("onlyFavs"))
				m_OnlyFavourites = prefs.getBoolean(key, true);
			else if  (key.contains("locnMinTime"))
				m_MinTime = prefs.getLong(key, 5 * 60 * 1000);
			else if  (key.contains("locMinDistance"))
				m_MinDistance = prefs.getFloat(key, 10);
			
			if  (key.contains("locnMinTime") || (key.contains("locMinDistance")))
			{
			       if(m_LocManager != null)
			       {
			    	   m_LocManager.removeUpdates(this);
			    	   registerListeners();
			       }
			}
		}
}
