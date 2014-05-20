package com.fss.report;

import java.sql.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FPT</p>
 * @author HiepTH
 * @version 1.0
 */

public class JSPComboUtil
{
	public static String createList(String arrValues[][],String strName,String strValue,String strExtension,String strAll)
	{
		if(strExtension == null)
			strExtension = "";
		String strReturn = "<select name=\"" + strName + "\" " + strExtension + ">";
		if(strAll != null)
			strReturn += "<option value=\"\">" + strAll + "</option>";
		for(int j=0;j < arrValues.length;j++ )
		{
			if(strValue.equals(arrValues[j][0]))
				strReturn += "<option value=\"" + arrValues[j][0] +"\" selected>" +  arrValues[j][1] +"</option>";
			else
				strReturn += "<option value=\"" + arrValues[j][0] +"\">" +  arrValues[j][1] +"</option>";
		}
		strReturn += "</select>";
		return strReturn;
	}

	public static String createList(ResultSet rsData,String strName,String strValue,String strExtension,String strAll)
	{
		if(strExtension == null)
			strExtension = "";
		String strReturn = "<select name=\"" + strName + "\" " + strExtension + ">";
		if(strAll != null)
			strReturn += "<option value=\"\">" + strAll + "</option>";
		if(rsData != null)
		{
			try
			{
				while(rsData.next() && rsData != null)
				{
					if(strValue.equals(rsData.getString(1)))
						strReturn += "<option value=\"" + rsData.getString(1) +"\" selected>" + rsData.getString(2) +"</option>";
					else
						strReturn += "<option value=\"" + rsData.getString(1) +"\">" + rsData.getString(2) +"</option>";
				}

			}
			catch(SQLException e){}
		}
		strReturn += "</select>";
		return strReturn;
	}

	public static String createList(Connection cn,String strSql,String strName,String strValue,String strExtension,String strAll)
	{
		String strReturn = "";
		try
		{
			Statement stmt = cn.createStatement();
			ResultSet rsData = stmt.executeQuery(strSql);
			strReturn = createList(rsData,strName,strValue,strExtension,strAll);
			rsData.close();
			stmt.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return strReturn;
	}
}
