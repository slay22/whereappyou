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

//import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import android.app.Notification;
//import android.app.Notification.Builder;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
//import android.content.BroadcastReceiver;
//import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
//import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
//import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
//import android.os.AsyncTask.Status;
//import android.os.AsyncTask.Status;
import android.os.Bundle;
//import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.format.Time;
import android.util.Log; 
//import android.widget.Toast;

public class WhereAppYouService extends Service implements LocationListener, 
						SharedPreferences.OnSharedPreferenceChangeListener,
						TextToSpeech.OnInitListener
{
	   private LocationManager m_LocManager;
	   private static Location m_Location;
	   private static Intent m_PassiveIntent;
	   private static PendingIntent m_LocationListenerPassivePendingIntent;

	   private boolean m_IncognitoMode = false;
	   private boolean m_VoiceNotifications = false;
	   private boolean m_OnlyFavourites = true;
	   private boolean m_RespondWhenLocationAvailable = false;
	   private boolean m_NotifyWhenLocked = false;
	   private boolean m_BatteryLow = false;

	   //private final static long DEFAULT_MIN_TIME = 5 * 60 * 1000; //5 minutes default
	   private final static long DEFAULT_WAIT_TIME = 45 * 1000;
	   //private final static float DEFAULT_MIN_DISTANCE  = 10;
	   public final static int TWO_MINUTES = 2 * 60 * 1000;
	   
	   private final static int MAX_DISTANCE = 75;
	   private final static long MAX_TIME = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
	   private static final String POINT_LATITUDE_KEY = "POINT_LATITUDE_KEY";
	   private static final String POINT_LONGITUDE_KEY = "POINT_LONGITUDE_KEY";
	   
//	   private long m_MinTime = 0;
//	   private float m_MinDistance = 10;
	   
	   private SharedPreferences m_Preferences = null;
	   private NotificationManager m_NotificationManager = null;
//	   private BroadcastReceiver m_PowerStateChangedReceiver = null;
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
	        	//TODO : TEST!!!!
	        	//In case that Manifest registration doesn't work.
//	        	IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_LOW);
//	        	m_PowerStateChangedReceiver = new PowerStateChangedReceiver(); 
//	        	registerReceiver(m_PowerStateChangedReceiver, filter);
	        	
	        	m_TaskList = new ArrayList<TaskProcessSms>(); 
	   
	        	m_Tts = new TextToSpeech(this, this);
	        	
	        	m_Preferences = PreferenceManager.getDefaultSharedPreferences(WhereAppYouApplication.getAppContext());
	        	m_Preferences.registerOnSharedPreferenceChangeListener(this);
	        	
                m_IncognitoMode = m_Preferences.getBoolean("incognitoMode", false);      
                m_VoiceNotifications = m_Preferences.getBoolean("voiceNotifications", false);
                m_OnlyFavourites = m_Preferences.getBoolean("onlyFavs", true);
                m_NotifyWhenLocked = m_Preferences.getBoolean("notifyWhenLocked", true);
                
                m_RespondWhenLocationAvailable = m_Preferences.getBoolean("respWhenLocAvailable", false); 
//         	   	m_MinTime = m_Preferences.getLong("locnMinTime", DEFAULT_MIN_TIME); 
//         	   	m_MinDistance = m_Preferences.getFloat("locMinDistance", DEFAULT_MIN_DISTANCE);
                
    	        //We retrieve the last known location in case of service crash.
    	        getLastSavedKnowLocation();    	        
	        } 
	        catch (Exception e) 
	        {
                e.printStackTrace();
                Log.e("WhereAppYouService", "prefs failed to load " + e.getMessage());
	        }

	        m_PassiveIntent = new Intent(WhereAppYouApplication.getAppContext(), PassiveLocationChangedReceiver.class);
	        m_LocationListenerPassivePendingIntent = PendingIntent.getBroadcast(WhereAppYouApplication.getAppContext(), 0, m_PassiveIntent, PendingIntent.FLAG_UPDATE_CURRENT);	        
	        m_LocManager = (LocationManager) WhereAppYouApplication.getAppContext().getSystemService(LOCATION_SERVICE);

    		Log.v("WhereAppYouReceiver", System.currentTimeMillis() + ": WhereAppYouService location manager activated.");
	        
	        registerListeners();

    		Log.v("WhereAppYouReceiver", System.currentTimeMillis() + ": WhereAppYouService listeners registered.");

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
		        	boolean _batteryLow = false;
		        	try
		        	{
		        		_batteryLow = Boolean.parseBoolean(bundle.get("BatteryLow").toString());
		        	}
		        	catch (Exception e)
		        	{
		        		e.printStackTrace();
		        	}
		        	
		        	//Only re-register Location Listeners either when a change has happened
		        	if ((_batteryLow && !m_BatteryLow) || (!_batteryLow && m_BatteryLow))
		        		registerListeners();
		        	
		        	boolean _processData = false;
		        	Location tempLoc = (Location)bundle.get("Location");
		        	
		        	if ((null == m_Location && null != tempLoc) || 
		        		(null != m_Location && null != tempLoc) || 
		        		(null != m_Location && null == tempLoc))
		        	{
		        		tempLoc = Utils.AdjustLocation(m_LocManager, tempLoc);
		        		
		        		if (null != tempLoc)
		        		{
		        			m_Location = tempLoc; 
		        		}
		        		
		     		  _processData = true;
		        	}
	        		
		        	if (_processData)
		        	{
		        		Log.v("WhereAppYouReceiver", System.currentTimeMillis() + ": WhereAppYouService location available process started.");

			        	processData();		        		
		        	}
		        }
	        }
	        else
	        	stopSelf();
	        
	        /****************************************************************************
	         * Changed to START_NOT_STICKY in case of a problem, shouldn't be restarted,
	         * instead we wait for a new call of startService(), which means that a new 
	         * Request SMS has arrived or a the Location has changed. 
	         **************************************************************************/
	        return START_NOT_STICKY; //START_STICKY
	   }

	   @Override
	   public void onDestroy() 
	   {
	        cleanAll();
	        
	        super.onDestroy();
	        
	        Log.v("WhereAppYouService", System.currentTimeMillis() + ": WhereAppYouService dead.");
	   }

    private void saveLastKnowLocation() 
	{
    	if (null != m_Location)
    	{
	        SharedPreferences.Editor prefsEditor = m_Preferences.edit();
	        prefsEditor.putFloat(POINT_LATITUDE_KEY, (float)m_Location.getLatitude());
	        prefsEditor.putFloat(POINT_LONGITUDE_KEY, (float)m_Location.getLongitude());
	        prefsEditor.commit();
	        
	        m_Location = null;
    	}
	}

    private void getLastSavedKnowLocation() 
	{
		double lat = m_Preferences.getFloat(POINT_LATITUDE_KEY, 0);
		double lon = m_Preferences.getFloat(POINT_LONGITUDE_KEY, 0);
    	
		//We retrieve the location ONLY if it was saved before and there's no location available 
    	if (null == m_Location && (0 != lat && 0 != lon))
    	{
    		m_Location = new Location("LAST_KNOWN_LOCATION");
    		m_Location.setLatitude(lat);
    		m_Location.setLongitude(lon);
    	}
	}
    
	private void unregisterPreferencesListener() 
	   {
		   m_Preferences.unregisterOnSharedPreferenceChangeListener(this);
	       m_Preferences = null;
	   }

	   private void clearTTS() 
	   {
			if (m_Tts != null) 
			{
				m_Tts.stop();
				m_Tts.shutdown();
			}
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
    	   cleanAll();
	       
	       super.finalize();
	       
	       Log.v("WhereAppYouService", System.currentTimeMillis() + ": WhereAppYouService finilzed.");
       }

       private void cleanAll() 
       {
    	   //We save the last known location in case of service crash.
    	   saveLastKnowLocation();    	   
    	   
    	   clearTasks();

	       clearTTS();
    	   
	       //unregisterReceiver(m_PowerStateChangedReceiver);
	       
	       unregisterListeners();
	        
	       unregisterPreferencesListener();
	       
	       stopForeground(true);
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
    	    	m_NotificationManager = (NotificationManager) WhereAppYouApplication.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);
    
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
	           
	           Context context = WhereAppYouApplication.getAppContext();
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
    
      private class TaskProcessSms extends AsyncTask<Request, Void, Boolean>
      {
    	  private StringBuilder message = null;
    	  private String ToWhom = "";
    	  private Request Contact = null;
    	  private String PayLoad = "";

		@Override
		protected Boolean doInBackground(Request... params) 
		{
//			mainContext = (Context)params[0];
//			String _ToWhom = ;
//			PayLoad = (String)params[2];
			
			Log.v("WhereAppYouService", System.currentTimeMillis() + ": ProcessData Runnable running");
			
			Contact = params[0];//(Request)params[0];
			ToWhom = Utils.GetName(Contact.getPhoneNumber());
			
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

					String addressgeocoded = Utils.getAddress(lat, lon);
					
					if ("" != addressgeocoded)
					{
						message.append(addressgeocoded);
					}

					if ( "" != PayLoad)
					{
						// TODO : Add here extra information requested
						/*------------------------------------------------
						 * Possible Extra Info : 
						 * ETA
						 * Distance to point  -> Implemented here not yet in protocol
						 * Lost/Stolen phone send answer back and shutdown/lock SIM/phone?
						 ---------------------------------------------- */
						
						//Distance To Point.
						String dtp = PayLoad.toUpperCase(Locale.getDefault());
						if (dtp.contains("DTP"))
						{
							int index = dtp.indexOf("DTP="); 
							String[] dtpParams = dtp.substring(index).split(",");
							message.append(Utils.getDistanceString(m_Location, dtpParams[0], dtpParams[1]));
							message.append(" entfernt"); // TODO : Add this to the localization files
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
						sms.sendMultipartTextMessage(Contact.getPhoneNumber(), null, parts, null, null);	
					else
						sms.sendTextMessage(Contact.getPhoneNumber(), null, message.toString(), null, null);						
					
					//Update the current Contact sent.
					Intent updateService = new Intent(WhereAppYouApplication.getAppContext(), RequestsUpdateService.class); 
	        		updateService.putExtra(WhereAppYouApplication.EXTRA_KEY_INSERT, Contact);
	        		WhereAppYouApplication.getAppContext().startService(updateService);
					
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
        }
      }
      
      
      //Starts a new task
	   private void processData()
	   {
		   List<Request> requests = Utils.getNotProcessedRequests(); 
		   
		   if (!requests.isEmpty())
		   {
			   for (Request _Contact : requests)
			   {
				
				   Log.v("WhereAppYouService", String.format("ProcessData, Request %s", _Contact.getRowId()));
				   
				   boolean _answer = true;
				   
				   // TODO : Check here to answer the SMS depending on the following options : 
				   // allow all, allow only favorites, allow custom list
			       // UPDATE : partially implemented using favorites/starred.
			       if (m_OnlyFavourites)
			       {
			    	   _answer = _Contact.isFavContact();//Utils.isFavContact(getContentResolver(),_Contact.getPhoneNumber());
			       }
	
			       if (_answer)
			       {
			 		   TaskProcessSms _Task = new TaskProcessSms();
					   _Task.execute(_Contact);
					   //m_TaskList.add(_Task);
			       }
			   }
		   }
		   else
				Log.v("WhereAppYouService", " ProcessData, nothing to do!");
			   
	   }

       // Register the listeners within the Location Manager to receive location updates.
	   // The idea its to use the Passive Provider (this consumes less Battery and CPU)
	   // and should return a "quick fix" when enabled if not then fall back to 
	   // GPS and NETWORK whenever they are enabled and if the Battery it's not in Low state.   
	   private void registerListeners() 
	   {
		   try
		   {
			   unregisterListeners();

	           if (!m_BatteryLow)
	           {	  
		           if (m_LocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) 
		           {
		        	   m_Location = m_LocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		        	   m_LocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MAX_TIME, MAX_DISTANCE, m_LocationListenerPassivePendingIntent);
		           }
		           
		           if (m_LocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) 
		           {
		        	   m_Location = m_LocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);        	   
		        	   m_LocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MAX_TIME, MAX_DISTANCE, m_LocationListenerPassivePendingIntent);
		           }
	           }
	           else
	           {
		           if (m_LocManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) 
		           {
		        	   m_Location = m_LocManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		        	   m_LocManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, MAX_TIME, MAX_DISTANCE, m_LocationListenerPassivePendingIntent);
		           }
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
			   //unregisterReceiver(m_PassiveIntent);
			   m_LocManager.removeUpdates(m_LocationListenerPassivePendingIntent);
		   }
		   catch (Exception e) 
		   {
			   e.printStackTrace();
			   Log.v("WhereAppYouService", System.currentTimeMillis() + "Exception unregistering listeners");
		   }
	   }
	   
	   private void signalTasks() 
	   {
//		   if (null != m_Location)
//		   {
//			   if (!m_TaskList.isEmpty())
//			   {
//				   for (TaskProcessSms _Task : m_TaskList) 
//				   {
//					   synchronized (_Task)
//					   {
//						   _Task.notifyAll();
//					   }
//				   }
//			   }
//		   }
	   }
	   
	   @Override
	   public void onLocationChanged(Location location) 
	   {
		   m_Location = Utils.AdjustLocation(m_LocManager, location); 
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
