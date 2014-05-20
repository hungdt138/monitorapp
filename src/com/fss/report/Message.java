package com.fss.report;

import com.fss.util.*;
import com.fss.dictionary.*;

///////////////////////////////////////////
// Parameter:
// strMessage : text will be shown in browser
// iButton : list of button to show.
// 1 : close
// 2 : home
// 4 : back
// 8 : forward
// 16 : refresh
// To show multiple button use | (OR) operator
// Ex: iButton = 1 | 8 will show close and forward button
///////////////////////////////////////////
public class Message
{
	public static String formatMessage(String strMessage,String strTab,String strImage,int iButton)
	{
		if(strMessage == null)
			strMessage = "Null pointer exception";
		strMessage = StringUtil.replaceAll(strMessage,"\r\n","\r\n<BR>\r\n");
		String strReturn = "";
		strReturn += strTab + "<table width=\"100%\" border=\"0\" cellspacing=\"0\">\r\n";
		strReturn += strTab + "<tr>\r\n";
		strReturn += strTab + "<td align=\"center\" class=\"message\">\r\n";
		strReturn += strTab + "<table border=\"0\" cellspacing=\"0\">\r\n";
		strReturn += strTab + "<td class=\"message\">\r\n";
		strReturn += strTab + "<img src=\"" + strImage + "\">\r\n";
		strReturn += strTab + "</td>\r\n";
		strReturn += strTab + "<td class=\"message\">\r\n";
		strReturn += strTab + "" + strMessage + "\r\n";
		strReturn += strTab + "</td>\r\n";
		strReturn += strTab + "</table>\r\n";
		strReturn += strTab + "</td>\r\n";
		strReturn += strTab + "</tr>\r\n";
		if(iButton > 0)
		{
		strReturn += strTab + "<tr>\r\n";
		strReturn += strTab + "<td align=\"center\" class=\"message\">\r\n";
		strReturn += strTab + "<form>\r\n";
		if((iButton | 4) == iButton)	// back button
			strReturn += strTab + "<input style=\"width:100\" type=\"button\" value=\"Back\" class=\"btn\" onclick=\"history.back()\">\r\n";
		if((iButton | 2) == iButton)	// home button
			strReturn += strTab + "<input style=\"width:100\" type=\"button\" value=\"Home\" class=\"btn\" onclick=\"window.location.href='index.jsp'>\r\n";
		if((iButton | 16) == iButton)	// refresh
			strReturn += strTab + "<input style=\"width:100\" type=\"button\" value=\"Refresh\" class=\"btn\" onclick=\"window.location.reload()\">\r\n";
		if((iButton | 8) == iButton)	// forward
			strReturn += strTab + "<input style=\"width:100\" type=\"button\" value=\"Forward\" class=\"btn\" onclick=\"history.forward()\">\r\n";
		if((iButton | 1) == iButton)	// close button
			strReturn += strTab + "<input style=\"width:100\" type=\"button\" value=\"Close\" class=\"btn\" onclick=\"window.close()\">\r\n";
		strReturn += strTab + "</form>\r\n";
		strReturn += strTab + "</td>\r\n";
		strReturn += strTab + "</tr>\r\n";
		}
		strReturn += strTab + "</table>\r\n";
		return strReturn;
	}
	public static String formatMessage(Exception e,String strTab,String strImage,int iButton)
	{
		String strMessage = null;
		if(e instanceof AppException)
			strMessage = ErrorDictionary.getString(((AppException)e).getReason(),
				StringUtil.toStringArray(StringUtil.nvl(((AppException)e).getInfo(),"")));
		else
			strMessage = ErrorDictionary.getString(e.getMessage());
		return formatMessage(strMessage,strTab,strImage,iButton);
	}
	public static String formatErrorMessage(String strMessage,String strTab,int iButton)
	{
		if(strMessage == null)
			strMessage = "Null pointer exception";
		strMessage = StringUtil.replaceAll(strMessage,"\r\n","\r\n<BR>\r\n");
		String strReturn = "";
		strReturn += strTab + "<table width=\"100%\" border=\"0\" cellspacing=\"0\">\r\n";
		strReturn += strTab + "<tr>\r\n";
		strReturn += strTab + "<td align=\"center\" class=\"message\">\r\n";
		strReturn += strTab + "<table border=\"0\" cellspacing=\"0\">\r\n";
		strReturn += strTab + "<td class=\"message\">\r\n";
		strReturn += strTab + "<img src=\"images/error.gif\">\r\n";
		strReturn += strTab + "</td>\r\n";
		strReturn += strTab + "<td class=\"message\">\r\n";
		strReturn += strTab + "" + strMessage + "\r\n";
		strReturn += strTab + "</td>\r\n";
		strReturn += strTab + "</table>\r\n";
		strReturn += strTab + "</td>\r\n";
		strReturn += strTab + "</tr>\r\n";
		if(iButton > 0)
		{
		strReturn += strTab + "<tr>\r\n";
		strReturn += strTab + "<td align=\"center\" class=\"message\">\r\n";
		strReturn += strTab + "<form>\r\n";
		if((iButton | 4) == iButton)	// back button
			strReturn += strTab + "<input style=\"width:100\" type=\"button\" value=\"Back\" class=\"btn\" onclick=\"history.back()\">\r\n";
		if((iButton | 2) == iButton)	// home button
			strReturn += strTab + "<input style=\"width:100\" type=\"button\" value=\"Home\" class=\"btn\" onclick=\"window.location.href='index.jsp'\">\r\n";
		if((iButton | 16) == iButton)	// refresh
			strReturn += strTab + "<input style=\"width:100\" type=\"button\" value=\"Refresh\" class=\"btn\" onclick=\"window.location.reload()\">\r\n";
		if((iButton | 8) == iButton)	// forward
			strReturn += strTab + "<input style=\"width:100\" type=\"button\" value=\"Forward\" class=\"btn\" onclick=\"history.forward()\">\r\n";
		if((iButton | 1) == iButton)	// close button
			strReturn += strTab + "<input style=\"width:100\" type=\"button\" value=\"Close\" class=\"btn\" onclick=\"window.close()\">\r\n";
		strReturn += strTab + "</form>\r\n";
		strReturn += strTab + "</td>\r\n";
		strReturn += strTab + "</tr>\r\n";
		}
		strReturn += strTab + "</table>\r\n";
		return strReturn;
	}
	public static String formatErrorMessage(Exception e,String strTab,int iButton)
	{
		String strMessage = null;
		if(e instanceof AppException)
			strMessage = ErrorDictionary.getString(((AppException)e).getReason(),
				StringUtil.toStringArray(StringUtil.nvl(((AppException)e).getInfo(),"")));
		else
			strMessage = ErrorDictionary.getString(e.getMessage());
		return formatErrorMessage(strMessage,strTab,iButton);
	}
	public static String formatWarningMessage(String strMessage,String strTab,int iButton)
	{
		if(strMessage == null)
			strMessage = "Null pointer exception";
		strMessage = StringUtil.replaceAll(strMessage,"\r\n","\r\n<BR>\r\n");
		String strReturn = "";
		strReturn += strTab + "<table width=\"100%\" border=\"0\" cellspacing=\"0\">\r\n";
		strReturn += strTab + "<tr>\r\n";
		strReturn += strTab + "<td align=\"center\" class=\"message\">\r\n";
		strReturn += strTab + "<table border=\"0\" cellspacing=\"0\">\r\n";
		strReturn += strTab + "<td class=\"message\">\r\n";
		strReturn += strTab + "<img src=\"images/warning.gif\">\r\n";
		strReturn += strTab + "</td>\r\n";
		strReturn += strTab + "<td class=\"message\">\r\n";
		strReturn += strTab + "" + strMessage + "\r\n";
		strReturn += strTab + "</td>\r\n";
		strReturn += strTab + "</table>\r\n";
		strReturn += strTab + "</td>\r\n";
		strReturn += strTab + "</tr>\r\n";
		if(iButton > 0)
		{
		strReturn += strTab + "<tr>\r\n";
		strReturn += strTab + "<td align=\"center\" class=\"message\">\r\n";
		strReturn += strTab + "<form>\r\n";
		if((iButton | 4) == iButton)	// back button
			strReturn += strTab + "<input style=\"width:100\" type=\"button\" value=\"Back\" class=\"btn\" onclick=\"history.back()\">\r\n";
		if((iButton | 2) == iButton)	// home button
			strReturn += strTab + "<input style=\"width:100\" type=\"button\" value=\"Home\" class=\"btn\" onclick=\"window.location.href='index.jsp'\">\r\n";
		if((iButton | 16) == iButton)	// refresh
			strReturn += strTab + "<input style=\"width:100\" type=\"button\" value=\"Refresh\" class=\"btn\" onclick=\"window.location.reload()\">\r\n";
		if((iButton | 8) == iButton)	// forward
			strReturn += strTab + "<input style=\"width:100\" type=\"button\" value=\"Forward\" class=\"btn\" onclick=\"history.forward()\">\r\n";
		if((iButton | 1) == iButton)	// close button
			strReturn += strTab + "<input style=\"width:100\" type=\"button\" value=\"Close\" class=\"btn\" onclick=\"window.close()\">\r\n";
		strReturn += strTab + "</form>\r\n";
		strReturn += strTab + "</td>\r\n";
		strReturn += strTab + "</tr>\r\n";
		}
		strReturn += strTab + "</table>\r\n";
		return strReturn;
	}
	public static String formatInformationMessage(String strMessage,String strTab,int iButton)
	{
		if(strMessage == null)
			strMessage = "Null pointer exception";
		strMessage = StringUtil.replaceAll(strMessage,"\r\n","\r\n<BR>\r\n");
		String strReturn = "";
		strReturn += strTab + "<table width=\"100%\" border=\"0\" cellspacing=\"0\">\r\n";
		strReturn += strTab + "<tr>\r\n";
		strReturn += strTab + "<td align=\"center\" class=\"message\">\r\n";
		strReturn += strTab + "<table border=\"0\" cellspacing=\"0\">\r\n";
		strReturn += strTab + "<td class=\"message\">\r\n";
		strReturn += strTab + "<img src=\"images/infor.gif\">\r\n";
		strReturn += strTab + "</td>\r\n";
		strReturn += strTab + "<td class=\"message\">\r\n";
		strReturn += strTab + "" + strMessage + "";
		strReturn += strTab + "</td>\r\n";
		strReturn += strTab + "</table>\r\n";
		strReturn += strTab + "</td>\r\n";
		strReturn += strTab + "</tr>\r\n";
		if(iButton > 0)
		{
		strReturn += strTab + "<tr>\r\n";
		strReturn += strTab + "<td align=\"center\" class=\"message\">\r\n";
		strReturn += strTab + "<form>\r\n";
		if((iButton | 4) == iButton)	// back button
			strReturn += strTab + "<input style=\"width:100\" type=\"button\" value=\"Back\" class=\"btn\" onclick=\"history.back()\">\r\n";
		if((iButton | 2) == iButton)	// home button
			strReturn += strTab + "<input style=\"width:100\" type=\"button\" value=\"Home\" class=\"btn\" onclick=\"window.location.href='index.jsp'\">\r\n";
		if((iButton | 16) == iButton)	// refresh
			strReturn += strTab + "<input style=\"width:100\" type=\"button\" value=\"Refresh\" class=\"btn\" onclick=\"window.location.reload()\">\r\n";
		if((iButton | 8) == iButton)	// forward
			strReturn += strTab + "<input style=\"width:100\" type=\"button\" value=\"Forward\" class=\"btn\" onclick=\"history.forward()\">\r\n";
		if((iButton | 1) == iButton)	// close button
			strReturn += strTab + "<input style=\"width:100\" type=\"button\" value=\"Close\" class=\"btn\" onclick=\"window.close()\">\r\n";
		strReturn += strTab + "</form>\r\n";
		strReturn += strTab + "</td>\r\n";
		strReturn += strTab + "</tr>\r\n";
		}
		strReturn += strTab + "</table>\r\n";
		return strReturn;
	}

