package com.freshtechnology.whereappyou;


import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;


// Zugriff auf Kontakte auf dem Phone
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;
import android.app.DownloadManager.Query;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.ContactsContract;

// ItemClickListener
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;





import android.app.Activity;
import android.os.Bundle;




// extends FragmentActivity
public class LayoutKontakteActivity extends Activity //implements OnItemClickListener
{
	
	// Zum Befüllen einer Liste
	private ArrayList<String> contactInfoList = new ArrayList<String>();

	// Position des angeklickten Kontakts
	private int curSelectedContactPosition = 1;
	
	
	private RadioGroup radioGroup;
	private RadioButton radioButton;
	
	private Button btnSend;


	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_layout);
        
        // Erzeuge das Event für die RadioButtons und den Send-Button
        addListenerOnButton();
        
        // Lade die Kontake auf dem Phone
        loadContacts();

	      // Erzeuge ArrayAdapter um die Daten der ListView hinzuzufügen
	      ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contactInfoList);
	      
	      
	      // Eine ListView über einen ListAdapter befüllen
	      setContentView(R.layout.contact_layout);
	      ListView detail2 = (ListView)findViewById(R.id.thecontacts);
	      detail2.setAdapter(adapter);
      
    }
	
	
	// Lade die Kontakte auf dem Phone
	private void loadContacts() 
	{
		// Der getContentResolver() benötigt implements FragmentActivity
		
		//contactInfoList.clear();
		
	    ContentResolver cr = getContentResolver();
	    Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
	    
	    
	    if (cur.getCount() > 0) 
	    {
	        while (cur.moveToNext()) 
	        {
	            String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
	            String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
	            if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) 
	            {
	                 Cursor pCur = cr.query(
	                         ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
	                         null, 
	                         ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
	                         new String[]{id}, null);
	                 
	                 while (pCur.moveToNext()) 
	                 {
	                     String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//	                     Toast.makeText(LayoutKontakteActivity.this, "Name: " + name + ", Phone No: " + phoneNo, Toast.LENGTH_SHORT).show();
//	                     
	                     // Befülle die Liste mit Kontakten
	                     contactInfoList.add("Name:" + name + ", Phone:" + phoneNo);
	
	                 } 
	                 pCur.close();
	            }
	        }
	    }
	}
	
	
	
    // RadioButtons und Send-Button Listener
    public void addListenerOnButton() 
    {
    	// Erzeuge RadioButtons und Send-Button
    	radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
    	btnSend = (Button) findViewById(R.id.button1);
    	
    	
    	// Ein Image hinzufügen
    	//btnSend.setImageResource(R.id.button1, android.R.drawable.star_big_on);
    	
    	
    	btnSend.setOnClickListener(new OnClickListener() 
    	{
    		@Override
    		public void onClick(View v) 
    		{
    			// nimm den selektierten RadioButton
    			int selectedId = radioGroup.getCheckedRadioButtonId();
     
    			// Erzeuge den RadioButton
    		    radioButton = (RadioButton) findViewById(selectedId);

    		    // Erzeuge das Event das ausgeführt werden soll
    			Toast.makeText(LayoutKontakteActivity.this, radioButton.getText(), Toast.LENGTH_SHORT).show();
    		}
    	});
      }
	


	
	

    
    
