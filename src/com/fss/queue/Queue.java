package com.fss.queue;

import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class Queue
{
	////////////////////////////////////////////////////////
	public static final String MESSAGE_DATE = "MDT";
	////////////////////////////////////////////////////////
	private String[] mstrIndex;
	private Index midxRoot;
	private int miMaxQueueSize = 10000;
	private int miQueueSize = 0;
	private int miTimeOut = 60000;
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String[]
	 */
	////////////////////////////////////////////////////////
	public Index getRootIndex()
	{
		return midxRoot;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String[]
	 */
	////////////////////////////////////////////////////////
	public String[] getIndexKey()
	{
		return mstrIndex;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return int
	 */
	////////////////////////////////////////////////////////
	public int getTimeOut()
	{
		return miTimeOut;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param iTimeOut int
	 */
	////////////////////////////////////////////////////////
	public void setTimeOut(int iTimeOut)
	{
		miTimeOut = iTimeOut;
	}
	////////////////////////////////////////////////////////
	/**
	 * Compare attribute
	 * @param prtSrc Hashtable
	 * @param prtDest Hashtable
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	protected boolean matchAttributes(Hashtable prtSrc,Hashtable prtDest)
	{
		return matchAttributes(prtSrc,prtDest,true);
	}
	////////////////////////////////////////////////////////
	protected boolean matchAttributes(Hashtable prtSrc,Hashtable prtDest,boolean bCheckAll)
	{
		if(prtDest != null)
		{
			Enumeration enm = prtDest.keys();
			while(enm.hasMoreElements())
			{
				String strKey = enm.nextElement().toString();
				if(bCheckAll || shouldCheckKey(strKey))
				{
					Object objValue = prtDest.get(strKey);
					if(objValue != null && !objValue.equals(""))
					{
						Object obj = prtSrc.get(strKey);
						if(objValue != obj && !objValue.equals(obj))
							return false;
					}
				}
			}
		}
		return true;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strKey String
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	protected boolean shouldCheckKey(String strKey)
	{
		for(int iIndex = 0;iIndex < mstrIndex.length;iIndex++)
		{
			if(mstrIndex[iIndex].equals(strKey))
				return false;
		}
		return true;
	}
	////////////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////////////
	public Queue(String[] strIndex)
	{
		mstrIndex = strIndex;
		midxRoot = new Index(null,new SimplePriority());
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param msg Attributable
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void attach(Attributable msg) throws Exception
	{
		// Get Hashtable path
		if(miMaxQueueSize >= 0 && getQueueSize() >= miMaxQueueSize)
			throw new Exception("Maximum queue element (" + miMaxQueueSize + ") exceeded");
		Index idxKey = midxRoot;
		int iLevel = 0;
		while(iLevel < mstrIndex.length)
		{
			String strKey = mstrIndex[iLevel];
			Object objValue = msg.getAttribute(strKey);
			if(objValue == null)
				objValue = "";
			Index idxChild = idxKey.getChild(objValue);
			if(idxChild == null)
			{
				Priority priority = null;
				if(objValue instanceof ImportantObject)
					priority = ((ImportantObject)objValue).getPriority();
				else
					priority = new SimplePriority();
				idxChild = new Index(objValue,priority);
				idxKey.mvtChild.addElement(idxChild);
			}
			idxKey = idxChild;

			if(iLevel >= mstrIndex.length - 1)
			{
				// Storage
				Date dt = (Date)msg.getAttribute(MESSAGE_DATE);
				if(dt == null)
					msg.setAttribute(MESSAGE_DATE,new java.util.Date());
				idxChild.mvtChild.addElement(msg);
				miQueueSize++;
			}
			iLevel++;
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return Attributable
	 */
	////////////////////////////////////////////////////////
	public Attributable detach()
	{
		return detach(null);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param prtAttribute Hashtable
	 * @return Attributable
	 */
	////////////////////////////////////////////////////////
	public Attributable detach(Hashtable prtAttribute)
	{
		return detach(prtAttribute,true);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param prtAttribute Hashtable
	 * @param bRemove boolean
	 * @return Attributable
	 */
	////////////////////////////////////////////////////////
	public Attributable detach(Hashtable prtAttribute,boolean bRemove)
	{
		if(miQueueSize <= 0)
			return null;
		return detach(prtAttribute,midxRoot,0,bRemove);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param prtAttribute Hashtable
	 * @param idxKey Index
	 * @param iLevel int
	 * @param bRemove boolean
	 * @return Attributable
	 */
	////////////////////////////////////////////////////////
	private Attributable detach(Hashtable prtAttribute,Index idxKey,int iLevel,boolean bRemove)
	{
		if(miQueueSize <= 0)
			return null;
		if(iLevel < mstrIndex.length)
		{
			String strKey = mstrIndex[iLevel];
			Object objValue = null;
			if(prtAttribute != null)
				objValue = prtAttribute.get(strKey);
			if(objValue != null)
			{
				Index idxChild = idxKey.getChild(objValue);
				if(idxChild == null)
					return null;
				Attributable msg = detach(prtAttribute,idxChild,iLevel + 1,bRemove);
				if(msg != null)
				{
					idxKey.childUsed(idxChild);
					return msg;
				}
				else
					idxKey.childIgnored(idxChild);
			}
			else
			{
				Vector vt = idxKey.sortChild();
				for(int iIndex = 0;iIndex < vt.size();iIndex++)
				{
					Index idxChild = (Index)vt.elementAt(iIndex);
					Attributable msg = detach(prtAttribute,idxChild,iLevel + 1,bRemove);
					if(msg != null)
					{
						idxKey.childUsed(idxChild);
						return msg;
					}
					else
						idxKey.childIgnored(idxChild);
				}
			}
		}
		else
		{
			synchronized(idxKey.mvtChild)
			{
				for(int iIndex = 0;iIndex < idxKey.mvtChild.size();iIndex++)
				{
					Attributable msg = (Attributable)idxKey.mvtChild.elementAt(iIndex);
					if(matchAttributes(msg.getAttributes(),prtAttribute,false))
					{
						if(bRemove)
						{
							if(idxKey.mvtChild.removeElement(msg))
							{
								iIndex--;
								miQueueSize--;
							}
						}

						if(miTimeOut > 0)
						{
							Date dt = (Date)msg.getAttribute(MESSAGE_DATE);
							if(dt.getTime() + miTimeOut < new Date().getTime())
								msg = onMessageTimedOut(msg);
						}
						return msg;
					}
				}
			}
		}
		return null;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param msg Attributable
	 * @return Attributable
	 */
	////////////////////////////////////////////////////////
	protected Attributable onMessageTimedOut(Attributable msg)
	{
		return null;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param prtAttribute Hashtable
	 * @param excReason Exception
	 */
	////////////////////////////////////////////////////////
	public void remove(Hashtable prtAttribute,Exception excReason)
	{
		remove(prtAttribute,midxRoot,0,excReason);
	}
	////////////////////////////////////////////////////////
	private void remove(Hashtable prtAttribute,Index idxKey,int iLevel,Exception excReason)
	{
		if(iLevel < mstrIndex.length)
		{
			String strKey = mstrIndex[iLevel];
			Object objValue = null;
			if(prtAttribute != null)
				objValue = prtAttribute.get(strKey);
			synchronized(idxKey.mvtChild)
			{
				if(objValue != null)
				{
					int iIndex = idxKey.getChildIndex(objValue);
					if(iIndex < 0)
						return;
					Index idxChild = (Index)idxKey.mvtChild.elementAt(iIndex);
					remove(prtAttribute,idxChild,iLevel + 1,excReason);
					if(idxChild.mvtChild.size() <= 0)
						idxKey.mvtChild.removeElement(idxChild);
				}
				else
				{
					for(int iIndex = 0;iIndex < idxKey.mvtChild.size();iIndex++)
					{
						Index idxChild = (Index)idxKey.mvtChild.elementAt(iIndex);
						remove(prtAttribute,idxChild,iLevel + 1,excReason);
						if(idxChild.mvtChild.size() <= 0)
						{
							idxKey.mvtChild.removeElement(idxChild);
							iIndex--;
						}
					}
				}
			}
		}
		else
		{
			synchronized(idxKey.mvtChild)
			{
				for(int iIndex = 0;iIndex < idxKey.mvtChild.size();iIndex++)
				{
					Attributable msg = (Attributable)idxKey.mvtChild.elementAt(iIndex);
					if(matchAttributes(msg.getAttributes(),prtAttribute,false))
					{
						onRemoveMessage(msg,excReason);
						idxKey.mvtChild.removeElement(msg);
						iIndex--;
						miQueueSize--;
					}
				}
			}
		}
	}
	////////////////////////////////////////////////////////
	protected void onRemoveMessage(Attributable msg,Exception excReason)
	{
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param prtAttribute Hashtable
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean isAvailable(Hashtable prtAttribute)
	{
		return isAvailable(prtAttribute,midxRoot,0);
	}
	////////////////////////////////////////////////////////
	private boolean isAvailable(Hashtable prtAttribute,Index idxKey,int iLevel)
	{
		if(iLevel < mstrIndex.length)
		{
			String strKey = mstrIndex[iLevel];
			Object objValue = null;
			if(prtAttribute != null)
				objValue = prtAttribute.get(strKey);
			if(objValue != null)
			{
				int iIndex = idxKey.getChildIndex(objValue);
				if(iIndex < 0)
					return false;
				Index idxChild = (Index)idxKey.mvtChild.elementAt(iIndex);
				if(isAvailable(prtAttribute,idxChild,iLevel + 1))
					return true;
			}
			else
			{
				for(int iIndex = 0;iIndex < idxKey.mvtChild.size();iIndex++)
				{
					Index idxChild = (Index)idxKey.mvtChild.elementAt(iIndex);
					if(isAvailable(prtAttribute,idxChild,iLevel + 1))
						return true;
				}
			}
		}
		else
		{
			for(int iIndex = 0;iIndex < idxKey.mvtChild.size();iIndex++)
			{
				Attributable msg = (Attributable)idxKey.mvtChild.elementAt(iIndex);
				if(matchAttributes(msg.getAttributes(),prtAttribute,false))
					return true;
			}
			return false;
		}
		return false;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return int
	 */
	////////////////////////////////////////////////////////
	public int getQueueSize()
	{
		return miQueueSize;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param prtAttribute Hashtable
	 * @return int
	 */
	////////////////////////////////////////////////////////
	public int getQueueSize(Hashtable prtAttribute)
	{
		if(miQueueSize <= 0)
			return miQueueSize;
		return getQueueSize(prtAttribute,midxRoot,0);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param prtAttribute Hashtable
	 * @param idxKey Index
	 * @param iLevel int
	 * @return int
	 */
	////////////////////////////////////////////////////////
	private int getQueueSize(Hashtable prtAttribute,Index idxKey,int iLevel)
	{
		int iReturn = 0;
		if(iLevel < mstrIndex.length)
		{
			String strKey = mstrIndex[iLevel];
			Object objValue = null;
			if(prtAttribute != null)
				objValue = prtAttribute.get(strKey);
			if(objValue != null)
			{
				Index idxChild = idxKey.getChild(objValue);
				if(idxChild == null)
					return 0;
				iReturn += getQueueSize(prtAttribute,idxChild,iLevel + 1);
			}
			else
			{
				Vector vt = idxKey.sortChild();
				for(int iIndex = 0;iIndex < vt.size();iIndex++)
				{
					Index idxChild = (Index)vt.elementAt(iIndex);
					iReturn += getQueueSize(prtAttribute,idxChild,iLevel + 1);
				}
			}
		}
		else
		{
			synchronized(idxKey.mvtChild)
			{
				for(int iIndex = 0;iIndex < idxKey.mvtChild.size();iIndex++)
				{
					Attributable msg = (Attributable)idxKey.mvtChild.elementAt(iIndex);
					if(matchAttributes(msg.getAttributes(),prtAttribute,false))
						iReturn++;
				}
			}
		}
		return iReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return int
	 */
	////////////////////////////////////////////////////////
	public int getMaxQueueSize()
	{
		return miMaxQueueSize;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param iMaxQueueSize int
	 */
	////////////////////////////////////////////////////////
	public void setMaxQueueSize(int iMaxQueueSize)
	{
		miMaxQueueSize = iMaxQueueSize;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	public String toString()
	{
		return toString(midxRoot,0,"");
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param idx Index
	 * @param iLevel int
	 * @param strTab String
	 * @return String
	 */
	////////////////////////////////////////////////////////
	private String toString(Index idx,int iLevel,String strTab)
	{
		StringBuffer str = new StringBuffer();
		for(int iIndex = 0;iIndex < idx.mvtChild.size();iIndex++)
		{
			Index idxChild = (Index)idx.mvtChild.elementAt(iIndex);
			str.append(strTab);
			str.append(idxChild.mobjKey);
			if(iLevel >= mstrIndex.length - 1)
			{
				str.append(":");
				str.append(idxChild.mvtChild.size());
				str.append("\r\n");
			}
			else
			{
				str.append("\r\n");
				str.append(toString(idxChild,iLevel + 1,strTab + "\t"));
			}
		}
		return str.toString();
	}
}
