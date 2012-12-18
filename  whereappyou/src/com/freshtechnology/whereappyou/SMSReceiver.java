/*
 * Copyright 2012 FreshTechnology (Leonardo Gutierrez & Alex Luja)
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
		                if(payLoad.toUpperCase().contains("WHEREAPPYOU")) {
		                    abortBroadcast();

		                    //TODO : Logging here requests in a database/start another service?
			        		Log.v("WhereAppYouReceiver", System.currentTimeMillis() + ": SMSReceiver got a Message.");
		                    
		                    Intent service = new Intent(context, WhereAppYouService.class);
		                    service.putExtra("PhoneNumber", msgs[i].getOriginatingAddress());
		                    service.putExtra("PayLoad", payLoad);
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
