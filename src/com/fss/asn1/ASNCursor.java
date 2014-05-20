package com.fss.asn1;

import java.io.*;
import java.util.*;

import com.fss.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class ASNCursor
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	public ASNDefinition mdefChoice = null;
	public ASNData mdatChoice = null;
	public Vector vctChoice = null;
	public ASNDefinition mdefCursor[] = null;
	public ASNDefinition[] mdefFieldList = null;
	public ASNData mdatCursor[] = null;
	public ASNData[] mdatFieldList = null;
	public String[] mstrFieldList = null;
	public ASNFormat mfmt = null;
	public int miTotalRecord;
	public int miLevel = 0;
	////////////////////////////////////////////////////////
	// Purpose: Move cursor to next record
	// Outputs: False if cursor reach to end of tree, other wise true
	// Author: HiepTH
	////////////////////////////////////////////////////////
	private boolean next()
	{
		while(true)
		{
			if(miLevel >= mdefCursor.length || mdatCursor[miLevel] == null)
			{
				if(miLevel == 0)
					return false;

				// Move next parent if child not found
				ASNData dat = mdatCursor[miLevel - 1].mpNext;
				if(dat == null)
					return false;
				mdatCursor[miLevel - 1] = dat;
				mdatCursor[miLevel] = mdatCursor[miLevel - 1].getChildOneLevel(mdefCursor[miLevel]);
			}
			else if(mdatCursor[miLevel].mpDefinition != mdefCursor[miLevel])
			{
				// Move next if type of datcursor not equals to def cursor
				mdatCursor[miLevel] = mdatCursor[miLevel].mpNext;
			}
			else
			{
				if(miLevel == mdefCursor.length - 1)
				{
					// Make sure cursor is same as choice
					if(mdefChoice == mdefCursor[miLevel])
						mdatChoice = mdatCursor[miLevel];
					else
					{
						mdatChoice = mdatCursor[miLevel].getChildOneLevel(mdefChoice);
						if(mdatChoice == null) // Not same as choice -> next choice
						{
							mdatCursor[miLevel] = mdatCursor[miLevel].mpNext;
							continue;
						}
					}

					// Fill data if found
					fillData();

					// Move next
					mdatCursor[miLevel] = mdatCursor[miLevel].mpNext;
					return true;
				}
				else
				{
					miLevel++;
					mdatCursor[miLevel] = mdatCursor[miLevel - 1].getChildOneLevel(mdefCursor[miLevel]);
					if(next())
						return true;

					// Move next parent if child not found
					miLevel--;
					mdatCursor[miLevel] = mdatCursor[miLevel].mpNext;
				}
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Fill data from dat choice to field list
	 */
	////////////////////////////////////////////////////////
	public void fillData()
	{
		// Fill data into dat field list
		for(int iIndex = 0;iIndex < mdefFieldList.length;iIndex++)
			fillRecord(iIndex);
	}
	////////////////////////////////////////////////////////
	public void fillRecord(int iIndex)
	{
		mdatFieldList[iIndex] = null;
		mstrFieldList[iIndex] = "";
		if(mdefFieldList[iIndex] != null)
		{
			if(mdefFieldList[iIndex].miType != ASNDefinition.ASN_CONST)
			{
				mdatFieldList[iIndex] = mdatChoice.getChild(mdefFieldList[iIndex]);
				if(mdatFieldList[iIndex] != null)
					mstrFieldList[iIndex] = StringUtil.nvl(mfmt.format(mdatFieldList[iIndex].mpDefinition,mdatFieldList[iIndex]),"");
			}
			else
				mstrFieldList[iIndex] = mdefFieldList[iIndex].mstrValue;
		}
	}
	////////////////////////////////////////////////////////
	// Purpose: Export query data to output
	// Inputs: Output stream, seperator symbol
	// Outputs: Exception throw if error occured,
	//  number of record exported
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public int storeText(OutputStream out,String strSeperator) throws IOException
	{
		int iRecordIndex = 0;
		while(next())
		{
			storeRecord(out,strSeperator);
			iRecordIndex++;
		}
		return iRecordIndex;
	}
	////////////////////////////////////////////////////////
	// Purpose: Write data to output stream
	// Inputs: Output stream, seperator symbol
	// Outputs: Exception throw if error occured,
	//  number of record exported
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public void storeRecord(OutputStream out,String strSeperator) throws IOException
	{
		StringBuffer strLine = new StringBuffer();
		for(int iIndex = 0;iIndex < mstrFieldList.length;iIndex++)
		{
			strLine.append(mstrFieldList[iIndex]);
			strLine.append(strSeperator);
		}
		strLine.append("\r\n");
		out.write(strLine.toString().getBytes());
	}
}
