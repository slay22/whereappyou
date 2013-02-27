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

import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
//import android.widget.Toast;
import android.util.Log;
 
public class SMSReceiver extends BroadcastReceiver 
{
    @Override
    public void onReceive(Context context, Intent intent) 
    {
    	 if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
    	 {    	
	        // Get the SMS message passed in
	        Bundle bundle = intent.getExtras();        
	        SmsMessage[] msgs = null;
	        if (bundle != null)
	        {
	        	try
	        	{	        	
		            // Retrieve the SMS message received
		            Object[] pdus = (Object[]) bundle.get("pdus");
		            msgs = new SmsMessage[pdus.length];
		            for (int i=0; i<msgs.length; i++){
		                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
		                String payLoad = msgs[i].getMessageBody();
		                if(payLoad.toUpperCase(Locale.getDefault()).contains("WHEREAPPYOU")) 
		                {
		                    abortBroadcast();

			        		Log.v("WhereAppYouReceiver", System.currentTimeMillis() + ": SMSReceiver got a Message.");

			        		String phoneNumber = msgs[i].getOriginatingAddress(); 

			        		WhereAppYouDatabaseHelper database = WhereAppYouApplication.getDB();
			        		
			        		Request _request = new Request(phoneNumber, payLoad);
			        		database.insertNewRequest(_request);

			        		//Saving data to the database.
		                    Intent service = new Intent(context, WhereAppYouService.class);
		                    service.putExtra("PhoneNumber", _request.getPhoneNumber());
		                    service.putExtra("PayLoad", _request.getPayLoad());
		                    context.startService(service);		                    
		                }            
		            }
	        	}
	        	catch(Exception e)
	        	{
	        		Log.v("WhereAppYouReceiver", System.currentTimeMillis() + ": SMSReceiver " + e.getMessage());
	        	}
	        }
        	
    	 }
    }	
}
