package com.freshtechnology.whereappyou;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class WhereAppYouDatabaseHelper extends SQLiteOpenHelper
{
	 //The Android's default system path of your application database.
	
	private static String DB_PATH = "/data/data/%s/databases/";
	private static String DB_NAME = "whereappyou";
	 
	private SQLiteDatabase m_DataBase;
	private Context m_Context;
	
	
	public static final String KEY_ROWID = "_id";
	public static final String KEY_DATE = "Date";
	public static final String KEY_NUMBER = "Number";
	public static final String KEY_PROCESSED = "processed";
	
	private String getDatabasePath()
	{
		return String.format("%s%s", String.format(DB_PATH, m_Context.getPackageName()), DB_NAME);
	}
	
	/**
	  * Constructor
	  * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
	  * @param context
	  */
	public WhereAppYouDatabaseHelper(Context context) 
	{
		super(context, DB_NAME, null, 1);
		m_Context = context;
	}
	
	@Override
	public void onCreate(SQLiteDatabase arg0) 
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		// TODO Auto-generated method stub
	}	
	
	 @Override
	 public synchronized void close() 
	 {
		 if(m_DataBase != null)
			 m_DataBase.close();
	  
		 super.close();
	 }	
	
	/**
	 * Creates a empty database on the system and rewrites it with the "assets" database.
	 **/
	public void createDataBase() throws IOException
	{
	 
		boolean dbExist = checkDataBase();
	 
		if(!dbExist)
		{
			//An empty database will be created into the default system path.
			this.getReadableDatabase();
			
			try 
			{
				copyDataBase();
			} 
			catch (IOException e) 
			{
				throw new Error("Error copying database");
			}
		}
	}	
	
	/**
	 * Check if the database already exist to avoid re-copying the file each time the application it's opened.
	 * @return true if it exists, false if it doesn't
	*/
	private boolean checkDataBase()
	{
		SQLiteDatabase checkDB = null;
	 
		try
		{
			String path = getDatabasePath();
			checkDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
		}
		catch(SQLiteException e)
		{
			//database does't exist yet.
		}
		
		if(checkDB != null)
		{
			checkDB.close();
		}
		
		return checkDB != null ? true : false;
	}
	 
	/**
	 * Copies your database from the local assets-folder to the just created empty database in the
	 * system folder, from where it can be accessed and handled.
	 **/
	private void copyDataBase() throws IOException
	{
		InputStream input = m_Context.getAssets().open(DB_NAME);
	 
		String outFileName = getDatabasePath();
	 
		OutputStream output = new FileOutputStream(outFileName);
	 
		byte[] buffer = new byte[1024];
		int length = 0;
		while ((length = input.read(buffer))>0)
		{
			output.write(buffer, 0, length);
		}
	 
		output.flush();
		output.close();
		input.close();
	}
	 
	public void openDataBase() throws SQLException
	{
		String path = DB_PATH + DB_NAME;
		m_DataBase = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
	}	
	
	public boolean insertNewRequest(String phoneNumber, String payload)
	{
		Date today = new Date();
		
		ContentValues values = new ContentValues();
		values.put(KEY_DATE, DateFormat.getDateTimeInstance().format(today));
		values.put(KEY_NUMBER, phoneNumber);
		values.put(KEY_PROCESSED, false);
		
		return (m_DataBase.insert(DB_NAME, null, values) > -1);
	}
	
}
