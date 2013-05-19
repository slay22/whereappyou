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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
//import android.app.Activity;

//import android.app.ListActivity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
//import android.widget.SimpleAdapter;


// Für Tabs benötigt
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
// ALEX: Fürs Layout hinzugefügt
//import android.content.Intent;
//import android.os.Bundle;
import android.os.StrictMode;
import android.widget.TabHost;

// Für den ContenResolver zum laden der Kontakte auf dem phone
import android.content.ContentResolver;
import android.widget.Toast;

// Wird für button benötigt
import android.view.View.OnClickListener;
import android.widget.Button;

// Wird für RadioButtons benötigt
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;

// Wird für Spinner (ComboBox) benötigt
import android.widget.Spinner;

// Wird für das erzeugen eines Layouts benötigt
import android.widget.LinearLayout;

// Wird für das erstellen eines Menüs benötigt
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


import android.widget.TabHost;
import android.widget.TabHost.TabSpec;



//public class MainActivity extends FragmentActivity implements TabHost.OnTabChangeListener
public class MainActivity extends TabActivity
{

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         
        
        
        // Erzeugt die Tabs
        //--------------------------
        
        TabHost tabHost = getTabHost();
         
        // Tab für die Kontakte
        TabSpec contactspec = tabHost.newTabSpec("Contacts");
        // Setze den Titel und das Icon
        contactspec.setIndicator("Contacts", getResources().getDrawable(R.drawable.kontakte48x48px));
        Intent contactsIntent = new Intent(this, LayoutKontakteActivity.class);
        contactspec.setContent(contactsIntent);
         
        
        // Tab für die empfangenen Nachrichten
        TabSpec receivedspec = tabHost.newTabSpec("Received");       
        receivedspec.setIndicator("Received", getResources().getDrawable(R.drawable.kontakte48x48px));
        Intent receivedIntent = new Intent(this, LayoutReceivedActivity.class);
        receivedspec.setContent(receivedIntent);
         
        
        // Tab für die gesendeten Nachrichten
        TabSpec sentspec = tabHost.newTabSpec("Sent");
        sentspec.setIndicator("Sent", getResources().getDrawable(R.drawable.kontakte48x48px));
        Intent sentIntent = new Intent(this, LayoutSentActivity.class);
        sentspec.setContent(sentIntent);
         
        
        
        // Für die TabSpec dem TabHost hinzu
        tabHost.addTab(contactspec);
        tabHost.addTab(receivedspec);
        tabHost.addTab(sentspec);
    }

    private HashMap<String, String> putData(String name, String purpose) 
    {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("name", name);
        item.put("purpose", purpose);
        return item;
      }




	// Erzeuge ein Menü für den Aufruf der Einstellungen
    //--------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		return true;
	}
	
	// Aufruf der einzelnen Menüpunkte
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{

		Intent intent = null;
		
		switch (item.getItemId()) 
		{
			case R.id.settings:
				intent = new Intent(WhereAppYouApplication.getAppContext(), WhereAppYouPrefsActivity.class);
				startActivity(intent);
				return true;
			
		    case R.id.refresh:
		         Toast.makeText(WhereAppYouApplication.getAppContext(), "Aktualisiert", Toast.LENGTH_SHORT).show();
		         //this.reload();
		         intent = new Intent(WhereAppYouApplication.getAppContext(), MainActivity.class);
		         startActivity(intent);
		         return true;
		         
		    case R.id.addcontact:
		         Toast.makeText(WhereAppYouApplication.getAppContext(), "Kontakte ausgewählt", Toast.LENGTH_SHORT).show();
		         //addContact();
		         return true;
			
			default:
				return super.onOptionsItemSelected(item);
		}
	}
    
    
	
//	private void addContact()
//	{
//		
//		// Add listener so your activity gets called back upon completion of action,
//		// in this case with ability to get handle to newly added contact
//		this.addActivityListener(someActivityListener);
//
//		Intent intent = new Intent(Intent.ACTION_INSERT);
//		intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
//
//		// Just two examples of information you can send to pre-fill out data for the
//		// user.  See android.provider.ContactsContract.Intents.Insert for the complete
//		// list.
//		intent.putExtra(ContactsContract.Intents.Insert.NAME, "some Contact Name");
//		intent.putExtra(ContactsContract.Intents.Insert.PHONE, "some Phone Number");
//
//		// Send with it a unique request code, so when you get called back, you can
//		// check to make sure it is from the intent you launched (ideally should be
//		// some public static final so receiver can check against it)
//		int PICK_CONTACT = 100;
//		this.startActivityForResult(intent, PICK_CONTACT);
//		
//		
//
//		
//	}
	
}
