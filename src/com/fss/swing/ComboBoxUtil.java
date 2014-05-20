package com.fss.swing;

import java.util.*;
import javax.swing.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: FTL-FSS-FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class ComboBoxUtil
{
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param vtData Vector
	 * @param iComboIndex int
	 * @param iVectorIndex int
	 * @param cbo JComboBox
	 * @param vt Vector
	 * @param bClear boolean
	 * @param bHaveNull boolean
	 * @throws Exception
	 */
	public static void fillValue(Vector vtData,int iComboIndex,int iVectorIndex,JComboBox cbo,Vector vt,boolean bClear,boolean bHaveNull) throws Exception
	{
		// Clear
		if(bClear)
		{
			vt.clear();
			cbo.removeAllItems();
		}

		// Add null value
		if(bHaveNull)
		{
			vt.addElement("");
			cbo.addItem("");
		}

		// Fill value
		for(int iRowIndex = 0;iRowIndex < vtData.size();iRowIndex++)
		{
			Vector vtResultRow = (Vector)vtData.elementAt(iRowIndex);
			vt.addElement(vtResultRow.elementAt(iVectorIndex));
			cbo.addItem(vtResultRow.elementAt(iComboIndex));
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param vtData Vector
	 * @param cbo JComboBox
	 * @param vt Vector
	 * @param bClear boolean
	 * @param bHaveNull boolean
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public static void fillValue(Vector vtData,JComboBox cbo,Vector vt,boolean bClear,boolean bHaveNull) throws Exception
	{
		fillValue(vtData,1,0,cbo,vt,bClear,bHaveNull);
	}
	////////////////////////////////////////////////////////
	public static void fillValue(Vector vtData,JComboBox cbo,Vector vt) throws Exception
	{
		fillValue(vtData,cbo,vt,true,false);
	}
}
