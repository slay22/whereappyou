package com.freshtechnology.whereappyou;

import java.util.Date;

public class Request 
{
	private int m_RowID;
	private Date m_DateReceived;
	private String m_PhoneNumber;
	private boolean m_Processed;
	private String m_PayLoad;
	
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
		m_RowID = 0;
		m_DateReceived = null;
		m_PhoneNumber = phoneNumber;
		m_Processed = false;
		m_PayLoad = payLoad;
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

}
