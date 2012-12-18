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

import android.app.Application;
import android.util.Log;

public class WhereAppYouApplication extends Application {

    public static final String APP_NAME = "WhereAppYou";

    //private DeviceDB dataHelper;

    @Override
    public void onCreate() 
    {
    	super.onCreate();
    	Log.d(APP_NAME, "APPLICATION onCreate");
        try 
        {
        	//this.dataHelper = new DeviceDB(this);
        } 
        catch (Exception e) 
        {
			e.printStackTrace();
			Log.e(APP_NAME, "APPLICATION onCreate failed to open DB");
        }
    }

    @Override
    public void onTerminate() 
    {
    	Log.d(APP_NAME, "APPLICATION onTerminate");
        super.onTerminate();
    }

//    public DeviceDB getDeviceDB() 
//    {
//    	return this.dataHelper;
//    }
//	public void setDeviceDB(DeviceDB dataHelper) 
//	{
//		this.dataHelper = dataHelper;
//	}
}

