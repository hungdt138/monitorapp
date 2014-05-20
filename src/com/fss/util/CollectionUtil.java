package com.fss.util;

import java.util.*;

/**
 * <p>Title: StringUtil</p>
 * <p>Description: Utility for string processing</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class CollectionUtil
{
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param vtData Vector
	 * @return Vector
	 */
	////////////////////////////////////////////////////////
	public static Vector cloneVector(Vector vtData)
	{
		Vector vtReturn = new Vector();
		for(int iIndex = 0;iIndex < vtData.size();iIndex++)
		{
			Object objValue = vtData.elementAt(iIndex);
			if(objValue instanceof Vector)
				vtReturn.addElement(CollectionUtil.cloneVector((Vector)objValue));
			else
				vtReturn.addElement(objValue);
		}
		return vtReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param lstSrc List
	 * @param lstDes List
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public static boolean isSimilar(List lstSrc,List lstDes)
	{
		if(lstSrc != lstDes)
		{
			if(lstSrc.size() != lstDes.size())
				return false;
			for(int iIndex = 0;iIndex < lstSrc.size();iIndex++)
			{
				Object objSrc = lstSrc.get(iIndex);
				Object objDes = lstDes.get(iIndex);
				if(objSrc == null && objDes == null)
					continue;
				if(objSrc == null)
					return false;
				if(objDes == null)
					return false;
				if(objSrc.getClass() != objDes.getClass())
					return false;
				if(objSrc instanceof List && !isSimilar((List)objSrc,(List)objDes))
					return false;
				if(!objSrc.equals(objDes))
					return false;
			}
		}
		return true;
	}
}
