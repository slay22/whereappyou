package com.freshtechnology.whereappyou;

import java.util.ArrayList;
import java.util.Date;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;





import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;







public class LayoutSentActivity extends Activity
{
	
	private ArrayList<String> contactInfoList = new ArrayList<String>();
	
	private ArrayAdapter<String> m_ListAdapter;
	
	
	
	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sent_layout);
        

 	    Loadlist();
 		
    }

	
	private void Loadlist()
	{
		
		// Leere die Liste vor dem Füllen
        contactInfoList.clear();
		

		Utils abfrage = new Utils();

		
		// TEST:
		// --------------------------------------------
		String testeingabe = "Leonardo Gutierrez    12345-6789-112";
		// Befülle die Liste mit Kontakten
        contactInfoList.add(testeingabe);
		
        

		// Die noch nicht abgewickelten Processes (die ausstehenden Anfragen)
		for (Request ausgabe : abfrage.getProcessedRequests()) 
		{
			
			// Abfrage der einzelnen Infos
			Date datum = ausgabe.getDateRecived(); // Datum an dem eine Anfrage eingegangen ist
			int id = ausgabe.getRowId(); // Die ID der Anfrage
			String phone = ausgabe.getPhoneNumber(); // Die Telefonnummer des Kontakts
			Boolean processed = ausgabe.getProcessed(); // Ob die Anfrage beantwortet wurde oder nicht
			String pay = ausgabe.getPayLoad(); // ???????????
			Boolean fav = ausgabe.isFavContact(); // Beschreib ob es sich im einen Favoriten in Kontekte handelt oder nicht
												  // Idee dahinter wenn Fav dann ohne anfrage antwort möglich sonst erlaubnis erforderlich
			
			// Anhand der PhoneNo den Namen des Kontakts abfragen
			String name = abfrage.GetName(phone);
			
			// Befülle die Liste mit Kontakten
			//contactInfoList.add("Name: " + name + ", Phone: " + phone + ", Datum: " + datum + ", Favorit: " + fav );
			contactInfoList.add("Name: " + name + "    Phone: " + phone);
			
		}
        
		
		// Fülle die Sent-Liste (beantwortet)
        //-------------------------------------------
        // Liste mit Daten füllen und Auswählen
        m_ListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contactInfoList);

        
		// Eine ListView über einen ListAdapter befüllen
        setContentView(R.layout.sent_layout);
        ListView detail2 = (ListView)findViewById(R.id.listViewSent);
        detail2.setAdapter(m_ListAdapter);

        
        
        // Listener selektiert den entsprechenden Eintrag
        detail2.setOnItemClickListener(new OnItemClickListener() 
        {
            @Override
            public void onItemClick(AdapterView av, View v, int index, long arg3) 
            {

            	Intent intent = new Intent(WhereAppYouApplication.getAppContext(), ShowContactInfos.class);

                //Intent mit den Daten füllen
            	intent.putExtra("Index", index);
            	intent.putExtra("Infos", "SENT");
            	
            	startActivity(intent);

            }
        });

        

	}
	
	
}
