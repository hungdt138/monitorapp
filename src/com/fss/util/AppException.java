package com.fss.util;

/**
 * <p>Title: AppException</p>
 * <p>Description: Exception throwed by fss library</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class AppException extends Exception
{
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	private String mstrReason = null;
	private String mstrContext = null;
	private String mstrInfo = null;
	////////////////////////////////////////////////////////
	/**
	 * @param strReason cause of exception
	 * @author Thai Hoang Hiep - Date: 13/05/2003
	 */
	////////////////////////////////////////////////////////
	public AppException(String strReason)
	{
		setReason(strReason);
	}
	////////////////////////////////////////////////////////
	/**
	 * @param strReason cause of exception
	 * @param strContext exception context
	 * @author Thai Hoang Hiep - Date: 13/05/2003
	 */
	////////////////////////////////////////////////////////
	public AppException(String strReason,String strContext)
	{
		setContext(strContext);
		setReason(strReason);
	}
	////////////////////////////////////////////////////////
	/**
	 * @param strReason cause of exception
	 * @param strContext exception context
	 * @param strInfo additional information
	 * @author Thai Hoang Hiep - Date: 13/05/2003
	 */
	////////////////////////////////////////////////////////
	public AppException(String strReason,String strContext,String strInfo)
	{
		setContext(strContext);
		setInfo(strInfo);
		setReason(strReason);
	}
	////////////////////////////////////////////////////////
	/**
	 * @param e cause of exception
	 * @param strContext exception context
	 * @author Thai Hoang Hiep - Date: 13/05/2003
	 */
	////////////////////////////////////////////////////////
	public AppException(Exception e,String strContext)
	{
		super(e);
		setContext(strContext);
		if(e instanceof AppException)
		{
			setReason(((AppException)e).getReason());
			setInfo(((AppException)e).getInfo());
		}
		else
			setReason(e.getMessage());
	}
	////////////////////////////////////////////////////////
	/**
	 * @param e cause of exception
	 * @param strContext exception context
	 * @param strInfo additional information
	 * @author Thai Hoang Hiep - Date: 13/05/2003
	 */
	////////////////////////////////////////////////////////
	public AppException(Exception e,String strContext,String strInfo)
	{
		super(e);
		setContext(strContext);
		setInfo(strInfo);
		if(e instanceof AppException)
			setReason(((AppException)e).getReason());
		else
			setReason(e.getMessage());
	}
	////////////////////////////////////////////////////////
	/**
	 * @return cause of exception
	 * @author Thai Hoang Hiep - Date: 13/05/2003
	 */
	////////////////////////////////////////////////////////
	public String getLocalizedMessage()
	{
		return mstrReason;
	}
	////////////////////////////////////////////////////////
	/**
	 * @return full description of exception
	 */
	////////////////////////////////////////////////////////
	public String getMessage()
	{
		String strMessage = mstrReason;
		if(mstrContext != null && mstrContext.length() > 0) strMessage += "\r\nContext: " + mstrContext;
		if(mstrInfo != null && mstrInfo.length() > 0) strMessage += "\r\nAdditional info: " + mstrInfo;
		return strMessage;
	}
	////////////////////////////////////////////////////////
	/**
	 * @return full description of exception
	 * @author Thai Hoang Hiep - Date: 13/05/2003
	 */
	////////////////////////////////////////////////////////
	public String toString()
	{
		return getMessage();
	}
	////////////////////////////////////////////////////////
	/**
	 * @return exception context
	 * @author Thai Hoang Hiep - Date: 13/05/2003
	 */
	////////////////////////////////////////////////////////
	public String getContext()
	{
		return mstrContext;
	}
	////////////////////////////////////////////////////////
	/**
	 * @param mstrContext exception context
	 * @author Thai Hoang Hiep - Date: 13/05/2003
	 */
	////////////////////////////////////////////////////////
	public void setContext(String mstrContext)
	{
		this.mstrContext = mstrContext;
	}
	////////////////////////////////////////////////////////
	/**
	 * @return additional info
	 * @author Thai Hoang Hiep - Date: 13/05/2003
	 */
	////////////////////////////////////////////////////////
	public String getInfo()
	{
		return mstrInfo;
	}
	////////////////////////////////////////////////////////
	/**
	 * @param mstrInfo additional information
	 * @author Thai Hoang Hiep - Date: 13/05/2003
	 */
	////////////////////////////////////////////////////////
	public void setInfo(String mstrInfo)
	{
		this.mstrInfo = mstrInfo;
	}
	////////////////////////////////////////////////////////
	/**
	 * @return cause of exception
	 * @author Thai Hoang Hiep - Date: 13/05/2003
	 */
	////////////////////////////////////////////////////////
	public String getReason()
	{
		return mstrReason;
	}
	////////////////////////////////////////////////////////
	/**
	 * @param strReason cause of exception
	 * @author Thai Hoang Hiep - Date: 13/05/2003
	 */
	////////////////////////////////////////////////////////
	public void setReason(String strReason)
	{
		if(strReason == null)
			mstrReason = "Null pointer exception";
		else
			mstrReason = strReason.trim();
	}
}
