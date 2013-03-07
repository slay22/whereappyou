package com.freshtechnology.whereappyou;

import android.content.ContentProvider;
//import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class RequestsContentProvider extends ContentProvider
{
	public static final Uri CONTENT_URI = Uri.parse("content://com.freshtechnology.provider.requests/requests");

	private SQLiteDatabase m_Database;	
	
	private static final int REQUESTS = 1;
	private static final int REQUEST_ID = 2;
	
	private static final UriMatcher uriMatcher;
	static 
	{
	   uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	   uriMatcher.addURI("com.freshtechnology.provider.requests", "requests", REQUESTS);
	   uriMatcher.addURI("com.freshtechnology.provider.requests", "requests/*", REQUEST_ID);
	}
	
	@Override
	public boolean onCreate() 
	{
        try 
        {
        	//WhereAppYouDatabaseHelper _DataHelper = new WhereAppYouDatabaseHelper(WhereAppYouApplication.getAppContext());
        	WhereAppYouDatabaseHelper _DataHelper = new WhereAppYouDatabaseHelper(getContext());
        	
        	m_Database = _DataHelper.getWritableDatabase();
        } 
        catch (Exception e) 
        {
			e.printStackTrace();
        }
		
		return (null != m_Database);
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) 
	{
		int _count;
		   
		switch (uriMatcher.match(uri)) 
		{
			case REQUESTS:
				_count = m_Database.delete(WhereAppYouDatabaseHelper.TABLE_NAME, where, whereArgs);
		        break;

			case REQUEST_ID:
		        String segment = uri.getPathSegments().get(1);
		        String _where = WhereAppYouDatabaseHelper.KEY_ROWID + "=" + segment + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
		        _count = m_Database.delete(WhereAppYouDatabaseHelper.TABLE_NAME, _where, whereArgs);
		        break;

		      default: throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		
		return _count;
	}

	@Override
	public String getType(Uri uri) 
	{
		String _result = "";
		
		switch (uriMatcher.match(uri)) 
		{
			case REQUESTS : 
				_result = "vnd.android.cursor.dir/vnd.freshtechnology.requests";
				break;
		    case REQUEST_ID: 
		    	_result = "vnd.android.cursor.item/vnd.freshtechnology.requests";
		    	break;
		}
		
		if (_result == "")
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		
		return _result;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) 
	{
		Uri _result = null;
		
		//Insert the new row, will return the row number if successful.
		long _rowID = m_Database.insert(WhereAppYouDatabaseHelper.TABLE_NAME, "nullhack", values);
	         
	    //Return a URI to the newly inserted row on success.
	    if (_rowID > 0) {
	      _result = ContentUris.withAppendedId(CONTENT_URI, _rowID);
	      getContext().getContentResolver().notifyChange(_result, null);
	    }
	    else
	    	throw new SQLException("Failed to insert row into " + _result);
		
	    return _result;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) 
	{
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		qb.setTables(WhereAppYouDatabaseHelper.TABLE_NAME);

		//If this is a row query, limit the result set to the passed in row.
		switch (uriMatcher.match(uri)) 
		{
		      case REQUEST_ID : 
		    	  qb.appendWhere(WhereAppYouDatabaseHelper.KEY_ROWID + "=" + uri.getPathSegments().get(1));
		          break;
		      default: 
		    	  break;
		}

		//If no sort order is specified sort by date / time
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) 
		{
			orderBy = WhereAppYouDatabaseHelper.KEY_DATE + " ASC";
		} 
		else 
		{
			orderBy = sortOrder;
		}

		//Apply the query to the underlying database.
		Cursor cursor = qb.query(m_Database, projection, selection, selectionArgs, null, null, orderBy);

		//Register the contexts ContentResolver to be notified if the cursor result set changes.
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		   
		//Return a cursor to the query result.
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) 
	{
		int _count;
		switch (uriMatcher.match(uri)) 
		{
		      case REQUESTS: 
		    	  _count = m_Database.update(WhereAppYouDatabaseHelper.TABLE_NAME, values, selection, selectionArgs);
		    	  break;

		      case REQUEST_ID: 
		    	  String segment = uri.getPathSegments().get(1);
		    	  String where = WhereAppYouDatabaseHelper.KEY_ROWID + "=" + segment + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
		          _count = m_Database.update(WhereAppYouDatabaseHelper.TABLE_NAME, values, where, selectionArgs);
		          break;

		      default: 
		    	  throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		
		return _count;
	}

	
	
}
