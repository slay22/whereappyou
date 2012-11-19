package com.freshtechnology.whereappyou;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity 
{
	TextView textView = null;
	Button btnGetPrefs = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        btnGetPrefs = (Button) findViewById(R.id.btnGetPreferences);
        
        textView = (TextView) findViewById(R.id.txtPrefs);
        View.OnClickListener listener = new View.OnClickListener() 
        {
        	@Override
        	public void onClick(View v) 
        	{
        		switch (v.getId()) 
        		{
        			case R.id.btnGetPreferences:
        	      		displaySharedPreferences();
        	      		break;
        			default:
        				break;
        	   }
        	}
        };        
        	
    	btnGetPrefs.setOnClickListener(listener);        	
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
		startActivity(intent);
    }
        
    private void displaySharedPreferences() 
    {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	
    	boolean usePass = prefs.getBoolean("usePassive", true);
        boolean useNet = prefs.getBoolean("useNetwork", false);
        boolean incognitoMode = prefs.getBoolean("incognitoMode", false);      
        boolean voiceNotifications = prefs.getBoolean("voiceNotifications", false);
        boolean onlyFavs = prefs.getBoolean("onlyFavs", true);
// 	   	long minTime = prefs.getLong("locMinTime", 5 * 60 * 1000); 
// 	   	float minDistance = prefs.getFloat("locMinDistance", 10);
    	
//    	String listPrefs = prefs.getString("listpref", "Default list prefs");

    	StringBuilder builder = new StringBuilder();
    	builder.append("usePassive: " + String.valueOf(usePass) + "\n");
    	builder.append("useNet: " + String.valueOf(useNet) + "\n");
    	builder.append("incognitoMode: " + String.valueOf(incognitoMode) + "\n");
    	builder.append("voiceNotifications: " + String.valueOf(voiceNotifications) + "\n");
    	builder.append("onlyFavs: " + String.valueOf(onlyFavs) + "\n");
//    	builder.append("minTime: " + String.valueOf(minTime) + "\n");    	
//    	builder.append("minDistance: " + String.valueOf(minDistance));
    	//builder.append("List preference: " + listPrefs);
    	
    	textView.setText(builder.toString());
    }    
}
