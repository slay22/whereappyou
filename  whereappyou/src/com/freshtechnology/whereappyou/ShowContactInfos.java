package com.freshtechnology.whereappyou;

import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;




@SuppressLint("ValidFragment")
public class ShowContactInfos extends Activity
{
	
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
	   super.onCreate(savedInstanceState);
       setContentView(R.layout.contact_infos);
       

       // Lese die in der aufrufenden Klasse übergeben Daten aus
       TextView Name = (TextView) findViewById(R.id.name);
       TextView Phone = (TextView) findViewById(R.id.telefon);
       TextView Datum = (TextView) findViewById(R.id.datum);
       
       
       Intent i = getIntent();
       // Receiving the Data
       int index = i.getIntExtra("Index", 0);
       //String index = i.getStringExtra("Index");
       String infos = i.getStringExtra("Infos");
       

       Utils abfrage = new Utils();
       
       
       // je nachdem aus welcher Klasse die Daten sind sollen hier entsprechend die Daten abfegragt werden
       if(infos.equals("SENT"))
       {
    	   
           int count = 0;
           
	   	    // Durchlaufe alle Infos und lade diese in die Listen
	   		for (Request ausgabe : abfrage.getProcessedRequests()) 
	   		{
	   			
	   			if(count == index)
	   			{
	   				// Abfrage der einzelnen Infos
	   				Date datum = ausgabe.getDateRecived(); // Datum an dem eine Anfrage eingegangen ist
	   				String phone = ausgabe.getPhoneNumber(); // Die Telefonnummer des Kontakts
	   				Boolean fav = ausgabe.isFavContact(); // Beschreib ob es sich im einen Favoriten in Kontekte handelt oder nicht
	   													  // Idee dahinter wenn Fav dann ohne anfrage antwort möglich sonst erlaubnis erforderlich
	   				
	   				// Anhand der PhoneNo den Namen des Kontakts abfragen
	   				String name = abfrage.GetName(phone);
	   				
	   				
	   				Name.setText(name);
	   			    Phone.setText(phone);
	   			    Datum.setText(datum.toString());
	   				
	   			}
	   			
	   			count++;
	   			
	   		}
       }
       else if(infos.equals("RECEIVED"))
       {
           int count = 0;
           
	   	    // Durchlaufe alle Infos und lade diese in die Listen
	   		for (Request ausgabe : abfrage.getNotProcessedRequests()) 
	   		{
	   			
	   			if(count == index)
	   			{
	   				// Abfrage der einzelnen Infos
	   				Date datum = ausgabe.getDateRecived(); // Datum an dem eine Anfrage eingegangen ist
	   				String phone = ausgabe.getPhoneNumber(); // Die Telefonnummer des Kontakts
	   				Boolean fav = ausgabe.isFavContact(); // Beschreib ob es sich im einen Favoriten in Kontekte handelt oder nicht
	   													  // Idee dahinter wenn Fav dann ohne anfrage antwort möglich sonst erlaubnis erforderlich
	   				
	   				// Anhand der PhoneNo den Namen des Kontakts abfragen
	   				String name = abfrage.GetName(phone);
	   				
	   				
	   				Name.setText(name);
	   			    Phone.setText(phone);
	   			    Datum.setText(datum.toString());
	   				
	   			}
	   			
	   			count++;
	   			
	   		}
       }
       
       
       
       // TEST
       String text1 = "Alexander Luja";
       String text2 = "121-456-678";
       Date d = new Date();
       Date text3 = d;
       
       Name.setText(text1);
	   Phone.setText(text2);
	   Datum.setText(text3.toString());

   }
   
}
