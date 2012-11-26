package com.freshtechnology.whereappyou;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
		Intent intent = new Intent(getBaseContext(), WhereAppYouPrefsActivity.class);
		int requestCode = 0; 
		startActivityForResult(intent, requestCode);
		displaySharedPreferences();
    }
        
    private void displaySharedPreferences() 
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	
        boolean incognitoMode = prefs.getBoolean("incognitoMode", false);      
        boolean voiceNotifications = prefs.getBoolean("voiceNotifications", false);
        boolean onlyFavs = prefs.getBoolean("onlyFavs", true);
        boolean respWhenLocAvailable = prefs.getBoolean("respWhenLocAvailable", false);        
        
        String[] values = new String[] {"Answer When Location Available " + String.valueOf(respWhenLocAvailable), 
        		"Incognito Mode " + String.valueOf(incognitoMode), "Voice Notifications " + String.valueOf(voiceNotifications), 
        		"Answer ONLY to favorites " + String.valueOf(onlyFavs) };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
        setListAdapter(adapter);        
        
// 	   	long minTime = prefs.getLong("locMinTime", 5 * 60 * 1000); 
// 	   	float minDistance = prefs.getFloat("locMinDistance", 10);
    	
//    	String listPrefs = prefs.getString("listpref", "Default list prefs");

//    	StringBuilder builder = new StringBuilder();
//    	builder.append("usePassive: " + String.valueOf(usePass) + "\n");
//    	builder.append("useNet: " + String.valueOf(useNet) + "\n");
//    	builder.append("incognitoMode: " + String.valueOf(incognitoMode) + "\n");
//    	builder.append("voiceNotifications: " + String.valueOf(voiceNotifications) + "\n");
//    	builder.append("onlyFavs: " + String.valueOf(onlyFavs) + "\n");
//    	builder.append("minTime: " + String.valueOf(minTime) + "\n");    	
//    	builder.append("minDistance: " + String.valueOf(minDistance));
    	//builder.append("List preference: " + listPrefs);
    	
//    	textView.setText(builder.toString());
    }
    
//    @Override
//    protected void onListItemClick(ListView l, View v, int position, long id) 
//    {
//    	String item = (String) getListAdapter().getItem(position);
//    	Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
//    }    
    
}
