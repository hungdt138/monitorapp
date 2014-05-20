package com.fss.util;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public abstract class XMLUtil extends DefaultHandler
{
	////////////////////////////////////////////////////////
	// Inner class
	////////////////////////////////////////////////////////
	public class XMLNode
	{
		public String mstrName;
		public String mstrValue;
		public Hashtable mprt;
		public XMLNode mpFirstChild,mpLastChild;
		public XMLNode mpNext;
		////////////////////////////////////////////////////////
		// Purpose: Add new element into last of child list
		// Inputs: ASN data to add
		// Author: HiepTH
		////////////////////////////////////////////////////////
		public void addChild(XMLNode pChild)
		{
			if(mpFirstChild == null)
				mpFirstChild = pChild;
			else
				mpLastChild.mpNext = pChild;

			mpLastChild = pChild;
			mpLastChild.mpNext = null;
		}
		////////////////////////////////////////////////////////
		// Purpose: Getchild by tagid
		// Inputs: Tagid of child field
		// Outputs: ASNData child field, null if not found
		// Author: HiepTH
		////////////////////////////////////////////////////////
		public XMLNode getChild(String strName)
		{
			XMLNode pChild = mpFirstChild;
			while(pChild != null)
			{
				if(pChild.mstrName == strName)
					return pChild;
				pChild = pChild.mpNext;
			}
			return pChild;
		}
	}
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	private Vector mvtFieldList;
	private String mstrRootElement;
	private String mstrNameSpace;
	private Vector mvtNameStack = new Vector();
	private Vector mvtData = new Vector();
	private StringBuffer mstrValue;
	////////////////////////////////////////////////////////
	public XMLNode mnd;
	public Vector mvtDataStack = new Vector();
	////////////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////////////
	protected XMLUtil(Vector vtFieldList,String strRootElement,String strNameSpace)
	{
		mvtFieldList = new Vector();
		for(int iIndex = 0;iIndex < vtFieldList.size();iIndex++)
			mvtFieldList.addElement(((String)vtFieldList.elementAt(iIndex)).toUpperCase());
		mstrRootElement = strRootElement;
		mstrNameSpace = strNameSpace;
		if(mstrNameSpace == null)
			mstrNameSpace = "";
		if(mstrNameSpace.length() > 0)
			mstrNameSpace += ":";
		mvtData = new Vector();
		for(int iIndex = 0;iIndex < mvtFieldList.size();iIndex++)
			mvtData.addElement(null);
	}
	////////////////////////////////////////////////////////
	// Member function
	////////////////////////////////////////////////////////
	public void queryData(String strFileName) throws Exception
	{
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setValidating(false);
		SAXParser parser = spf.newSAXParser();

		mvtNameStack.clear();
		parser.parse(new File(strFileName),this);
	}
	////////////////////////////////////////////////////////
	public void queryData(InputStream in) throws Exception
	{
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setValidating(false);
		SAXParser parser = spf.newSAXParser();

		mvtNameStack.clear();
		parser.parse(in,this);
	}
	////////////////////////////////////////////////////////
	public void queryData(InputStream in,SAXParser parser) throws Exception
	{
		mvtNameStack.clear();
		parser.parse(in,this);
	}
	////////////////////////////////////////////////////////
	// Get field name from name stack
	////////////////////////////////////////////////////////
	public static String getFieldName(Vector vtNameStack)
	{
		StringBuffer strFieldName = new StringBuffer();
		for(int iIndex = 0;iIndex < vtNameStack.size();iIndex++)
		{
			strFieldName.append('.');
			strFieldName.append((String)vtNameStack.elementAt(iIndex));
		}
		if(strFieldName.length() > 0)
			strFieldName.delete(0,1);
		return strFieldName.toString();
	}
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public void startElement(String uri,String name,String qName,Attributes atts) throws SAXException
	{
		if(mstrNameSpace.length() > 0)
			qName = qName.replaceAll(mstrNameSpace,"");
		mvtNameStack.addElement(qName);
		mstrValue = null;
		mnd = new XMLNode();
		mnd.mstrName = qName;
		mnd.mprt = new Hashtable();
		for(int iIndex = 0;iIndex < atts.getLength();iIndex++)
			mnd.mprt.put(atts.getQName(iIndex),atts.getValue(iIndex));
		mvtDataStack.addElement(mnd);
	}
	////////////////////////////////////////////////////////
	public void endElement(String uri,String name,String qName) throws SAXException
	{
		if(mstrNameSpace.length() > 0)
			qName = qName.replaceAll(mstrNameSpace,"");
		if(!qName.equals(mvtNameStack.elementAt(mvtNameStack.size() - 1)))
			throw new SAXException("Found end mark of " + qName + " without start mark");
		XMLNode nd = (XMLNode)mvtDataStack.elementAt(mvtDataStack.size() - 1);
		mvtDataStack.removeElementAt(mvtDataStack.size() - 1);
		String strFieldName = getFieldName(mvtNameStack);

		if(mstrValue != null)
			nd.mstrValue = mstrValue.toString().trim();
		else
			nd.mstrValue = "";
		int iResult = mvtFieldList.indexOf(strFieldName.toUpperCase());
		if(iResult >= 0)
		{
			if(mvtData.elementAt(iResult) == null)
				mvtData.setElementAt(nd,iResult);
			else if(mvtData.elementAt(iResult) instanceof XMLNode)
			{
				Vector vt = new Vector();
				vt.addElement(mvtData.elementAt(iResult));
				vt.addElement(nd);
				mvtData.setElementAt(vt,iResult);
			}
			else
				((Vector)mvtData.elementAt(iResult)).addElement(nd);
		}

		if(strFieldName.equals(mstrRootElement))
		{
			try
			{
				processRecord(nd,mvtData);
			}
			catch(Exception e)
			{
				throw new SAXException(e);
			}
			for(int iIndex = 0;iIndex < mvtData.size();iIndex++)
				mvtData.setElementAt(null,iIndex);
		}

		mvtNameStack.removeElementAt(mvtNameStack.size() - 1);
		mstrValue = null;
	}
	////////////////////////////////////////////////////////
	public void characters(char ch[],int start,int length)
	{
		if(mstrValue == null)
			mstrValue = new StringBuffer();
		mstrValue.append(new String(ch,start,length));
	}
	////////////////////////////////////////////////////////
	// Abstract function
	////////////////////////////////////////////////////////
	public abstract void processRecord(Vector vtFieldValue) throws Exception;
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param nd XMLNode
	 * @param vtFieldValue Vector
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void processRecord(XMLNode nd,Vector vtFieldValue) throws Exception
	{
		processRecord(vtFieldValue);
	}
	////////////////////////////////////////////////////////
	// Purpose: Create field list from string
	// Inputs: String store field list, separator symbol
	// Outputs: Vector contain field list
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public static Vector createFieldList(String strFieldList,String strSeparator)
	{
		// Build field list
		Vector vtFieldList = new Vector();
		String strFieldName;
		int iIndex = 0;
		int iLastIndex = iIndex;
		while((iIndex = strFieldList.indexOf(strSeparator,iLastIndex)) >= 0)
		{
			strFieldName = strFieldList.substring(iLastIndex,iIndex);
			vtFieldList.add(strFieldName);
			iLastIndex = iIndex + 1;
		}
		strFieldName = strFieldList.substring(iLastIndex,strFieldList.length());
		vtFieldList.add(strFieldName);
		return vtFieldList;
	}
}
