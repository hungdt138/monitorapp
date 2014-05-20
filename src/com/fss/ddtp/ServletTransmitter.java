package com.fss.ddtp;

import java.io.*;
import java.net.*;

import com.fss.util.*;

/**
 * <p>Title: ServletTransmitter</p>
 * <p>Description: Send request to and receive response from servlet server</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class ServletTransmitter implements Transmitter
{
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	private ExpireListener expirelistener = null;
	private String mstrSessionKey = null;
	private String mstrServletHost = null;
	private String mstrServerClass = "com.fss.ddtp.ServletServer";
	private String mstrPackage = null;
	////////////////////////////////////////////////////////
	/**
	 * @return ExpireListener
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public ExpireListener getExpireListener()
	{
		return expirelistener;
	}
	////////////////////////////////////////////////////////
	/**
	 * @param lsn ExpireListener
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void setExpireListener(ExpireListener lsn)
	{
		expirelistener = lsn;
	}
	////////////////////////////////////////////////////////
	/**
	 * @return SessionKey
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public String getSessionKey()
	{
		return mstrSessionKey;
	}
	////////////////////////////////////////////////////////
	/**
	 * @param strSessionKey String
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void setSessionKey(String strSessionKey)
	{
		mstrSessionKey = strSessionKey;
	}
	////////////////////////////////////////////////////////
	/**
	 * @return ServletHost of Transmitter
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public String getServletHost()
	{
		return mstrServletHost;
	}
	////////////////////////////////////////////////////////
	/**
	 * @param strServletHost String
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void setServletHost(String strServletHost)
	{
		mstrServletHost = strServletHost;
	}
	////////////////////////////////////////////////////////
	/**
	 * @return current class package
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public String getPackage()
	{
		return mstrPackage;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strPackage String
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void setPackage(String strPackage)
	{
		mstrPackage = strPackage;
	}
	////////////////////////////////////////////////////////
	/**
	 * @return main class to handler all request from Transmitter
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public String getServerClass()
	{
		return mstrServerClass;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strServerClass String
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void setServerClass(String strServerClass)
	{
		mstrServerClass = strServerClass;
	}
	////////////////////////////////////////////////////////
	/**
	 * @param strClass class contain method to invoke
	 * @param strFunctionName method to invoke
	 * @return response data
	 * @throws Exception
	 * @author Thai Hoang Hiep - Date: 13/05/2003
	 */
	////////////////////////////////////////////////////////
	public DDTP sendRequest(String strClass,String strFunctionName) throws Exception
	{
		return sendRequest(strClass,strFunctionName,null);
	}
	////////////////////////////////////////////////////////
	/**
	 * @param strClass class contain method to invoke
	 * @param strFunctionName method to invoke
	 * @param request request data
	 * @return response data
	 * @throws Exception
	 * @author Thai Hoang Hiep - Date: 13/05/2003
	 */
	////////////////////////////////////////////////////////
	public DDTP sendRequest(String strClass,String strFunctionName,DDTP request) throws Exception
	{
		// Create empty request if passed value is null
		if(request == null)
			request = new DDTP();

		// Set function name & class name
		request.setFunctionName(strFunctionName);
		if(strClass.indexOf(".") < 0)
			strClass = StringUtil.nvl(mstrPackage,"") + strClass;
		request.setClassName(strClass);

		// Call servlet
		return callServlet(StringUtil.nvl(mstrServletHost,""),StringUtil.nvl(mstrServerClass,""),request);
	}
	////////////////////////////////////////////////////////
	/**
	 * @param strServletHost http address to webserver
	 * @param strClass class handle request
	 * @param request request data
	 * @return response data
	 * @throws Exception if error occured
	 * @author Thai Hoang Hiep - Date: 13/05/2003
	 */
	////////////////////////////////////////////////////////
	private DDTP callServlet(String strServletHost,String strClass,DDTP request) throws Exception
	{
		try
		{
			// Open connection to servlet
			URL urlServlet = new URL(strServletHost + strClass);
			HttpURLConnection cn = (HttpURLConnection)urlServlet.openConnection();
			cn.setDoInput(true);
			cn.setDoOutput(true);
			cn.setUseCaches(false);
			if(mstrSessionKey != null)
			   cn.setRequestProperty("Cookie",mstrSessionKey);

			// Bind request parameter
			OutputStream output = cn.getOutputStream();
			request.store(output);

			// Send request
			output.flush();
			output.close();

			// Bind response parameter
			InputStream is = null;
			DDTP response = null;
			try
			{
				is = cn.getInputStream();
				response = new DDTP(is);
			}
			finally
			{
				FileUtil.safeClose(is);
			}

			String strCookieVal = cn.getHeaderField("Set-Cookie");
			if(strCookieVal != null)
				mstrSessionKey = strCookieVal.substring(0,strCookieVal.indexOf(";"));

			// Test error
			AppException e = response.getException();
			if(e != null)
				throw e;

			// Return value
			return response;
		}
		catch(AppException e)
		{
			if(e.getReason().equals("SESSION_EXPIRED") && expirelistener != null)
				return expirelistener.onExpire(this,request);
			throw e;
		}
	}
}
