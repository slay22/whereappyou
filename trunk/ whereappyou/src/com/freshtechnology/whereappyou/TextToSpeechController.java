package com.freshtechnology.whereappyou;

import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

public class TextToSpeechController implements OnInitListener 
{
	private boolean m_ttsInitialized = false;
    private TextToSpeech m_Tts;
    private Context m_Context;

    private static TextToSpeechController singleton;

    public static TextToSpeechController getInstance(Context context) 
    {
        if (singleton == null)
            singleton = new TextToSpeechController(context);
        
        return singleton;
    }

    private TextToSpeechController(Context context) 
    {
    	m_Context = context;
    	
    	m_Tts = new TextToSpeech(m_Context, this);
    }
    
    public void speak(String text) 
    {
        if (null != m_Tts && m_ttsInitialized) 
        {
        	m_Tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);        	
        }
    }

	@Override
	public void onInit(int status) 
	{
		if (status == TextToSpeech.SUCCESS) 
		{
			int result = m_Tts.setLanguage(Locale.getDefault());

			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) 
			{
				Log.e("WhereAppYouService", "TTS : This Language is not supported");
			} 
			else 
			{
				m_ttsInitialized = true;
			}
			} 
		else 
		{
			Log.e("WhereAppYouService", "TTS : Initilization Failed!");
		}			
	}


    @Override
    protected void finalize() throws Throwable 
    {
    	stop();
	       
 	   super.finalize();
    }
	
    public void stop() 
    {
		if (m_Tts != null) 
		{
			m_Tts.stop();
			m_Tts.shutdown();
		}
    }

}
