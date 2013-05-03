package com.duodeck.workout;

import java.text.SimpleDateFormat;
import java.util.Date;


public class CustomDate extends Date {
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	public String getCurrentDateAsString() 
	{
		return dateFormat.format(new Date());
	}
	
	public Date getDateObjFromString(String dateString) throws java.text.ParseException
	{
		return dateFormat.parse(dateString);
		
	}
	
	public Date getEarlierDate(Date otherDate) 
	{
		if (this.before(otherDate))
		{
			return this;
		} else {
			return otherDate;
		}
	}
	
	public Date getLaterDate(Date otherDate) 
	{
		if (this.after(otherDate))
		{
			return this;
		} else {
			return otherDate;
		}
	}
}
