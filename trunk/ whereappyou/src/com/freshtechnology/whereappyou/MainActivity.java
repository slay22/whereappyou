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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
//import android.app.Activity;
//import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.ListAdapter;
//import android.widget.ListView;
//import android.widget.SimpleAdapter;
//import android.widget.SimpleCursorAdapter;
//import android.widget.TextView;
//import android.widget.Toast;


// ALEX: Fürs Layout hinzugefügt
//import android.content.Intent;
//import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
//import android.widget.TabHost.TabSpec;


public class MainActivity extends FragmentActivity implements TabHost.OnTabChangeListener
{
	private TabHost m_TabHost;
	private TabInfo m_LastTab = null;
	private HashMap m_MapTabInfo = new HashMap();
	
	//private ArrayAdapter<String> m_ListAdapter = null;
	
	private class TabInfo 
	{
		private String tag;
		private Class clss;
		private Bundle args;
		private Fragment fragment;
		
		TabInfo(String tag, Class clazz, Bundle args) 
		{
			tag = tag;
			clss = clazz;
			args = args;
		}
	}	
	
	 class TabFactory implements TabContentFactory 
	 {
		 private final Context m_Context;
		 /**
		  * @param context
		  */
		 public TabFactory(Context context) 
		 {
			 m_Context = context;
		 }
		 
		 /** (non-Javadoc)
		 * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
		 */
		 public View createTabContent(String tag) 
		 {
			 View v = new View(m_Context);
			 v.setMinimumWidth(0);
			 v.setMinimumHeight(0);
			 return v;
		 }
	 }	
	
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        // Step 1: Inflate layout
        setContentView(R.layout.activity_main);
        // Step 2: Setup TabHost
        initialiseTabHost(savedInstanceState);
        if (savedInstanceState != null) 
        {
        	//set the tab as per the saved state        	
        	m_TabHost.setCurrentTabByTag(savedInstanceState.getString("tab")); 
        }        
       
        
        // statt Layout werden weitere Funktionen aufgerufen, siehe Methodeninhalt
        //displaySharedPreferences();
 
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) 
    {
    	outState.putString("tab", m_TabHost.getCurrentTabTag()); 
		super.onSaveInstanceState(outState);
    }    
    
    private void initialiseTabHost(Bundle savedInstanceState)
    {
        // ALEX: Aufruf der Tabs
    	m_TabHost = (TabHost)findViewById(android.R.id.tabhost);
    	m_TabHost.setup();    	

    	TabInfo tabInfo = null;
    	MainActivity.addTab(this, this.m_TabHost, m_TabHost.newTabSpec("CONTACTS").setIndicator("Contacts"), ( tabInfo = new TabInfo("Contacts", LayoutKontakteActivity.class, savedInstanceState)));
    	m_MapTabInfo.put(tabInfo.tag, tabInfo);
    	MainActivity.addTab(this, this.m_TabHost, m_TabHost.newTabSpec("SENT").setIndicator("Sent"), ( tabInfo = new TabInfo("Sent", LayoutSendedActivity.class, savedInstanceState)));
    	m_MapTabInfo.put(tabInfo.tag, tabInfo);
    	MainActivity.addTab(this, this.m_TabHost, m_TabHost.newTabSpec("RECEIVED").setIndicator("Received"), ( tabInfo = new TabInfo("Received", LayoutReceivedActivity.class, savedInstanceState)));
    	m_MapTabInfo.put(tabInfo.tag, tabInfo);
    	// Default to first tab
    	this.onTabChanged("Contacts");
    	//
    	m_TabHost.setOnTabChangedListener(this);    	
    	
    	
//    	
//    	
//        // Tab for Contacts
//        //-------------------------------
//        TabSpec contactspec = m_TabHost.newTabSpec("CONTACTS");
//        // setting Title and Icon for the Tab
//        contactspec.setIndicator("CONTACTS", getResources().getDrawable(R.drawable.kontakte48x48px));
//        Intent photosIntent = new Intent(this, LayoutKontakteActivity.class);
//        contactspec.setContent(photosIntent);
// 
//        // Tab for Sended Messages
//        //--------------------------------
//        TabSpec sendedspec = tabHost.newTabSpec("SENDED");
//        sendedspec.setIndicator("SENDED", getResources().getDrawable(R.drawable.kontakte48x48px));
//        Intent songsIntent = new Intent(this, LayoutSendedActivity.class);
//        sendedspec.setContent(songsIntent);
//        
//        // Tab for Received Messages
//        //---------------------------------
//        TabSpec receivedspec = tabHost.newTabSpec("RECEIVED");
//        receivedspec.setIndicator("RECEIVED", getResources().getDrawable(R.drawable.kontakte48x48px));
//        Intent videosIntent = new Intent(this, LayoutReceivedActivity.class);
//        receivedspec.setContent(videosIntent);
//         
//        // Adding all TabSpec to TabHost
//        tabHost.addTab(contactspec); // Adding contacts tab
//        tabHost.addTab(sendedspec); // Adding sent Messages tab
//        tabHost.addTab(receivedspec); // Adding received Messages tab
    }
    
    private static void addTab(MainActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) 
    {
    	// Attach a Tab view factory to the spec
    	tabSpec.setContent(activity.new TabFactory(activity));
    	String tag = tabSpec.getTag();
    	
    	// Check to see if we already have a fragment for this tab, probably
    	// from a previously saved state.  If so, deactivate it, because our
    	// initial state is that a tab isn't shown.
    	tabInfo.fragment = activity.getSupportFragmentManager().findFragmentByTag(tag);
    	if (tabInfo.fragment != null && !tabInfo.fragment.isDetached()) 
    	{
    		FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
    		ft.detach(tabInfo.fragment);
    		ft.commit();
    		activity.getSupportFragmentManager().executePendingTransactions();
    	}
    	tabHost.addTab(tabSpec);
    }    
    
    // Neu erzeugter Layout aufruf statt diesen siehe oben beim start aufzurufen
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
		// ALEX: Erstmal weggelassen//displaySharedPreferences();
    }
        
    
    
    // ALEX: Testweise weggelassen
    /*
    // folgendes wird beim starten der App ausgeführt
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
    */
    
    
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


	@Override
	public void onTabChanged(String tabId) 
	{
	    TabInfo newTab = (TabInfo)m_MapTabInfo.get(tabId);
	    if (m_LastTab != newTab) 
	    {
	    	FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
	    	if (m_LastTab != null) 
	    	{
	    		if (m_LastTab.fragment != null) 
	    		{
	    			ft.detach(m_LastTab.fragment);
	    		}
	    	}
		    if (newTab != null) 
		    {
		    	if (newTab.fragment == null) 
		    	{
		    		newTab.fragment = Fragment.instantiate(this, newTab.clss.getName(), newTab.args);
		    		ft.add(R.id.realtabcontent, newTab.fragment, newTab.tag);
		    	} 
		    	else 
		    	{
		    		ft.attach(newTab.fragment);
		    	}
		    }
		    m_LastTab = newTab;
		    ft.commit();
		    getSupportFragmentManager().executePendingTransactions();
	    }		
	}
    
//    @Override
//    protected void onListItemClick(ListView l, View v, int position, long id) 
//    {
//    	String item = (String) getListAdapter().getItem(position);
//    	Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
//    }    
    
}
