/*
 * Copyright 2012-2013 FreshTechnology (Leonardo Gutierrez & Alex Luja)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.freshtechnology.whereappyou;

import java.io.IOException;
//import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import android.app.Notification;
//import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
//import android.location.Criteria;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
//import android.os.AsyncTask.Status;
//import android.os.AsyncTask.Status;
import android.os.Bundle;
//import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.format.Time;
import android.util.Log; 
//import android.widget.Toast;

public class WhereAppYouService extends Service implements LocationListener, 
						SharedPreferences.OnSharedPreferenceChangeListener,
						TextToSpeech.OnInitListener
{
	   private String m_Contact = "";
	   private String m_PayLoad = "";
	   //private String m_Provider = "";
	   private LocationManager m_LocManager;
	   private static Location m_Location;
	   //private boolean m_GpsEnabled = false;
	   private boolean m_IncognitoMode = false;
	   private boolean m_VoiceNotifications = false;
	   private boolean m_OnlyFavourites = true;
	   private boolean m_RespondWhenLocationAvailable = false;
	   private boolean m_NotifyWhenLocked = false;
	   
	   //private final static long DEFAULT_MIN_TIME = 5 * 60 * 1000; //5 minutes default
	   private final static long DEFAULT_WAIT_TIME = 45 * 1000;
	   //private final static float DEFAULT_MIN_DISTANCE  = 10;
	   private final static int TWO_MINUTES = 2 * 60 * 1000;
	   
//	   private long m_MinTime = 0;
//	   private float m_MinDistance = 10;
	   
//	   private WhereAppYouApplication m_Application = null;
	   private SharedPreferences m_Preferences = null;
	   private NotificationManager m_NotificationManager = null;
//	   private Builder m_builder = null;
	   
	   private TextToSpeech m_Tts = null;
	   private boolean m_ttsInitialied = false;
	   private List<TaskProcessSms> m_TaskList;
   
	   
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
	        	m_TaskList = new ArrayList<TaskProcessSms>(); 
	   
	        	m_Tts = new TextToSpeech(this, this);
	        	
		        //m_Application = (WhereAppYouApplication)getApplication();
	        	m_Preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	        	m_Preferences.registerOnSharedPreferenceChangeListener(this);
	        	
                m_IncognitoMode = m_Preferences.getBoolean("incognitoMode", false);      
                m_VoiceNotifications = m_Preferences.getBoolean("voiceNotifications", false);
                m_OnlyFavourites = m_Preferences.getBoolean("onlyFavs", true);
                m_NotifyWhenLocked = m_Preferences.getBoolean("notifyWhenLocked", true);
                
                m_RespondWhenLocationAvailable = m_Preferences.getBoolean("respWhenLocAvailable", false); 
//         	   	m_MinTime = m_Preferences.getLong("locnMinTime", DEFAULT_MIN_TIME); 
//         	   	m_MinDistance = m_Preferences.getFloat("locMinDistance", DEFAULT_MIN_DISTANCE);
	        } 
	        catch (Exception e) 
	        {
                e.printStackTrace();
                Log.e("WhereAppYouService", "prefs failed to load " + e.getMessage());
	        }

	        m_LocManager = (LocationManager) getBaseContext().getSystemService(LOCATION_SERVICE);
	        
	        SetNotification(getString(R.string.serviceIsRunning_txt));	        
	   }
	   
	   @Override
	   public int onStartCommand(Intent intent, int flags, int startId) 
	   {
	        Log.v("WhereAppYouService", System.currentTimeMillis() + ": WhereAppYouService started.");

	        if (null != intent)
	        {
		        Bundle bundle = intent.getExtras();
		        if (null != bundle)
		        {
			        registerListeners();	    
		        	
		        	m_Contact = (String)bundle.get("PhoneNumber");
		        	m_PayLoad = (String)bundle.get("PayLoad");
		        	
		        	// TODO : Check here to answer the SMS depending on the options to allow all, allow only favorites, allow custom list
		        	
		        	boolean answer = true;
		        	if (m_OnlyFavourites)
		        	{
		        		answer = isFavContact(m_Contact);
		        	}
		        	
		        	if (answer)
		        	{
			        	processData();
			        	signalTasks();
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
	        }
	        else
	        	stopSelf();
	        
	        /****************************************************************************
	         * Changed to START_NOT_STICKY in case of a problem, shouldn't be restarted,
	         * instead we wait for a new call of startService(), which means that a new 
	         * Request SMS has arrived. 
	         **************************************************************************/
	        return START_NOT_STICKY; //START_STICKY;
	   }

	   @Override
	   public void onDestroy() 
	   {
	        Log.v("WhereAppYouService", System.currentTimeMillis() + ": WhereAppYouService dead.");

	        clearTasks();
	        
	        if (m_Tts != null) 
	        {
	        	m_Tts.stop();
	        	m_Tts.shutdown();
	        }
	        
	        unregisterListeners();
	        
        	m_Preferences.unregisterOnSharedPreferenceChangeListener(this);
        	m_Preferences = null;
	        
	        stopForeground(true);
	        
	        super.onDestroy();
	   }

		private void clearTasks() 
		{
			if (null != m_TaskList && !m_TaskList.isEmpty())
			{
				for (TaskProcessSms _Task : m_TaskList)
				{
					_Task.cancel(true);
				}
				m_TaskList.clear();
				m_TaskList = null;
			}
		} 
	   
       @Override
       protected void finalize() throws Throwable 
       {
    	   clearTasks();

	        if (m_Tts != null) 
	        {
	        	m_Tts.stop();
	        	m_Tts.shutdown();
	        }
    	   
	       if (null != m_LocManager)
	       {
	    	   m_LocManager.removeUpdates(this);
	       }
	       
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
	   
       
       private void SetNotification(String message)
       {
           // set up the notification and start foreground if not Incognito mode
    	    if (null == m_NotificationManager)
    	    	m_NotificationManager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
    
			m_NotificationManager.cancelAll();
		        	    
    		if (m_IncognitoMode)
    		{
    			stopForeground(true);    			
    		}
    		else
    		{
		       Intent notificationIntent = new Intent(this, MainActivity.class);
	           PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

	           Resources res = getResources();
	           
	           Context context = getApplicationContext();
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

	           m_NotificationManager.notify(1, not);       
	           
	           startForeground(1, not);
	           
    		   if (m_VoiceNotifications && m_ttsInitialied)
    		   {
    			   m_Tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
    		   }

			   //TODO : I don't like how this is done, we must re-check this feature before release.
    		   if (m_NotifyWhenLocked)
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
    		}
       }
    
      private class TaskProcessSms extends AsyncTask<Object, Object, Boolean>
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
			
			//TODO : Translate all messages to "current" language.
			
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
						
						//If the Service must respond when location becomes available, then we wait here until notified.
						//if not then we wait anyway but only 45 seconds (see explanation below) but if after waiting, still 
						//no location becomes available, we inform the caller to try again later.
						
						//IMPORTANT : "Normally" It can take about 45 seconds to acquire satellite signals (if GPS Enabled) 
						//when first start the application or roughly 15 seconds if it has been used recently. Because the 
						//signal cannot pass through solid non-transparent objects, GPS requires an non-obstructed view 
						//of the sky to work correctly. Thus, signal reception can be degraded by tall buildings, bridges,
						//tunnels, mountains, etc. Also, moving around while locking onto several satellites makes it harder
						//for those separate signals to pinpoint the exact location, that's why we use the 45 seconds to wait. 
						
						if (m_RespondWhenLocationAvailable)
							wait(); //Actually i don't know which problems (CPU/battery) this option may carry, we need to test on real devices.
						else
							wait(DEFAULT_WAIT_TIME);
						
					} catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
				}
			}
			  
			try 
			{
				
				message = new StringBuilder();
					
				if (null == m_Location)
				{
					message.append(String.format("%s %s", getString(R.string.greetings_txt), ToWhom));
					message.append(String.format("\n %s", getString(R.string.NoLocationAvailable_txt)));
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
					uri.append("(Here)&z=14&ll=");
					uri.append(String.valueOf(lat));
					uri.append(",");
					uri.append(String.valueOf(lon));					
				        
					message.append(String.format("%s ", getString(R.string.here_txt)));

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
						 * Lost/Stolen phone send answer back and shutdown/lock SIM/phone?
						 ---------------------------------------------- */
						
						//Distance To Point.
						String dtp = m_PayLoad.toUpperCase(Locale.getDefault());
						if (dtp.contains("DTP"))
						{
							int index = dtp.indexOf("DTP="); 
							String[] dtpParams = dtp.substring(index).split(",");
							message.append(getDistanceString(dtpParams[0], dtpParams[1]));
							message.append(" entfernt");
						}
					}
					
					message.append(" ");
					message.append(uri.toString());
//					message.append(URLEncoder.encode(uri.toString(), "UTF-8"));
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
					
					SetNotification(String.format("%s %s", getString(R.string.messageDeliveredTo_txt), ToWhom));
        		}
        		catch (Exception e) 
        		{
        			e.printStackTrace();
        			SetNotification(String.format("%s %s", getString(R.string.messageNotDeliveredTo_txt), ToWhom));
				}
        	}
        	else
        		SetNotification(getString(R.string.messageWithProblems_txt)); 
        	
        	unregisterListeners();
        }
      }
      
      //Return the Distance between current location and the requested one.
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
      
      //Returns a Geocoded Address from coordinates.
      public String getAddress(double latitude, double longitude)
      {
    	  String result = "";
          List<Address> address = null;
          Geocoder gc = new Geocoder(getBaseContext());
          
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
      
      
      //Starts a new task
	   private void processData()
	   {
		   AdjustLocation();

		   TaskProcessSms _Task = new TaskProcessSms();
		   _Task.execute("");
		   m_TaskList.add(_Task);
	   }
	   
	   //Returns the name of the caller, searching in the contact List
	   private String GetName(String number) 
	   {
		   String result = number;
		   try
		   {
	           ContentResolver cr = getContentResolver();
	
	           Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
	           Cursor c = cr.query(uri, new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
	
	           if (c.moveToFirst()) 
	           {
                   String name = c.getString(c.getColumnIndex(PhoneLookup.DISPLAY_NAME));
                   result = name;
	           }       
           }
		   catch(Exception e) 
	       {
			   e.printStackTrace();
	       }
           return result;
	   }
	   
	   //Returns of the caller belongs to the favourite's list.
	   private boolean isFavContact(String number) 
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

       // Register the listeners with the Location Manager to receive location updates.
	   // First try to use ONLY Passive Provider (this consumes less Battery and CPU)
	   // and should return a "quick fix" when enabled if not then fall back to 
	   // others Providers, trying to use GPS as LAST RESORT.
	   private void registerListeners() 
	   {
		   try
		   {
//	           if (m_LocManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) 
//	           {
//	        	   m_Location = m_LocManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
//	        	   m_LocManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, m_MinTime, m_MinDistance, this);
//	           }
//	           else
//	           {
//		           if (m_LocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) 
//		           {
//		        	   m_Location = m_LocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//		        	   m_LocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, m_MinTime, m_MinDistance, this);
//		           }
//		           else if (m_LocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) 
//		           {
//		        	   m_Location = m_LocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);        	   
//		        	   m_LocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, m_MinTime, m_MinDistance, this);
//		           } 
//	           }
			   
			   unregisterListeners();
			   
	           if (m_LocManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) 
	           {
	        	   m_Location = m_LocManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
	        	   m_LocManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
	           }
			   
	           if (m_LocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) 
	           {
	        	   m_Location = m_LocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	        	   m_LocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
	           }
	           
	           if (m_LocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) 
	           {
	        	   m_Location = m_LocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);        	   
	        	   m_LocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	           } 
		   }
		   catch (IllegalArgumentException e)  // http://code.google.com/p/android/issues/detail?id=21237, maybe?
		   {
			   e.printStackTrace();
			   Log.v("WhereAppYouService", System.currentTimeMillis() + "Exception requesting updates -- may be emulator issue");
			   
			   m_Location = new Location(LocationManager.PASSIVE_PROVIDER);
		   }
	   }
	   
	   private void unregisterListeners()
	   {
		   try
		   {
			   m_LocManager.removeUpdates(this);
		   }
		   catch (Exception e) 
		   {
			   e.printStackTrace();
			   Log.v("WhereAppYouService", System.currentTimeMillis() + "Exception unregistering listeners");
		   }
	   }
	   
	   // finds the most recent and most accurate locations
	   private void AdjustLocation() 
	   {
		   List<String> providers = m_LocManager.getAllProviders();
		   Location tempLoc = null;
		   
		   try 
		   {
			   // Iterate through all the providers on the system to get a quick fix
			   // keeping note of the most accurate result within the acceptable time limit.
			   for (String provider: providers) 					   
			   {
				   tempLoc = m_LocManager.getLastKnownLocation(provider);

				   if (tempLoc != null) 
				   {
					   if (isBetterLocation(tempLoc, m_Location))
					   {
						   m_Location = tempLoc;
					   }
				   }
			   }
		   } 
		   catch (Exception e) 
		   {
			   e.printStackTrace();
		   }
	   }

	   /** 
	     * From the SDK documentation. Determines whether one Location reading is better than the current Location fix
	     * 
	     * @param location  The new Location that you want to evaluate
	     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	     * @return true if the location is better then the currentBestLocation
	     */
	   protected boolean isBetterLocation(Location location, Location currentBestLocation) 
	   {
		   boolean result = false;
		   
	        if (null != currentBestLocation) 
	        {
		        long timeDelta = location.getTime() - currentBestLocation.getTime();
		        boolean isSignificantlyNewer = (timeDelta > TWO_MINUTES);
		        boolean isSignificantlyOlder = (timeDelta < -TWO_MINUTES);
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
	   
	    private boolean isSameProvider(String provider1, String provider2) 
	    {
	        if (null == provider1) 
	        {
	            return (null == provider2);
	        }
	        return provider1.equals(provider2);
	    }
	   
	   private void signalTasks() 
	   {
		   if (null != m_Location)
		   {
			   if (!m_TaskList.isEmpty())
			   {
				   for (TaskProcessSms _Task : m_TaskList) 
				   {
					   synchronized (_Task)
					   {
						   _Task.notifyAll();
					   }
				   }
			   }
		   }
	   }
	   
	   @Override
	   public void onLocationChanged(Location location) 
	   {
		   m_Location = location;
		   AdjustLocation();
		   signalTasks();
	   }
		
	   @Override
	   public void onProviderDisabled(String provider) 
	   {
           Log.v("WhereAppYouService", System.currentTimeMillis() + provider + " disabled");              
	   }
		
	   @Override
	   public void onProviderEnabled(String provider) 
	   {
           Log.v("WhereAppYouService", System.currentTimeMillis() + provider + " enabled");              
	   }
		
	   @Override
	   public void onStatusChanged(String provider, int status, Bundle extras) 
	   {
           Log.v("WhereAppYouService", System.currentTimeMillis() + provider + " status changed to " + String.valueOf(status));              
	   }
	    
		@Override
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) 
		{
			if  (key.contains("incognitoMode"))
				m_IncognitoMode = prefs.getBoolean(key, false);
			else if  (key.contains("voiceNotifications"))
				m_VoiceNotifications = prefs.getBoolean(key, false);
			else if  (key.contains("onlyFavs"))
				m_OnlyFavourites = prefs.getBoolean(key, true);
//			else if  (key.contains("locMinTime"))
//				m_MinTime = prefs.getLong(key, DEFAULT_MIN_TIME);
//			else if  (key.contains("locMinDistance"))
//				m_MinDistance = prefs.getFloat(key, DEFAULT_MIN_DISTANCE);
			else if  (key.contains("respWhenLocAvailable"))
				m_RespondWhenLocationAvailable = prefs.getBoolean(key, false);
			else if  (key.contains("notifyWhenLocked"))
				m_NotifyWhenLocked = prefs.getBoolean(key, true);
			
//			if  (key.contains("locMinTime") || (key.contains("locMinDistance")))
//			{
//			       if(m_LocManager != null)
//			       {
//			    	   m_LocManager.removeUpdates(this);
//			    	   registerListeners();
//			       }
//			}
		}

		@Override
		public void onInit(int status) 
		{
			if (status == TextToSpeech.SUCCESS) 
			{
				int result = m_Tts.setLanguage(Locale.getDefault());
 
				if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) 
				{
					Log.e("WhereAppYouService", "TTS : This Language is not supported");
				} 
				else 
				{
					m_ttsInitialied = true;
				}
 			} 
			else 
			{
				Log.e("WhereAppYouService", "TTS : Initilization Failed!");
			}			
		}
}
