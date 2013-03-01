package com.freshtechnology.whereappyou;

import java.text.DateFormat;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class Request implements Parcelable 
{
	private int m_RowID;
	private Date m_DateReceived;
	private String m_PhoneNumber;
	private boolean m_Processed;
	private String m_PayLoad;
	
	public static final Parcelable.Creator<Request> CREATOR = new Parcelable.Creator<Request>() 
    {
		@Override
		public Request createFromParcel(Parcel source) 
		{
			return new Request(source);  
		}
		 
		@Override
		public Request[] newArray(int size) 
		{
			return new Request[size];
		}
	};	
	
	public Request(int rowID, Date dateReceived, String phoneNumber, boolean processed)
	{
		m_RowID = rowID;
		m_DateReceived = dateReceived;
		m_PhoneNumber = phoneNumber;
		m_Processed = processed;
		m_PayLoad = "";
	}

	public Request(String phoneNumber, String payLoad)
	{
		Date today = new Date();
		try
		{
			m_RowID = 0;
			m_DateReceived = DateFormat.getDateTimeInstance().parse(today.toString());
			m_PhoneNumber = phoneNumber;
			m_Processed = false;
			m_PayLoad = payLoad;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//Parcelable constructor
	public Request(Parcel in)
	{
		String[] data = new String[5];
	 
		in.readStringArray(data);
		
		try
		{
			m_RowID = Integer.parseInt(data[0]);
			m_DateReceived =  DateFormat.getDateTimeInstance().parse(data[1]);
			m_PhoneNumber = data[2];
			m_Processed = Boolean.parseBoolean(data[3]); 
			m_PayLoad = data[4];
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}	
	
	public int getRowId()
	{
		return m_RowID;
	}
	
	public Date getDateRecived()
	{
		return m_DateReceived;
	}

	public String getPhoneNumber()
	{
		return m_PhoneNumber;
	}
	
	public boolean getProcessed()
	{
		return m_Processed;
	}
	
	public String getPayLoad()
	{
		return m_PayLoad;
	}
	
	public boolean isFavContact() 
	{
		boolean result = false;
		
		if ("" != m_PhoneNumber)
		{
			result = Utils.isFavContact(m_PhoneNumber);
		}
		
		return result;
	}

	@Override
	public int describeContents() 
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) 
	{
		String _date = "";
		
		if (null != m_DateReceived)
			_date = m_DateReceived.toString();
		
		dest.writeStringArray(new String[]{ String.valueOf(m_RowID), _date, m_PhoneNumber, String.valueOf(m_Processed), m_PayLoad });		
	}
}