	/////////////////////////////////////////////////////////////////////
	// Not use tab
	/////////////////////////////////////////////////////////////////////
	public static String formatMessage(String strMessage,String strImage,int iButton)
	{
		if(strMessage == null)
			strMessage = "Null pointer exception";
		strMessage = StringUtil.replaceAll(strMessage,"\r\n","\r\n<BR>\r\n");
		String strReturn = "";
		strReturn += "<table width=\"100%\" border=\"0\" cellspacing=\"0\">\r\n";
		strReturn += "<tr>\r\n";
		strReturn += "<td align=\"center\" class=\"message\">\r\n";
		strReturn += "<table border=\"0\" cellspacing=\"0\">\r\n";
		strReturn += "<td class=\"message\">\r\n";
		strReturn += "<img src=\"" + strImage + "\">\r\n";
		strReturn += "</td>\r\n";
		strReturn += "<td class=\"message\">\r\n";
		strReturn += "" + strMessage + "";
		strReturn += "</td>\r\n";
		strReturn += "</table>\r\n";
		strReturn += "</td>\r\n";
		strReturn += "</tr>\r\n";
		if(iButton > 0)
		{
		strReturn += "<tr>\r\n";
		strReturn += "<td align=\"center\" class=\"message\">\r\n";
		strReturn += "<form>\r\n";
		if((iButton | 4) == iButton)	// back button
			strReturn += "<input style=\"width:100\" type=\"button\" value=\"Back\" class=\"btn\" onclick=\"history.back()\">\r\n";
		if((iButton | 2) == iButton)	// home button
			strReturn += "<input style=\"width:100\" type=\"button\" value=\"Home\" class=\"btn\" onclick=\"window.location.href='index.jsp'\">\r\n";
		if((iButton | 16) == iButton)	// refresh
			strReturn += "<input style=\"width:100\" type=\"button\" value=\"Refresh\" class=\"btn\" onclick=\"window.location.reload()\">\r\n";
		if((iButton | 8) == iButton)	// forward
			strReturn += "<input style=\"width:100\" type=\"button\" value=\"Forward\" class=\"btn\" onclick=\"history.forward()\">\r\n";
		if((iButton | 1) == iButton)	// close button
			strReturn += "<input style=\"width:100\" type=\"button\" value=\"Close\" class=\"btn\" onclick=\"window.close()\">\r\n";
		strReturn += "</form>\r\n";
		strReturn += "</td>\r\n";
		strReturn += "</tr>\r\n";
		}
		strReturn += "</table>\r\n";
		return strReturn;
	}
	public static String formatMessage(Exception e,String strImage,int iButton)
	{
		String strMessage = null;
		if(e instanceof AppException)
			strMessage = ErrorDictionary.getString(((AppException)e).getReason(),
				StringUtil.toStringArray(StringUtil.nvl(((AppException)e).getInfo(),"")));
		else
			strMessage = ErrorDictionary.getString(e.getMessage());
		return formatMessage(strMessage,strImage,iButton);
	}
	public static String formatErrorMessage(String strMessage,int iButton)
	{
		if(strMessage == null)
			strMessage = "Null pointer exception";
		strMessage = StringUtil.replaceAll(strMessage,"\r\n","\r\n<BR>\r\n");
		String strReturn = "";
		strReturn += "<table width=\"100%\" border=\"0\" cellspacing=\"0\">\r\n";
		strReturn += "<tr>\r\n";
		strReturn += "<td align=\"center\" class=\"message\">\r\n";
		strReturn += "<table border=\"0\" cellspacing=\"0\">\r\n";
		strReturn += "<td class=\"message\">\r\n";
		strReturn += "<img src=\"images/error.gif\">\r\n";
		strReturn += "</td>\r\n";
		strReturn += "<td class=\"message\">\r\n";
		strReturn += "" + strMessage + "";
		strReturn += "</td>\r\n";
		strReturn += "</table>\r\n";
		strReturn += "</td>\r\n";
		strReturn += "</tr>\r\n";
		if(iButton > 0)
		{
		strReturn += "<tr>\r\n";
		strReturn += "<td align=\"center\" class=\"message\">\r\n";
		strReturn += "<form>\r\n";
		if((iButton | 4) == iButton)	// back button
			strReturn += "<input style=\"width:100\" type=\"button\" value=\"Back\" class=\"btn\" onclick=\"history.back()\">\r\n";
		if((iButton | 2) == iButton)	// home button
			strReturn += "<input style=\"width:100\" type=\"button\" value=\"Home\" class=\"btn\" onclick=\"window.location.href='index.jsp'\">\r\n";
		if((iButton | 16) == iButton)	// refresh
			strReturn += "<input style=\"width:100\" type=\"button\" value=\"Refresh\" class=\"btn\" onclick=\"window.location.reload()\">\r\n";
		if((iButton | 8) == iButton)	// forward
			strReturn += "<input style=\"width:100\" type=\"button\" value=\"Forward\" class=\"btn\" onclick=\"history.forward()\">\r\n";
		if((iButton | 1) == iButton)	// close button
			strReturn += "<input style=\"width:100\" type=\"button\" value=\"Close\" class=\"btn\" onclick=\"window.close()\">\r\n";
		strReturn += "</form>\r\n";
		strReturn += "</td>\r\n";
		strReturn += "</tr>\r\n";
		}
		strReturn += "</table>\r\n";
		return strReturn;
	}
	public static String formatErrorMessage(Exception e,int iButton)
	{
		String strMessage = null;
		if(e instanceof AppException)
			strMessage = ErrorDictionary.getString(((AppException)e).getReason(),
				StringUtil.toStringArray(StringUtil.nvl(((AppException)e).getInfo(),"")));
		else
			strMessage = ErrorDictionary.getString(e.getMessage());
		return formatErrorMessage(strMessage,iButton);
	}
	public static String formatWarningMessage(String strMessage,int iButton)
	{
		if(strMessage == null)
			strMessage = "Null pointer exception";
		strMessage = StringUtil.replaceAll(strMessage,"\r\n","\r\n<BR>\r\n");
		String strReturn = "";
		strReturn += "<table width=\"100%\" border=\"0\" cellspacing=\"0\">\r\n";
		strReturn += "<tr>\r\n";
		strReturn += "<td align=\"center\" class=\"message\">\r\n";
		strReturn += "<table border=\"0\" cellspacing=\"0\">\r\n";
		strReturn += "<td class=\"message\">\r\n";
		strReturn += "<img src=\"images/warning.gif\">\r\n";
		strReturn += "</td>\r\n";
		strReturn += "<td class=\"message\">\r\n";
		strReturn += "" + strMessage + "";
		strReturn += "</td>\r\n";
		strReturn += "</table>\r\n";
		strReturn += "</td>\r\n";
		strReturn += "</tr>\r\n";
		if(iButton > 0)
		{
		strReturn += "<tr>\r\n";
		strReturn += "<td align=\"center\" class=\"message\">\r\n";
		strReturn += "<form>\r\n";
		if((iButton | 4) == iButton)	// back button
			strReturn += "<input style=\"width:100\" type=\"button\" value=\"Back\" class=\"btn\" onclick=\"history.back()\">\r\n";
		if((iButton | 2) == iButton)	// home button
			strReturn += "<input style=\"width:100\" type=\"button\" value=\"Home\" class=\"btn\" onclick=\"window.location.href='index.jsp'\">\r\n";
		if((iButton | 16) == iButton)	// refresh
			strReturn += "<input style=\"width:100\" type=\"button\" value=\"Refresh\" class=\"btn\" onclick=\"window.location.reload()\">\r\n";
		if((iButton | 8) == iButton)	// forward
			strReturn += "<input style=\"width:100\" type=\"button\" value=\"Forward\" class=\"btn\" onclick=\"history.forward()\">\r\n";
		if((iButton | 1) == iButton)	// close button
			strReturn += "<input style=\"width:100\" type=\"button\" value=\"Close\" class=\"btn\" onclick=\"window.close()\">\r\n";
		strReturn += "</form>\r\n";
		strReturn += "</td>\r\n";
		strReturn += "</tr>\r\n";
		}
		strReturn += "</table>\r\n";
		return strReturn;
	}
	public static String formatInformationMessage(String strMessage,int iButton)
	{
		if(strMessage == null)
			strMessage = "Null pointer exception";
		strMessage = StringUtil.replaceAll(strMessage,"\r\n","\r\n<BR>\r\n");
		String strReturn = "";
		strReturn += "<table width=\"100%\" border=\"0\" cellspacing=\"0\">\r\n";
		strReturn += "<tr>\r\n";
		strReturn += "<td align=\"center\" class=\"message\">\r\n";
		strReturn += "<table border=\"0\" cellspacing=\"0\">\r\n";
		strReturn += "<td class=\"message\">\r\n";
		strReturn += "<img src=\"images/infor.gif\">\r\n";
		strReturn += "</td>\r\n";
		strReturn += "<td class=\"message\">\r\n";
		strReturn += "" + strMessage + "";
		strReturn += "</td>\r\n";
		strReturn += "</table>\r\n";
		strReturn += "</td>\r\n";
		strReturn += "</tr>\r\n";
		if(iButton > 0)
		{
		strReturn += "<tr>\r\n";
		strReturn += "<td align=\"center\" class=\"message\">\r\n";
		strReturn += "<form>\r\n";
		if((iButton | 4) == iButton)	// back button
			strReturn += "<input style=\"width:100\" type=\"button\" value=\"Back\" class=\"btn\" onclick=\"history.back()\">\r\n";
		if((iButton | 2) == iButton)	// home button
			strReturn += "<input style=\"width:100\" type=\"button\" value=\"Home\" class=\"btn\" onclick=\"window.location.href='index.jsp'\">\r\n";
		if((iButton | 16) == iButton)	// refresh
			strReturn += "<input style=\"width:100\" type=\"button\" value=\"Refresh\" class=\"btn\" onclick=\"window.location.reload()\">\r\n";
		if((iButton | 8) == iButton)	// forward
			strReturn += "<input style=\"width:100\" type=\"button\" value=\"Forward\" class=\"btn\" onclick=\"history.forward()\">\r\n";
		if((iButton | 1) == iButton)	// close button
			strReturn += "<input style=\"width:100\" type=\"button\" value=\"Close\" class=\"btn\" onclick=\"window.close()\">\r\n";
		strReturn += "</form>\r\n";
		strReturn += "</td>\r\n";
		strReturn += "</tr>\r\n";
		}
		strReturn += "</table>\r\n";
		return strReturn;
	}
}
