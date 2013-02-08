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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.preference.PreferenceManager;
//import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.ListAdapter;
//import android.widget.ListView;
import android.widget.SimpleAdapter;
//import android.widget.SimpleCursorAdapter;
//import android.widget.TextView;
//import android.widget.Toast;

public class MainActivity extends ListActivity 
{
	ArrayAdapter<String> m_ListAdapter = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        displaySharedPreferences();
        
        //setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
        
    @Override  
    public boolean onOptionsItemSelected(MenuItem item) {  
        switch(item.getItemId())
        {  
        case R.id.menu_settings:
        	startPrefs();
            break;  
        case R.id.exit:  
            finish();  
            break;  
        }  
        return false;  
    }      
    
    private void startPrefs()
    {
		Intent intent = new Intent(WhereAppYouApplication.getAppContext(), WhereAppYouPrefsActivity.class);
		int requestCode = 0; 
		startActivityForResult(intent, requestCode);
		displaySharedPreferences();
    }
        
    private void displaySharedPreferences() 
    {
    	ArrayList<Map<String, String>> list = buildData();
    	String[] from = { "name", "purpose" };
    	int[] to = { android.R.id.text1, android.R.id.text2 };

    	SimpleAdapter adapter = new SimpleAdapter(this, list, android.R.layout.simple_list_item_2, from, to);    	
    	
        setListAdapter(adapter);        
        
        adapter.notifyDataSetChanged();
        
        
// 	   	long minTime = prefs.getLong("locMinTime", 5 * 60 * 1000); 
// 	   	float minDistance = prefs.getFloat("locMinDistance", 10);
   	
    }
    
    private ArrayList<Map<String, String>> buildData() 
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WhereAppYouApplication.getAppContext());
    	
        boolean incognitoMode = prefs.getBoolean("incognitoMode", false);      
        boolean voiceNotifications = prefs.getBoolean("voiceNotifications", false);
        boolean onlyFavs = prefs.getBoolean("onlyFavs", true);
        boolean respWhenLocAvailable = prefs.getBoolean("respWhenLocAvailable", false);        
        boolean notifyWhenLocked = prefs.getBoolean("notifyWhenLocked", false);
        
        
        ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
        
        String _msgTemplate = "%s (%s)";
        
        list.add(putData(String.format(_msgTemplate, getString(R.string.respWhenLocAvailable_label), String.valueOf(respWhenLocAvailable)), getString(R.string.respWhenLocAvailable_txt)));
        list.add(putData(String.format(_msgTemplate, getString(R.string.incognitomode_label), String.valueOf(incognitoMode)), getString(R.string.incognitomode_txt)));
        list.add(putData(String.format(_msgTemplate, getString(R.string.voiceNotifications_label), String.valueOf(voiceNotifications)), getString(R.string.voiceNotifications_txt)));
        list.add(putData(String.format(_msgTemplate, getString(R.string.onlyFavs_label), String.valueOf(onlyFavs)), getString(R.string.onlyFavs_txt)));
        list.add(putData(String.format(_msgTemplate, getString(R.string.notifyWhenLocked_label), String.valueOf(notifyWhenLocked)), getString(R.string.notifyWhenLocked_txt)));
        
        return list;
      }    
    
    private HashMap<String, String> putData(String name, String purpose) 
    {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("name", name);
        item.put("purpose", purpose);
        return item;
      }
    
//    @Override
//    protected void onListItemClick(ListView l, View v, int position, long id) 
//    {
//    	String item = (String) getListAdapter().getItem(position);
//    	Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
//    }    
    
}
