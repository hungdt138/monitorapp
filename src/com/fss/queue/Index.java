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

public class Index
{
	////////////////////////////////////////////////////////
	public Index(Object objKey,Priority priority)
	{
		mobjKey = objKey;
		this.priority = priority;
	}
	////////////////////////////////////////////////////////
	public Index getChild(Object objKey)
	{
		Object[] obj = mvtChild.toArray();
		for(int iIndex = 0;iIndex < obj.length;iIndex++)
		{
			Index idx = (Index)obj[iIndex];
			if(objKey.equals(idx.mobjKey))
				return idx;
		}
		return null;
	}
	////////////////////////////////////////////////////////
	public int getChildIndex(Object objKey)
	{
		Object[] obj = mvtChild.toArray();
		for(int iIndex = 0;iIndex < obj.length;iIndex++)
		{
			Index idx = (Index)obj[iIndex];
			if(objKey.equals(idx.mobjKey))
				return iIndex;
		}
		return -1;
	}
	////////////////////////////////////////////////////////
	public Vector sortChild()
	{
		Vector vt = (Vector)mvtChild.clone();
		Collections.sort(vt,new Comparator()
		{
			public int compare(Object obj1,Object obj2)
			{
				Priority priority1 = ((Index)obj1).priority;
				Priority priority2 = ((Index)obj2).priority;
				if(priority1 instanceof BalancePriority &&
				   priority2 instanceof BalancePriority)
				{
					int iValue1 = ((BalancePriority)priority1).getSurplus();
					int iValue2 = ((BalancePriority)priority2).getSurplus();
					if(iValue1 > iValue2)
						return -1;
					if(iValue1 < iValue2)
						return 1;
				}
				int iValue1 = priority1.getValue();
				int iValue2 = priority2.getValue();
				if(iValue1 > iValue2)
					return -1;
				if(iValue1 < iValue2)
					return 1;
				else
					return 0;
			}
		});
		return vt;
	}
	////////////////////////////////////////////////////////
	public void childUsed(Index idx)
	{
		Object[] obj = mvtChild.toArray();
		for(int iIndex = 0;iIndex < obj.length;iIndex++)
		{
			Index idxChild = (Index)obj[iIndex];
			if(idxChild != idx &&
			   idx.priority instanceof BalancePriority &&
			   idxChild.priority instanceof BalancePriority)
			{
				int iValue = idxChild.priority.getValue();
				((BalancePriority)idxChild.priority).increase(iValue);
				((BalancePriority)idx.priority).decrease(iValue);
			}
		}
	}
	////////////////////////////////////////////////////////
	public void childIgnored(Index idx)
	{
		if(idx.priority instanceof BalancePriority)
			((BalancePriority)idx.priority).reset();
	}
	////////////////////////////////////////////////////////
	public Object mobjKey;
	public Vector mvtChild = new Vector();
	public Priority priority;
}
