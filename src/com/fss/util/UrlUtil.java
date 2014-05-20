package com.fss.util;

import java.net.*;

/**
 * <p>Title: Browser</p>
 * <p>Description: Utility for http</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class UrlUtil
{
	////////////////////////////////////////////////////////
	/**
	 * Open URL
	 * @param obj main object
	 * @param strURL address to open
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static void openURL(Object obj,String strURL)
	{
		if(obj instanceof java.applet.Applet)
		{
			java.applet.Applet applet = (java.applet.Applet)obj;
			try
			{
				applet.getAppletContext().showDocument(new URL(strURL),Global.APP_NAME);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			String strCommand = null;
			if(System.getProperty("os.name").startsWith("Windows"))
			{
				try
				{
					strCommand = "rundll32 url.dll,FileProtocolHandler " + strURL;
					Runtime.getRuntime().exec(strCommand);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}