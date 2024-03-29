package com.freshtechnology.whereappyou;

import java.util.ArrayList;
import java.util.Date;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;





import android.app.Activity;
import android.os.Bundle;






public class LayoutReceivedActivity extends Activity
{
	
	private ArrayList<String> contactInfoList = new ArrayList<String>();
	private ArrayAdapter<String> m_ListAdapter;

	
	public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.received_layout);

        
		// Lade die Liste mit den Infos
		Loadlist();

    }
	
	

	private void Loadlist()
	{
		
		// Leere die Liste vor dem F�llen
        contactInfoList.clear();
		

		Utils abfrage = new Utils();

		
		
		
		
		// TEST:
		String testeingabe = "Test1    123-123-12";
		// Bef�lle die Liste mit Kontakten
        contactInfoList.add(testeingabe);
		
        String testeingabe2 = "Alexander Luja2  345-345-67";
		// Bef�lle die Liste mit Kontakten
        contactInfoList.add(testeingabe);
        
        String testeingabe3 = "Max Power3  456-678-345";
		// Bef�lle die Liste mit Kontakten
        contactInfoList.add(testeingabe);
        
        String testeingabe4 = "Leonardo Gutierrez4  567-789-78";
		// Bef�lle die Liste mit Kontakten
        contactInfoList.add(testeingabe);
		
		
        
        
		
		// Durchlaufe alle Infos und lade diese in die Listen
		for (Request ausgabe : abfrage.getNotProcessedRequests()) 
		{
			// Abfrage der einzelnen Infos
			Date datum = ausgabe.getDateRecived(); // Datum an dem eine Anfrage eingegangen ist
			String phone = ausgabe.getPhoneNumber(); // Die Telefonnummer des Kontakts
			Boolean fav = ausgabe.isFavContact(); // Beschreib ob es sich im einen Favoriten in Kontekte handelt oder nicht
												  // Idee dahinter wenn Fav dann ohne anfrage antwort m�glich sonst erlaubnis erforderlich
			
			// Anhand der PhoneNo den Namen des Kontakts abfragen
			String name = abfrage.GetName(phone);
			

			// Bef�lle die Liste mit Kontakten
            //contactInfoList.add("Name: " + name + ", Phone: " + phone + ", Datum: " + datum + ", Favorit: " + fav );
            contactInfoList.add("Name: " + name + "    Phone: " + phone);
		}
		
        // F�lle die Received-Liste (beantwortet)
        //-------------------------------------------
        // Liste mit Daten f�llen und Ausw�hlen
        m_ListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contactInfoList);


		// Eine ListView �ber einen ListAdapter bef�llen
        setContentView(R.layout.received_layout);
        ListView detail2 = (ListView)findViewById(R.id.listViewReceived);
        detail2.setAdapter(m_ListAdapter);

        

        // Listener selektiert den entsprechenden Eintrag
        detail2.setOnItemClickListener(new OnItemClickListener() 
        {
            @Override
            public void onItemClick(AdapterView av, View v, int index, long arg3) 
            {

            	Intent intent = new Intent(WhereAppYouApplication.getAppContext(), ShowContactInfos.class);

                //Intent mit den Daten f�llen
            	intent.putExtra("Index", index);
            	intent.putExtra("Infos", "RECEIVED");
            	
            	startActivity(intent);

            }
        });
        
	}
	
}