//	 SimpleCursorAdapter mAdapter;
//	    MatrixCursor mMatrixCursor;
//	 
//	    
//	    
//	    @Override
//	    public void onCreate(Bundle savedInstanceState) 
//	    {
//	        super.onCreate(savedInstanceState);
//	        setContentView(R.layout.contact_layout);
//	 
//	        // The contacts from the contacts content provider is stored in this cursor
//	        mMatrixCursor = new MatrixCursor(new String[] { "_id","name"} );
//	 
//	        // Adapter to set data in the listview
//	        mAdapter = new SimpleCursorAdapter(getBaseContext(),
//	            R.layout.contact_layout,
//	            null,
//	            new String[] { "name"},
//	            new int[] { R.id.name}, 0);
//	 
//	        // Getting reference to listview
//	        ListView lstContacts = (ListView) findViewById(R.id.thecontacts);
//	 
//	        // Setting the adapter to listview
//	        lstContacts.setAdapter(mAdapter);
//	 
//	        // Creating an AsyncTask object to retrieve and load listview with contacts
//	        ListViewContactsLoader listViewContactsLoader = new ListViewContactsLoader();
//	 
//	        // Starting the AsyncTask process to retrieve and load listview with contacts
//	        listViewContactsLoader.execute();
//	    }
//	 
//	    /** An AsyncTask class to retrieve and load listview with contacts */
//	    private class ListViewContactsLoader extends AsyncTask<Void, Void, Cursor>
//	    {
//	 
//	        @Override
//	        protected Cursor doInBackground(Void... params) {
//	            Uri contactsUri = ContactsContract.Contacts.CONTENT_URI;
//	 
//	            // Querying the table ContactsContract.Contacts to retrieve all the contacts
//	            Cursor contactsCursor = getContentResolver().query(contactsUri, null, null, null,
//	            ContactsContract.Contacts.DISPLAY_NAME + " ASC ");
//	 
//	            if(contactsCursor.moveToFirst()){
//	                do{
//	                    long contactId = contactsCursor.getLong(contactsCursor.getColumnIndex("_ID"));
//	 
//	                    Uri dataUri = ContactsContract.Data.CONTENT_URI;
//	 
//	                    // Querying the table ContactsContract.Data to retrieve individual items like
//	                    // home phone, mobile phone, work email etc corresponding to each contact
//	                    Cursor dataCursor = getContentResolver().query(dataUri, null,
//	                                        ContactsContract.Data.CONTACT_ID + "=" + contactId,
//	                                        null, null);
//	 
//	                    String displayName="";
//	                    String nickName="";
//	                    String homePhone="";
//	                    String mobilePhone="";
//	                    String workPhone="";
//	                    String photoPath="" + R.drawable.contact_pic;
//	                    byte[] photoByte=null;
//	                    String homeEmail="";
//	                    String workEmail="";
//	                    String companyName="";
//	                    String title="";
//	 
//	                    if(dataCursor.moveToFirst()){
//	                        // Getting Display Name
//	                        displayName = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME ));
//	                        do{
//	 
//	                            // Getting NickName
//	                            if(dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE))
//	                                nickName = dataCursor.getString(dataCursor.getColumnIndex("data1"));
//	 
//	                            // Getting Phone numbers
//	                            if(dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)){
//	                                switch(dataCursor.getInt(dataCursor.getColumnIndex("data2"))){
//	                                    case ContactsContract.CommonDataKinds.Phone.TYPE_HOME :
//	                                        homePhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
//	                                        break;
//	                                    case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE :
//	                                        mobilePhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
//	                                        break;
//	                                    case ContactsContract.CommonDataKinds.Phone.TYPE_WORK :
//	                                        workPhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
//	                                        break;
//	                                }
//	                            }
//	 
//	                            // Getting EMails
//	                            if(dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE ) ) {
//	                                switch(dataCursor.getInt(dataCursor.getColumnIndex("data2"))){
//	                                    case ContactsContract.CommonDataKinds.Email.TYPE_HOME :
//	                                        homeEmail = dataCursor.getString(dataCursor.getColumnIndex("data1"));
//	                                        break;
//	                                    case ContactsContract.CommonDataKinds.Email.TYPE_WORK :
//	                                        workEmail = dataCursor.getString(dataCursor.getColumnIndex("data1"));
//	                                        break;
//	                                }
//	                            }
//	 
//	                            // Getting Organization details
//	                            if(dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)){
//	                                companyName = dataCursor.getString(dataCursor.getColumnIndex("data1"));
//	                                title = dataCursor.getString(dataCursor.getColumnIndex("data4"));
//	                            }
//	 
//	                            // Getting Photo
//	                            if(dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)){
//	                                photoByte = dataCursor.getBlob(dataCursor.getColumnIndex("data15"));
//	 
//	                                if(photoByte != null) {
//	                                    Bitmap bitmap = BitmapFactory.decodeByteArray(photoByte, 0, photoByte.length);
//	 
//	                                    // Getting Caching directory
//	                                    File cacheDirectory = getBaseContext().getCacheDir();
//	 
//	                                    // Temporary file to store the contact image
//	                                    File tmpFile = new File(cacheDirectory.getPath() + "/wpta_"+contactId+".png");
//	 
//	                                    // The FileOutputStream to the temporary file
//	                                    try {
//	                                        FileOutputStream fOutStream = new FileOutputStream(tmpFile);
//	 
//	                                        // Writing the bitmap to the temporary file as png file
//	                                        bitmap.compress(Bitmap.CompressFormat.PNG,100, fOutStream);
//	 
//	                                        // Flush the FileOutputStream
//	                                        fOutStream.flush();
//	 
//	                                        //Close the FileOutputStream
//	                                        fOutStream.close();
//	 
//	                                    } catch (Exception e) {
//	                                        e.printStackTrace();
//	                                    }
//	                                    photoPath = tmpFile.getPath();
//	                                }
//	                            }
//	                        }while(dataCursor.moveToNext());
//	                        String details = "";
//	 
//	                        // Concatenating various information to single string
//	                        if(homePhone != null && !homePhone.equals("") )
//	                            details = "HomePhone : " + homePhone + "\n";
//	                        if(mobilePhone != null && !mobilePhone.equals("") )
//	                            details += "MobilePhone : " + mobilePhone + "\n";
//	                        if(workPhone != null && !workPhone.equals("") )
//	                            details += "WorkPhone : " + workPhone + "\n";
//	                        if(nickName != null && !nickName.equals("") )
//	                            details += "NickName : " + nickName + "\n";
//	                        if(homeEmail != null && !homeEmail.equals("") )
//	                            details += "HomeEmail : " + homeEmail + "\n";
//	                        if(workEmail != null && !workEmail.equals("") )
//	                            details += "WorkEmail : " + workEmail + "\n";
//	                        if(companyName != null && !companyName.equals("") )
//	                            details += "CompanyName : " + companyName + "\n";
//	                        if(title != null && !title.equals("") )
//	                            details += "Title : " + title + "\n";
//	 
//	                        // Adding id, display name, path to photo and other details to cursor
//	                        mMatrixCursor.addRow(new Object[]{ Long.toString(contactId),displayName,photoPath,details});
//	                    }
//	                }while(contactsCursor.moveToNext());
//	            }
//	            return mMatrixCursor;
//	        }
//	 
//	        @Override
//	        protected void onPostExecute(Cursor result) 
//	        {
//	            // Setting the cursor containing contacts to listview
//	            mAdapter.swapCursor(result);
//	        }
//	    }
//	 
//	    @Override
//	    public boolean onCreateOptionsMenu(Menu menu) 
//	    {
//	        getMenuInflater().inflate(R.menu.activity_main, menu);
//	        return true;
//	    }

	
	
	
}
