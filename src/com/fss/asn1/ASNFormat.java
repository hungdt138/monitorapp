package com.fss.asn1;

import java.io.*;
import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public abstract class ASNFormat
{
	////////////////////////////////////////////////////////
	// Data
	////////////////////////////////////////////////////////
	public ASNData mdatRoot = null;
	public ASNDefinition mdefRoot = null;
	public String strSeperator = ";";
	public String strReplace = ",";
	////////////////////////////////////////////////////////
	// Abstract function
	////////////////////////////////////////////////////////
	public abstract String format(ASNDefinition def,ASNData dat);
	public abstract byte[] parse(ASNDefinition def,String strData);
	////////////////////////////////////////////////////////
	// Open data source
	////////////////////////////////////////////////////////
	public void open(ASNData dat)
	{
		mdatRoot = dat;
		mdefRoot.compile(mdatRoot);
	}
	////////////////////////////////////////////////////////
	// Purpose: Create cursor from query command
	// Inputs: String store query command
	// Outputs: ASNCursor created
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public void createCursor(ASNCursor cursor,String strQuery) throws IOException
	{
		// Get table name
		int iIndex = 0;
		int iLastIndex = iIndex;
		iIndex = strQuery.indexOf('(');
		String strTableName = strQuery.substring(iLastIndex,iIndex);
		iLastIndex = iIndex + 1;
		int iResult = strTableName.indexOf('!');
		if(iResult >= 0)
		{
			String strChoiceName = strTableName.substring(iResult + 1,strTableName.length());
			strTableName = strTableName.substring(0,iResult);
			cursor.mdefCursor = buildCursorList(strTableName);
			cursor.mdefChoice = cursor.mdefCursor[cursor.mdefCursor.length - 1].getChild(strChoiceName);
		}
		else
		{
			cursor.mdefCursor = buildCursorList(strTableName);
			cursor.mdefChoice = cursor.mdefCursor[cursor.mdefCursor.length - 1];
		}

		if(cursor.mdefCursor == null || cursor.mdefCursor.length <= 0)
			throw new IOException("Empty cursor");

		// Get field list
		iIndex = strQuery.indexOf(')');
		String strFieldList = strQuery.substring(iLastIndex,iIndex);
		iIndex = 0;
		iLastIndex = iIndex;

		// Build field list
		Vector vctFieldList = new Vector();
		String strFieldName;
		while((iIndex = strFieldList.indexOf(',',iLastIndex)) >= 0)
		{
			strFieldName = strFieldList.substring(iLastIndex,iIndex);
			vctFieldList.add(strFieldName);
			iLastIndex = iIndex + 1;
		}
		strFieldName = strFieldList.substring(iLastIndex,strFieldList.length());
		vctFieldList.add(strFieldName);

		// Analyse field name
		int iFieldCount = vctFieldList.size();
		cursor.mdefFieldList = new ASNDefinition[iFieldCount];
		for(iIndex = 0;iIndex < vctFieldList.size();iIndex++)
		{
			strFieldName = (String)vctFieldList.elementAt(iIndex);
			if(strFieldName.toUpperCase().equals("NULL"))
				cursor.mdefFieldList[iIndex] = null;
			else if(strFieldName.startsWith("'") && strFieldName.endsWith("'"))
			{
				ASNDefinition def = new ASNDefinition();
				def.miType = ASNDefinition.ASN_CONST;
				def.mstrValue = strFieldName.substring(1,strFieldName.length() - 1);
				cursor.mdefFieldList[iIndex] = def;
			}
			else
			{
				cursor.mdefFieldList[iIndex] = cursor.mdefChoice.getChild(strFieldName);
				if(cursor.mdefFieldList[iIndex] == null)
					throw new IOException("Field " + strFieldName + " not found in asn description");
			}
		}

		// Initialize
		cursor.mdatFieldList = new ASNData[cursor.mdefFieldList.length];
		cursor.mstrFieldList = new String[cursor.mdefFieldList.length];
		cursor.mdatCursor = new ASNData[cursor.mdefCursor.length];
		cursor.mfmt = this;
	}
	////////////////////////////////////////////////////////
	// Purpose: Build cursor list
	// Inputs: String contain cursor path
	// Outputs: Exception throw if error occured
	// Author: HiepTH
	////////////////////////////////////////////////////////
	private ASNDefinition[] buildCursorList(String strCursor) throws IOException
	{
		Vector vtReturn = new Vector();
		ASNDefinition def = null;
		int iIndex = 0;
		while((iIndex = strCursor.indexOf('.')) >= 0)
		{
			String strCursorName = strCursor.substring(0,iIndex);
			strCursor = strCursor.substring(iIndex + 1,strCursor.length());
			if(def == null)
				def = mdefRoot.getChild(strCursorName);
			else
				def = def.getChild(strCursorName);
			if(def == null)
				throw new IOException(strCursorName + " not found in path");
			vtReturn.addElement(def);
		}
		if(def == null)
			def = mdefRoot.getChild(strCursor);
		else
			def = def.getChild(strCursor);
		if(def == null)
			throw new IOException(strCursor + " not found in path");
		vtReturn.addElement(def);
		ASNDefinition[] defReturn = new ASNDefinition[vtReturn.size()];
		for(iIndex = 0;iIndex < defReturn.length;iIndex++)
			defReturn[iIndex] = (ASNDefinition)vtReturn.elementAt(iIndex);
		return defReturn;
	}
	////////////////////////////////////////////////////////
	// Purpose: Build query command
	// Inputs: ASNCursor store query statement
	// Outputs: Exception throw if error occured
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public void query(ASNCursor cursor) throws IOException
	{
		// Contruct cursor
		cursor.mfmt = this;
		if(cursor.mdefCursor.length <= 0)
			throw new IOException("Emty cursor");

		// Construct first element
		cursor.mdatCursor[0] = mdatRoot.getChildOneLevel(cursor.mdefCursor[0]);
		cursor.miLevel = 0;
	}
	////////////////////////////////////////////////////////
	// Purpose: Build query command
	// Inputs: String store query command
	// Outputs: ASNCursor created, exception throw if error occured
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public void query(ASNCursor cursor,String strQuery) throws IOException
	{
		createCursor(cursor,strQuery);
		query(cursor);
	}
	////////////////////////////////////////////////////////
	// Purpose: Process single file, override function
	// Inputs: File name, file id
	// Outputs: Exception throw if error occured,
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public int convert(String strSrcFile,String strDesFile,Hashtable prtParameter) throws Exception
	{
		return 0;
	}
}
