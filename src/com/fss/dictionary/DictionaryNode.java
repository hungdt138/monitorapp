package com.fss.dictionary;

import java.util.*;

/**
 * <p>Title: DictionaryNode</p>
 * <p>Description: Object conatain dictionary element data</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class DictionaryNode
{
	////////////////////////////////////////////////////////
	// Constant
	////////////////////////////////////////////////////////
	public static final int TYPE_VALUE = 0;
	public static final int TYPE_REFERENCE = 1;
	public static final int TYPE_VECTOR = 2;
	public static final int TYPE_LONG_STRING = 3;
	public static final String SEPARATOR_SYMBOL = ".";
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	public String mstrName;
	public String mstrValue;
	public int miType = TYPE_VALUE;
	public Vector mvtChild;
	public DictionaryNode mndRoot;
	////////////////////////////////////////////////////////
	/**
	 * Get dictionary node from child list
	 * @param strPath String
	 * @param bCreateIfNotExist boolean
	 * @return DictionaryNode
	 * @author Thai Hoang Hiep - Date: 22/04/2003
	 */
	////////////////////////////////////////////////////////
	public DictionaryNode getChild(String strPath,boolean bCreateIfNotExist)
	{
		DictionaryNode ndReturn = null;
		int iIndex = strPath.indexOf(SEPARATOR_SYMBOL);
		if(iIndex < 0)
		{
			if(mvtChild != null)
			{
				strPath = strPath.trim();
				for(int iChildIndex = 0;iChildIndex < mvtChild.size();iChildIndex++)
				{
					DictionaryNode ndChild = (DictionaryNode)mvtChild.elementAt(iChildIndex);
					if(ndChild.mstrName.equals(strPath))
						return ndChild;
				}
			}
			if(bCreateIfNotExist)
			{
				ndReturn = new DictionaryNode();
				ndReturn.mstrName = strPath;
				if(mvtChild == null)
					mvtChild = new Vector();
				mvtChild.addElement(ndReturn);
			}
		}
		else
		{
			String strNodeName = strPath.substring(0,iIndex).trim();
			String strSubPath = strPath.substring(iIndex + 1,strPath.length()).trim();
			ndReturn = getChild(strNodeName);
			if(ndReturn != null && strSubPath.length() > 0)
				ndReturn = ndReturn.getChild(strSubPath);
		}
		if(ndReturn == null && this.mstrValue != null && this.miType == TYPE_REFERENCE)
			ndReturn = mndRoot.getChild(this.mstrValue).getChild(strPath);
		if(bCreateIfNotExist && ndReturn == null)
		{
			ndReturn = new DictionaryNode();
			ndReturn.mstrName = strPath;
			if(mvtChild == null)
				mvtChild = new Vector();
			mvtChild.addElement(ndReturn);
		}
		return ndReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 * Get dictionary node from child list
	 * @param strPath Path to child node
	 * @return dictionary node from child list
	 * @throws Exception if error occured
	 * @author Thai Hoang Hiep - Date: 22/04/2003
	 */
	////////////////////////////////////////////////////////
	public DictionaryNode getChild(String strPath)
	{
		return getChild(strPath,false);
	}
	////////////////////////////////////////////////////////
	/**
	 * Get dictionary node from child list if child path start with path
	 * @param strPath Path to child node
	 * @return dictionary node from child list
	 * @throws Exception if error occured
	 * @author Thai Hoang Hiep - Date: 22/04/2003
	 */
	////////////////////////////////////////////////////////
	public DictionaryNode getChildLike(String strPath)
	{
		if(strPath == null)
			return null;
		DictionaryNode ndReturn = null;
		int iIndex = strPath.indexOf(SEPARATOR_SYMBOL);
		if(iIndex < 0)
		{
			if(mvtChild != null)
			{
				strPath = strPath.trim();
				for(int iChildIndex = 0;iChildIndex < mvtChild.size();iChildIndex++)
				{
					DictionaryNode ndChild = (DictionaryNode)mvtChild.elementAt(iChildIndex);
					if(strPath.startsWith(ndChild.mstrName))
						return ndChild;
				}
			}
		}
		else
		{
			String strNodeName = strPath.substring(0,iIndex).trim();
			String strSubPath = strPath.substring(iIndex + 1,strPath.length()).trim();
			ndReturn = getChildLike(strNodeName);
			if(ndReturn != null && strSubPath.length() > 0)
			{
				DictionaryNode nd = ndReturn.getChildLike(strSubPath);
				if(nd != null)
					ndReturn = nd;
			}
		}
		if(ndReturn == null && this.mstrValue != null && this.miType == TYPE_REFERENCE)
			ndReturn = mndRoot.getChildLike(this.mstrValue).getChildLike(strPath);
		return ndReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 * Return list of child of this node
	 * @return Vector contain list of child of this node
	 * @throws Exception if error occured
	 * @author Thai Hoang Hiep - Date: 22/04/2003
	 */
	////////////////////////////////////////////////////////
	public Vector getChildList()
	{
		return getChildList(this);
	}
	////////////////////////////////////////////////////////
	/**
	 * Return list of child of this node
	 * @param ndFirst first node, used to prevent unlimited loop
	 * @return Vector contain list of child of this node
	 * @throws Exception if error occured
	 * @author Thai Hoang Hiep - Date: 22/04/2003
	 */
	////////////////////////////////////////////////////////
	public Vector getChildList(DictionaryNode ndFirst)
	{
		Vector vtReturn = new Vector();
		if(mvtChild != null)
			vtReturn.addAll(mvtChild);
		if(this.mstrValue != null && this.miType == TYPE_REFERENCE)
		{
			DictionaryNode ndReference = mndRoot.getChild(this.mstrValue);
			if(ndReference != null)
			{
				if(ndReference != ndFirst)
					vtReturn.addAll(ndReference.getChildList(ndFirst));
			}
		}
		return vtReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 * Get value of dictionary node
	 * @return String contain value of this node
	 * @throws Exception if error occured
	 * @author Thai Hoang Hiep - Date: 22/04/2003
	 */
	////////////////////////////////////////////////////////
	public String getValue()
	{
		if(this.mstrValue != null && this.miType == TYPE_REFERENCE)
		{
			DictionaryNode ndReference = mndRoot.getChild(this.mstrValue);
			if(ndReference != null)
			{
				String strReturn = ndReference.getValue();
				return strReturn;
			}
			else
				return null;
		}
		String strReturn = "";
		if(this.mstrValue != null)
			strReturn = mstrValue;
		return strReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 * Get value of dictionary node
	 * @return String contain value of this node
	 * @throws Exception if error occured
	 * @author Thai Hoang Hiep - Date: 22/04/2003
	 */
	////////////////////////////////////////////////////////
	public Vector getVector()
	{
		Vector vtReturn = new Vector();
		for(int iIndex = 0;mvtChild != null && iIndex < mvtChild.size();iIndex++)
		{
			DictionaryNode nd = (DictionaryNode)mvtChild.elementAt(iIndex);
			if(nd.mvtChild != null && nd.mvtChild.size() > 0)
				vtReturn.addElement(nd.getVector());
			else
				vtReturn.addElement(nd.getValue());
		}
		return vtReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 * Merge with new node
	 * @param ndDes DictionaryNode
	 * @author Thai Hoang Hiep - Date: 22/04/2003
	 */
	////////////////////////////////////////////////////////
	public void merge(DictionaryNode ndDes)
	{
		if(ndDes == null || ndDes.mvtChild == null)
			return;
		for(int iDesIndex = 0;iDesIndex < ndDes.mvtChild.size();iDesIndex++)
		{
			DictionaryNode ndDesChild = (DictionaryNode)ndDes.mvtChild.elementAt(iDesIndex);
			DictionaryNode ndSrcChild = getChild(ndDesChild.mstrName);
			if(ndSrcChild == null)
				mvtChild.addElement(ndDesChild);
			else if(ndDesChild.mvtChild != null)
				ndSrcChild.merge(ndDesChild);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * set child value
	 * @param strName String
	 * @param strValue String
	 * @return DictionaryNode
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public DictionaryNode setChildValue(String strName,String strValue)
	{
		DictionaryNode nd = getChild(strName);
		if(nd == null)
		{
			nd = new DictionaryNode();
			nd.mstrName = strName;
			if(mvtChild == null)
				mvtChild = new Vector();
			mvtChild.addElement(nd);
		}
		nd.mstrValue = strValue;
		return nd;
	}
	////////////////////////////////////////////////////////
	/**
	 * set child value
	 * @param strName String
	 * @param vtValue Vector
	 * @return DictionaryNode
	 */
	////////////////////////////////////////////////////////
	public DictionaryNode setChildVector(String strName,Vector vtValue)
	{
		DictionaryNode nd = setChildValue(strName,"");
		nd.miType = TYPE_VECTOR;
		for(int iIndex = 0;iIndex < vtValue.size();iIndex++)
		{
			Object obj = vtValue.elementAt(iIndex);
			if(obj != null && obj instanceof Vector)
				nd.setChildVector(String.valueOf(iIndex),(Vector)obj);
			else
				nd.setChildValue(String.valueOf(iIndex),(String)obj);
		}
		return nd;
	}
	////////////////////////////////////////////////////////
	/**
	 * set child value
	 * @param strName String
	 * @param strValue String
	 * @param vtChild Vector
	 * @return DictionaryNode
	 */
	////////////////////////////////////////////////////////
	public DictionaryNode setChildValue(String strName,String strValue,Vector vtChild)
	{
		DictionaryNode nd = getChild(strName);
		if(nd == null)
		{
			nd = new DictionaryNode();
			nd.mstrName = strName;
			if(mvtChild == null)
				mvtChild = new Vector();
			mvtChild.addElement(nd);
		}
		nd.mstrValue = strValue;
		nd.mvtChild = vtChild;
		return nd;
	}
}
