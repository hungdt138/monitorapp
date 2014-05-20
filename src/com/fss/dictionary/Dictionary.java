package com.fss.dictionary;

import java.io.*;
import java.util.*;

import com.fss.util.*;

/**
 * <p>Title: Dictionary</p>
 * <p>Description: Object contain dictionary data</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Nguyen Thi Thu Trang
 * @version 1.0
 */

public class Dictionary
{
	////////////////////////////////////////////////////////
	// Constant
	////////////////////////////////////////////////////////
	public static final int BUFFER_SIZE = 16384; // 16K
	////////////////////////////////////////////////////////
	// Constant
	////////////////////////////////////////////////////////
	private static final String SEPARATOR_SYMBOL = "=";
	private static final String NEXT_LEVEL_SYMBOL = "\t";
	private static final String REF_SYMBOL = ">";
	private static final String VAL_SYMBOL = "=";
	private static final String VECTOR_SYMBOL = "^";
	private static final String LONG_STRING_SYMBOL = ":";
	private static final String COMMENT_SYMBOL = "#";
	private static final String BEGIN_SYMBOL = "{";
	private static final String END_SYMBOL = "}";
	private int miLineIndex;
	private int miLevel;
	////////////////////////////////////////////////////////
	public DictionaryNode mndRoot = new DictionaryNode();
	public Hashtable mprtComment = new Hashtable();
	private String strLanguage = null;
	private String strIconPath = null;
	////////////////////////////////////////////////////////
	/**
	 * Create new instance of dictionary
	 * @param prt Hashtable contain dictionary data
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public Dictionary(Hashtable prt)
	{
		mndRoot = new DictionaryNode();
		mndRoot.mvtChild = new Vector();
		Enumeration enm = prt.keys();
		while(enm.hasMoreElements())
		{
			String str = (String)enm.nextElement();
			Object obj = prt.get(str);
			if(obj != null && obj instanceof Vector)
				mndRoot.setChildVector(str,(Vector)obj);
			else
				mndRoot.setChildValue(str,(String)obj);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Convert to Hashtable
	 * @return Hashtable
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public Hashtable toHashtable()
	{
		Hashtable prt = new Hashtable();
		if(mndRoot.mvtChild != null)
		{
			for(int iIndex = 0;iIndex < mndRoot.mvtChild.size();iIndex++)
			{
				DictionaryNode nd = (DictionaryNode)mndRoot.mvtChild.elementAt(iIndex);
				if(nd.miType == nd.TYPE_VECTOR)
					prt.put(nd.mstrName,nd.getVector());
				else if(nd.mstrValue != null)
					prt.put(nd.mstrName,nd.mstrValue);
			}
		}
		return prt;
	}
	////////////////////////////////////////////////////////
	/**
	 * Convert to Hashtable
	 * @return Hashtable
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public Properties toProperties()
	{
		Properties prt = new Properties();
		if(mndRoot.mvtChild != null)
		{
			for(int iIndex = 0;iIndex < mndRoot.mvtChild.size();iIndex++)
			{
				DictionaryNode nd = (DictionaryNode)mndRoot.mvtChild.elementAt(iIndex);
				if(nd.miType == nd.TYPE_VECTOR)
					prt.put(nd.mstrName,nd.getVector());
				else if(nd.mstrValue != null)
					prt.put(nd.mstrName,nd.mstrValue);
			}
		}
		return prt;
	}
	////////////////////////////////////////////////////////
	/**
	 * Create new instance of dictionary
	 * @param nd dictionary root node
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public Dictionary(DictionaryNode nd)
	{
		mndRoot = nd;
	}
	////////////////////////////////////////////////////////
	/**
	 * Create new instance of dictionary
	 * @param is input stream contain dictionary data
	 * @throws IOException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public Dictionary(InputStream is) throws IOException
	{
		load(is);
	}
	////////////////////////////////////////////////////////
	/**
	 * Create new instance of dictionary
	 * @param strFileName path to file contain dictionary data
	 * @throws IOException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public Dictionary(String strFileName) throws IOException
	{
		load(strFileName);
	}
	////////////////////////////////////////////////////////
	/**
	 * Create empty dictionary
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public Dictionary()
	{
	}
	////////////////////////////////////////////////////////
	/**
	 * Load dictionary from BufferedReader
	 * @param buf String buffer contain dictionary data
	 * @param ndParent parent node
	 * @param iLevel child level
	 * @return last line index
	 * @throws IOException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public DictionaryNode load(StringBuffer buf,DictionaryNode ndParent,int iLevel) throws IOException
	{
		// Analyse string buffer
		String strLine = null;
		int iIndex = 0,iLastIndex = 0;
		int iCurrentLevel = 0;
		String strValue = null;
		while((strLine = getLine(buf)) != null)
		{
			// Process comment
			if(strLine.trim().startsWith(COMMENT_SYMBOL))
			{
				if(ndParent.mvtChild.size() > 0)
				{
					String strComment = StringUtil.nvl(mprtComment.get(ndParent.mvtChild.lastElement()),"");
					strComment += strLine + "\r\n";
					mprtComment.put(ndParent.mvtChild.lastElement(),strComment);
				}
				else
				{
					String strComment = StringUtil.nvl(mprtComment.get(ndParent),"");
					strComment += strLine + "\r\n";
					mprtComment.put(ndParent,strComment);
				}
				continue;
			}

			// Get level
			iIndex = StringUtil.indexOfLetter(strLine,0);
			if(iIndex < 0)
				continue;
			iCurrentLevel = StringUtil.countSymbol(strLine.substring(0,iIndex),NEXT_LEVEL_SYMBOL,0);

			// Get name & value
			iLastIndex = iIndex;
			iIndex = strLine.indexOf(SEPARATOR_SYMBOL,iIndex);
			DictionaryNode node = new DictionaryNode();
			node.mndRoot = this.mndRoot;
			if(iIndex < 0)
				node.mstrName = strLine.substring(iLastIndex,strLine.length()).trim();
			else
			{
				node.mstrName = strLine.substring(iLastIndex,iIndex).trim();
				strValue = strLine.substring(iIndex + SEPARATOR_SYMBOL.length(),strLine.length());
				if(strValue.startsWith(REF_SYMBOL))
				{
					node.miType = DictionaryNode.TYPE_REFERENCE;
					node.mstrValue = strValue.substring(REF_SYMBOL.length(),strValue.length()).trim();
					node.mstrValue = StringEscapeUtil.unescapeJava(node.mstrValue);
				}
				else if(strValue.startsWith(VAL_SYMBOL))
				{
					node.miType = DictionaryNode.TYPE_VALUE;
					node.mstrValue = strValue.substring(VAL_SYMBOL.length(),strValue.length()).trim();
					node.mstrValue = StringEscapeUtil.unescapeJava(node.mstrValue);
				}
				else if(strValue.startsWith(VECTOR_SYMBOL))
				{
					node.miType = DictionaryNode.TYPE_VECTOR;
					node.mstrValue = strValue.substring(VECTOR_SYMBOL.length(),strValue.length()).trim();
					node.mstrValue = StringEscapeUtil.unescapeJava(node.mstrValue);
				}
				else if(strValue.startsWith(LONG_STRING_SYMBOL))
				{
					node.miType = DictionaryNode.TYPE_LONG_STRING;
					strLine = getLine(buf);
					if(strLine == null || !strLine.trim().equals(BEGIN_SYMBOL))
						throw new IOException("Begin symbol ('" + BEGIN_SYMBOL + "') expected, line " + miLineIndex);
					node.mstrValue = "";
					while((strLine = getLine(buf,true)) != null)
					{
						if(strLine.trim().equals(END_SYMBOL))
							break;
						iIndex = 0;
						while(iIndex <= iCurrentLevel && iIndex < strLine.length() &&
							  (strLine.charAt(iIndex) == '\t'))
							iIndex++;
						node.mstrValue += strLine.substring(iIndex,strLine.length());
					}
					if(strLine == null)
						throw new IOException("End symbol ('" + END_SYMBOL + "') expected, line " + miLineIndex);
					if(node.mstrValue.endsWith("\r\n"))
						node.mstrValue = node.mstrValue.substring(0,node.mstrValue.length() - 2);
					else
						node.mstrValue = node.mstrValue.substring(0,node.mstrValue.length() - 1);
				}
				else
					throw new IOException("Type of value not supported, line " + miLineIndex);
			}

			if(iCurrentLevel == iLevel)
				ndParent.mvtChild.addElement(node);
			else if(iCurrentLevel == iLevel + 1)
			{
				DictionaryNode ndLast = (DictionaryNode)ndParent.mvtChild.elementAt(ndParent.mvtChild.size() - 1);
				ndLast.mvtChild = new Vector();
				ndLast.mvtChild.addElement(node);
				DictionaryNode nd = load(buf,ndLast,iCurrentLevel);
				if(nd != null)
				{
					if(iLevel == miLevel)
						ndParent.mvtChild.addElement(nd);
					else
						return nd;
				}
			}
			else if(iCurrentLevel >= iLevel + 1)
				throw new IOException("Invalid node level, line " + miLineIndex);
			else
			{
				miLevel = iCurrentLevel;
				return node;
			}
		}
		ndParent.mvtChild.trimToSize();
		return null;
	}
	////////////////////////////////////////////////////////
	private String getLine(StringBuffer buf)
	{
		return getLine(buf,false);
	}
	////////////////////////////////////////////////////////
	private String getLine(StringBuffer buf,boolean bKeepLineFeed)
	{
		int iIndex = 0;
		while(iIndex < buf.length())
		{
			char c = buf.charAt(iIndex);
			if(c == '\r' || c == '\n')
			{
				String strReturn = null;
				if(c == '\r' && buf.charAt(iIndex + 1) == '\n')
				{
					if(bKeepLineFeed)
						strReturn = buf.substring(0,iIndex + 2);
					else
						strReturn = buf.substring(0,iIndex);
					buf.delete(0,iIndex + 2);
				}
				else
				{
					if(bKeepLineFeed)
						strReturn = buf.substring(0,iIndex + 1);
					else
						strReturn = buf.substring(0,iIndex);
					buf.delete(0,iIndex + 1);
				}
				miLineIndex++;
				return strReturn;
			}
			iIndex++;
		}
		String strReturn = buf.toString();
		buf.delete(0,buf.length());
		if(strReturn.length() == 0)
			return null;
		return strReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 * Load dictionary from InputStream
	 * @param is InputStream contain dictionary data
	 * @throws IOException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void load(InputStream is) throws IOException
	{
		StringBuffer buf = null;
		synchronized(is)
		{
			mndRoot.mvtChild = new Vector();
			byte[] bt = new byte[BUFFER_SIZE];
			int iByteRead = 0;
			buf = new StringBuffer();
			while((iByteRead = is.read(bt)) >= 0)
				buf.append(new String(bt,0,iByteRead,Global.ENCODE));
		}
		miLineIndex = 0;
		DictionaryNode nd = load(buf,mndRoot,0);
		if(nd != null)
		{
			if(miLevel == 0)
				mndRoot.mvtChild.addElement(nd);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Load dictionary from InputStream
	 * @param strFileName path to file contain dictionary data
	 * @throws IOException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void load(String strFileName) throws IOException
	{
		InputStream is = null;
		try
		{
			is = new FileInputStream(strFileName);
			load(is);
		}
		finally
		{
			FileUtil.safeClose(is);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Store dictionary to Outputstream
	 * @param os OutputStream to store data to
	 * @param nd dictionary node to store
	 * @param strLevel node level
	 * @throws IOException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	private void store(OutputStream os,DictionaryNode nd,String strLevel) throws IOException
	{
		String strNode = strLevel + nd.mstrName;
		if(nd.miType == DictionaryNode.TYPE_VECTOR ||
		   (nd.mstrValue != null && nd.mstrValue.length() > 0))
		{
			strNode += SEPARATOR_SYMBOL;
			String strValue = nd.mstrValue;
			if(nd.miType == DictionaryNode.TYPE_VALUE)
			{
				strNode += VAL_SYMBOL;
				strValue = StringEscapeUtil.escapeJava(strValue);
				strNode += strValue;
			}
			else if(nd.miType == DictionaryNode.TYPE_VECTOR)
			{
				strNode += VECTOR_SYMBOL;
				strValue = StringEscapeUtil.escapeJava(strValue);
				strNode += strValue;
			}
			else if(nd.miType == DictionaryNode.TYPE_LONG_STRING)
			{
				strNode += LONG_STRING_SYMBOL;
				strNode += "\r\n";
				strNode += strLevel + BEGIN_SYMBOL + "\r\n";
				String strNextLevel = strLevel + "\t";
				int iIndex = 0;
				int iLastIndex = iIndex;
				while((iIndex = strValue.indexOf('\n',iLastIndex)) >= 0)
				{
					strNode += strNextLevel + strValue.substring(iLastIndex,iIndex + 1);
					iLastIndex = iIndex + 1;
				}
				strNode += strNextLevel + strValue.substring(iLastIndex,strValue.length()) + "\r\n";
				strNode += strLevel + END_SYMBOL;
			}
			else
			{
				strNode += REF_SYMBOL;
				strValue = StringEscapeUtil.escapeJava(strValue);
				strNode += strValue;
			}
		}
		strNode += "\r\n";
		os.write(strNode.getBytes(Global.ENCODE));
		String strComment = (String)mprtComment.get(nd);
		if(strComment != null)
			os.write(strComment.getBytes(Global.ENCODE));
		if(nd.mvtChild != null)
		{
			for(int iIndex = 0;iIndex < nd.mvtChild.size();iIndex++)
				store(os,(DictionaryNode)nd.mvtChild.elementAt(iIndex),strLevel + NEXT_LEVEL_SYMBOL);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Store dictionary to file
	 * @param strFileName path to file to store data to
	 * @throws IOException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void store(String strFileName) throws IOException
	{
		FileOutputStream os = null;
		try
		{
			os = new FileOutputStream(strFileName);
			store(os);
		}
		finally
		{
			FileUtil.safeClose(os);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Store dictionary to stream
	 * @param os stream to store
	 * @throws IOException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void store(OutputStream os) throws IOException
	{
		synchronized(os)
		{
			String strComment = (String)mprtComment.get(mndRoot);
			if(strComment != null)
				os.write(strComment.getBytes(Global.ENCODE));
			if(mndRoot.mvtChild != null)
			{
				for(int iIndex = 0;iIndex < mndRoot.mvtChild.size();iIndex++)
					store(os,(DictionaryNode)mndRoot.mvtChild.elementAt(iIndex),"");
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * get child node
	 * @param strPath child node path (seperate by dot (.) symbol)
	 * @return node contain value of path
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public DictionaryNode getNode(String strPath)
	{
		return mndRoot.getChild(strPath);
	}
	////////////////////////////////////////////////////////
	/**
	 * get dictionary value with a key without any Parameter
	 * @param strKey dictinary path
	 * @return dictionary value
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public String getStringLike(String strKey)
	{
		DictionaryNode nd = mndRoot.getChildLike(strKey);
		if(nd == null)
			return "";
		return StringUtil.nvl(nd.getValue(),"");
	}
	////////////////////////////////////////////////////////
	/**
	 * get dictionary value with a key without any Parameter
	 * @param strKey dictinary path
	 * @return dictionary value
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public String getString(String strKey)
	{
		if(strKey == null)
			return "";
		DictionaryNode nd = mndRoot.getChild(strKey);
		if(nd == null)
			return "";
		return StringUtil.nvl(nd.getValue(),"");
	}
	////////////////////////////////////////////////////////
	/**
	 * get Value with a key and a Parameter (<%p>)
	 * @param strKey dictinary path
	 * @param strPar first parameter
	 * @return dictionary value
	 * @author Nguyen Thi Thu Trang
	 */
	////////////////////////////////////////////////////////
	public String getString(String strKey,String strPar)
	{
		return StringUtil.replaceAll(getString(strKey),"<%p>",strPar);
	}
	////////////////////////////////////////////////////////
	/**
	 * get Value with a key and 2 Parameter (<%p>)
	 * @param strKey dictinary path
	 * @param strPar1 first parameter
	 * @param strPar2 second parameter
	 * @return dictionary value
	 * @author Nguyen Thi Thu Trang
	 */
	////////////////////////////////////////////////////////
	public String getString(String strKey,String strPar1,String strPar2)
	{
		String strArr[] = {strPar1,strPar2};
		return getString(strKey,strArr);
	}
	////////////////////////////////////////////////////////
	/**
	 * get Value with a key and an array of Parameter (<%p>)
	 * @param strKey dictinary path
	 * @param strPar array of parameter
	 * @return dictionary value
	 * @author Nguyen Thi Thu Trang
	 */
	////////////////////////////////////////////////////////
	public String getString(String strKey,String strPar[])
	{
		String strValueTemp = this.getString(strKey).toString();
		if(strPar != null)
		{
			for(int i = 0;i < strPar.length;i++)
				strValueTemp = StringUtil.replaceAll(strValueTemp,"<%p>",strPar[i],1);
		}
		return strValueTemp;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param e Exception
	 * @return String
	 */
	////////////////////////////////////////////////////////
	public String getString(Exception e)
	{
		if(e == null)
			return "";
		String strKey = e.getClass().getName();
		int iIndex = strKey.lastIndexOf('.');
		if(iIndex >= 0)
		{
			if(iIndex < strKey.length())
				strKey = strKey.substring(iIndex + 1,strKey.length());
			else
				strKey = "";
		}
		String str = getStringLike("{" + strKey + "}");
		if(str != null && str.length() > 0)
		{
			str = StringUtil.replaceAll(str,"<%p>",e.getMessage());
			return str;
		}
		if(e instanceof AppException)
		{
			str = getStringLike(((AppException)e).getReason());
			String[] strPar = StringUtil.toStringArray(StringUtil.nvl(((AppException)e).getInfo(),""));
			if(strPar != null)
			{
				for(int i = 0;i < strPar.length;i++)
					str = StringUtil.replaceAll(str,"<%p>",strPar[i],1);
			}
			if(str != null && str.length() > 0)
				return str;
		}
		else
			str = getStringLike(e.getMessage());
		if(str != null && str.length() > 0)
			return str;
		return e.getMessage();
	}
	////////////////////////////////////////////////////////
	/**
	 * Set dictionary language
	 * @param strLanguage language of dictionary
	 * @author Nguyen Thi Thu Trang
	 */
	////////////////////////////////////////////////////////
	public void setLanguage(String strLanguage)
	{
		this.strLanguage = strLanguage;
	}
	////////////////////////////////////////////////////////
	/**
	 * Get dictionary language
	 * @return language of dictionary
	 * @author Nguyen Thi Thu Trang
	 */
	////////////////////////////////////////////////////////
	public String getLanguage()
	{
		return strLanguage;
	}
	////////////////////////////////////////////////////////
	/**
	 * Set dictionary icon path
	 * @param strIconPath dictionary icon path
	 * @author Nguyen Thi Thu Trang
	 */
	////////////////////////////////////////////////////////
	public void setIconPath(String strIconPath)
	{
		this.strIconPath = strIconPath;
	}
	////////////////////////////////////////////////////////
	/**
	 * Get dictionary icon path
	 * @return dictionary icon path
	 * @author Nguyen Thi Thu Trang
	 */
	////////////////////////////////////////////////////////
	public String getIconPath()
	{
		return strIconPath;
	}
}
