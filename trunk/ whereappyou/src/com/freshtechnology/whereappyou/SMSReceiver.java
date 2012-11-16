package com.freshtechnology.whereappyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
//import android.widget.Toast;
 
public class SMSReceiver extends BroadcastReceiver 
{
    @Override
    public void onReceive(Context context, Intent intent) 
    {
    	//we dont need this
//       	 if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
//       	 {
//             Intent service = new Intent(context, WhereAppYouService.class);
//             context.startService(service);		                    
//       	 } 
    		
    	 if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
    	 {    	
	        //---get the SMS message passed in---
	        Bundle bundle = intent.getExtras();        
	        SmsMessage[] msgs = null;
	        if (bundle != null)
	        {
	        	try
	        	{	        	
		            //---retrieve the SMS message received---
		            Object[] pdus = (Object[]) bundle.get("pdus");
		            msgs = new SmsMessage[pdus.length];
		            for (int i=0; i<msgs.length; i++){
		                msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
		                String payLoad = msgs[i].getMessageBody();
		                if(payLoad.toUpperCase().contains("WHEREAPPYOU")) {
		                    abortBroadcast();
//		                    String str = "";                    
//		                    str += "SMS from " + msgs[i].getOriginatingAddress();                     
//		                    str += " :";
//		                    str += msgs[i].getMessageBody().toString();
//		                    str += "\n";
		                    //---display the new SMS message---
		                    //Toast.makeText(context, str, Toast.LENGTH_SHORT).show();

		                    //TODO : Logging here requests?
		                    
		                    Intent service = new Intent(context, WhereAppYouService.class);
		                    service.putExtra("PhoneNumber", msgs[i].getOriginatingAddress());
		                    service.putExtra("PayLoad", payLoad);
		                    context.startService(service);		                    
		                }            
		            }
	        	}
	        	catch(Exception e)
	        	{
        			// Log.d("Exception caught",e.getMessage());
	        	}
	        }
    	 }
    }	
}
