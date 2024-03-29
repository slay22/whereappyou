package com.freshtechnology.whereappyou;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;

public class ProcessRequestsService extends IntentService 
{
	protected static String TAG = "ProcessRequestsService";
	private Location m_Location;
	
	private boolean m_IncognitoMode = false;
	private boolean m_VoiceNotifications = false;
	private boolean m_NotifyWhenLocked = false;
	
	public ProcessRequestsService() 
	{
		super(TAG);
	}
	
	@Override
	public void onCreate() 
	{
	    super.onCreate();
	}
	

	@Override
	protected void onHandleIntent(Intent intent) 
	{
		if (intent == null) return;
		
		SharedPreferences _Preferences = PreferenceManager.getDefaultSharedPreferences(WhereAppYouApplication.getAppContext());
		boolean _OnlyFavourites = _Preferences.getBoolean("onlyFavs", true);
		
        m_IncognitoMode = _Preferences.getBoolean("incognitoMode", false);      
        m_VoiceNotifications = _Preferences.getBoolean("voiceNotifications", false);
        m_NotifyWhenLocked = _Preferences.getBoolean("notifyWhenLocked", true);
		
		Bundle extras = intent.getExtras();
		if (intent.hasExtra(WhereAppYouApplication.EXTRA_KEY_LOCATION)) 
		{
			m_Location = (Location)(extras.get(WhereAppYouApplication.EXTRA_KEY_LOCATION));
		}
		
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
		       if (_OnlyFavourites)
		       {
		    	   _answer = _Contact.isFavContact();
		       }

		       if (_answer)
		       {
		    	   ProcessRequest(_Contact);
		       }
		   }
		}
		else
			Log.v("WhereAppYouService", " ProcessData, nothing to do!");

	}
	
	private void ProcessRequest(Request Contact)
	{
		StringBuilder message = null;
  	  	String ToWhom = "";
		
		Log.v("WhereAppYouService", System.currentTimeMillis() + ": ProcessData Runnable running");
		
		ToWhom = Utils.GetName(Contact.getPhoneNumber());
		
		Notification notification = null;
		TextToSpeechController _TextToSpeechController = TextToSpeechController.getInstance(WhereAppYouApplication.getAppContext());

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

				if ( "" != Contact.getPayLoad())
				{
					// TODO : Add here extra information requested
					/*------------------------------------------------
					 * Possible Extra Info : 
					 * ETA
					 * Distance to point  -> Implemented here not yet in protocol
					 * Lost/Stolen phone send answer back and shutdown/lock SIM/phone?
					 ---------------------------------------------- */
					
					//Distance To Point.
					String dtp = Contact.getPayLoad().toUpperCase(Locale.getDefault());
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
//				message.append(URLEncoder.encode(uri.toString(), "UTF-8"));
			}
			
			//Test to send an MMS with the snapshot of the mapo location
//			URL url = new URL ("http://maps.google.com/maps/api/staticmap?center=" + lat + "," + longi+ "&zoom=15&size=200x200&sensor=false")
//
//		    InputStream input = url.openStream();
//		    try {
//		        OutputStream output = new FileOutputStream ("/sdcard/image.png");
//		        try {
//		            byte[] buffer = new byte[aReasonableSize];
//		            int bytesRead = 0;
//		            while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
//		                output.write(buffer, 0, bytesRead);
//		            }
//		        } finally {
//		            output.close();
//		        }
//		    } finally {
//		        input.close();
//		    }
//
//		Intent intent = new Intent(Intent.ACTION_SEND);
//		intent.putExtra("sms_body", "Hi how are you");
//		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File("/sdcard/image.png")));
//		intent.setType("image/gif"); 
			
			
			
			
			
			
			
			Log.v("WhereAppYouService", System.currentTimeMillis() + ": ProcessData Message built");
		
			SmsManager sms = SmsManager.getDefault();
			
			ArrayList<String> parts = sms.divideMessage(message.toString());
			
			if (parts.size() > 1)
				sms.sendMultipartTextMessage(Contact.getPhoneNumber(), null, parts, null, null);	
			else
				sms.sendTextMessage(Contact.getPhoneNumber(), null, message.toString(), null, null);						
			
			//Update the current Contact sent.
			Intent updateService = new Intent(WhereAppYouApplication.getAppContext(), RequestsUpdateService.class); 
    		updateService.putExtra(WhereAppYouApplication.EXTRA_KEY_UPDATE, Contact);
    		WhereAppYouApplication.getAppContext().startService(updateService);
			Log.v("WhereAppYouService", System.currentTimeMillis() + ": ProcessData Update Service called");
    		
			notification = Utils.SetNotification(String.format("%s %s", getString(R.string.messageDeliveredTo_txt), ToWhom), m_NotifyWhenLocked, m_VoiceNotifications, _TextToSpeechController);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			
			notification = Utils.SetNotification(String.format("%s %s", getString(R.string.messageNotDeliveredTo_txt), ToWhom), m_NotifyWhenLocked, m_VoiceNotifications, _TextToSpeechController);
		}
		
 		if (m_IncognitoMode)
 		{
 			stopForeground(true);    			
 		}
 		else
 		{
 			startForeground(1, notification);
 		}
	}
}
