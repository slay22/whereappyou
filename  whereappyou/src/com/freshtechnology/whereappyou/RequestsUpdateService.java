package com.freshtechnology.whereappyou;

import java.util.Date;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import java.text.DateFormat;

public class RequestsUpdateService extends IntentService
{
	protected static String TAG = "RequestsUpdateService";

	public RequestsUpdateService() 
	{
		super(TAG);
		
		setIntentRedelivery(false);
	}

	@Override
	public void onCreate() 
	{
	    super.onCreate();
	}
	
	@Override
	protected void onHandleIntent(Intent intent) 
	{
		if (null != intent)
		{
			//Bundle extras = intent.getExtras();
			Request _request = null;
			
			if (intent.hasExtra(WhereAppYouApplication.EXTRA_KEY_INSERT)) 
			{
				_request = (Request)intent.getParcelableExtra(WhereAppYouApplication.EXTRA_KEY_INSERT);//extras.getParcelable(WhereAppYouApplication.EXTRA_KEY_INSERT);
//				String _phoneNumber = (String)extras.get(WhereAppYouApplication.EXTRA_KEY_INSERT);
//				_request = new Request(_phoneNumber, "");
				
				Date today = new Date();
				
				ContentValues values = new ContentValues();
				values.put(WhereAppYouDatabaseHelper.KEY_DATE, DateFormat.getDateTimeInstance().format(today));
				values.put(WhereAppYouDatabaseHelper.KEY_NUMBER, _request.getPhoneNumber());
				values.put(WhereAppYouDatabaseHelper.KEY_PROCESSED, false);
	
				getContentResolver().insert(RequestsContentProvider.CONTENT_URI, values);
			}
			else if (intent.hasExtra(WhereAppYouApplication.EXTRA_KEY_DELETE))
			{
				// TODO : Implement DELETE
				//_request = (Request)extras.get(WhereAppYouApplication.EXTRA_KEY_DELETE);
			}
			else if (intent.hasExtra(WhereAppYouApplication.EXTRA_KEY_UPDATE))
			{
				_request = (Request)intent.getParcelableExtra(WhereAppYouApplication.EXTRA_KEY_UPDATE);//extras.getParcelable(WhereAppYouApplication.EXTRA_KEY_UPDATE);

//				String _phoneNumber = (String)extras.get(WhereAppYouApplication.EXTRA_KEY_UPDATE);
//				_request = new Request(_phoneNumber, "");
				
				
				ContentValues values = new ContentValues();
				values.put(WhereAppYouDatabaseHelper.KEY_PROCESSED, true);
				
				getContentResolver().update(RequestsContentProvider.CONTENT_URI, values, WhereAppYouDatabaseHelper.KEY_NUMBER + " = ?", new String[] { _request.getPhoneNumber() });
			}
		}
	}

}
