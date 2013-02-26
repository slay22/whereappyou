package com.freshtechnology.whereappyou;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class WhereAppYouDatabaseHelper extends SQLiteOpenHelper
{
	 //The Android's default system path of your application database.
	
	private static String DB_PATH = "/data/data/%s/databases/";
	private static String DB_NAME = "whereappyou";
	private static String TABLE_NAME = "requests";
	private static final int DATABASE_VERSION = 1;
	//private SQLiteDatabase m_DataBase;
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
		super(context, DB_NAME, null, DATABASE_VERSION);
		m_Context = context;
//		
//		try
//		{
//			createDataBase();
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		String CREATE_REQUESTS_TABLE = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s TEXT, %s TEXT, %s NUMERIC)", TABLE_NAME, KEY_ROWID, KEY_DATE, KEY_NUMBER, KEY_PROCESSED);		
        db.execSQL(CREATE_REQUESTS_TABLE);		
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", TABLE_NAME));
        onCreate(db);	
	}	
	
	 @Override
	 public synchronized void close() 
	 {
//		 if(m_DataBase != null)
//			 m_DataBase.close();
	  
		 super.close();
	 }	
	
	/**
	 * Creates a empty database on the system and rewrites it with the "assets" database.
	 **/
	@SuppressWarnings("unused")
	private void createDataBase() throws IOException
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
//		if (null == m_DataBase || (null != m_DataBase &&  !m_DataBase.isOpen()) 
//				m_Database =  this.getWritableDatabase();
	}	
	
	public boolean insertNewRequest(String phoneNumber, String payload)
	{
		Date today = new Date();
		
		ContentValues values = new ContentValues();
		values.put(KEY_DATE, DateFormat.getDateTimeInstance().format(today));
		values.put(KEY_NUMBER, phoneNumber);
		values.put(KEY_PROCESSED, false);
		
		SQLiteDatabase m_DataBase = this.getWritableDatabase();
		boolean result = (m_DataBase.insert(TABLE_NAME, null, values) > -1);
		m_DataBase.close();
		
		return result;
	}

	public boolean updateRequest(String phoneNumber)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_PROCESSED, true);

		SQLiteDatabase m_DataBase = this.getWritableDatabase();
		boolean result = (1 == m_DataBase.update(TABLE_NAME, values, KEY_NUMBER + " = ?", new String[] { phoneNumber }));
		m_DataBase.close();
		return result; 
	}
	
	public Cursor getNotProcessedRequests()
	{
		String[] columns = new String[] { KEY_ROWID, KEY_DATE, KEY_NUMBER, KEY_PROCESSED };
		String[] values = new String[] { String.valueOf(false) }; 

		SQLiteDatabase m_DataBase = this.getWritableDatabase();
		Cursor result = m_DataBase.query(TABLE_NAME, columns, KEY_PROCESSED + "=?", values, null, null, KEY_DATE);
		m_DataBase.close();
		return result; 
	}
}
