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

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

public class WhereAppYouApplication extends Application 
{
	private static Context context;
    public static final String APP_NAME = "WhereAppYou";

    private static WhereAppYouDatabaseHelper m_DataHelper;

    public static Context getAppContext() 
    {
        return WhereAppYouApplication.context;
    }    

    public static WhereAppYouDatabaseHelper getDB() 
    {
    	return m_DataHelper;
    }
    
    @TargetApi(Build.VERSION_CODES.GINGERBREAD | Build.VERSION_CODES.HONEYCOMB)
	@Override
    public void onCreate() 
    {
    	Log.v(APP_NAME, System.currentTimeMillis() + ": APPLICATION created");
    	
    	if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD)
    	{
    		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
    	    	.detectAll()
    	    	.penaltyLog()
    	    	.penaltyDeath()
    	    	.build());
    		
    		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
    			.detectAll()
    			.penaltyLog()
    			.penaltyDeath()
    			.build());     		
    	}
    	
    	super.onCreate();
    	
    	WhereAppYouApplication.context = getApplicationContext();
    	
        try 
        {
        	m_DataHelper = new WhereAppYouDatabaseHelper(WhereAppYouApplication.context);
        } 
        catch (Exception e) 
        {
			e.printStackTrace();
			Log.e(APP_NAME, System.currentTimeMillis() + ": APPLICATION onCreate failed to open DB");
        }
    }

    @Override
    public void onTerminate() 
    {
    	Log.v(APP_NAME, System.currentTimeMillis() + ": APPLICATION terminated");
        super.onTerminate();
    }
}

