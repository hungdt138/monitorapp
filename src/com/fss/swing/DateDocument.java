package com.fss.swing;

import java.util.*;
import java.text.*;
import javax.swing.text.*;

import com.fss.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class DateDocument extends PlainDocument
{
	////////////////////////////////////////////////////////
	protected static char mchrSeperator = '/';
	protected static DecimalFormat FORMAT_00 = new DecimalFormat("0");
	protected String mstrFormat;
	protected JTextComponent txtParent;
	public int iDValue = -1;
	public int iMValue = -1;
	public int iYValue = -1;
	////////////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////////////
	public DateDocument(JTextComponent parent)
	{
		txtParent = parent;
		setFormat("DMY");
	}
	////////////////////////////////////////////////////////
	public DateDocument(JTextComponent parent,String strFormat)
	{
		txtParent = parent;
		setFormat(strFormat);
	}
	////////////////////////////////////////////////////////
	// Format property
	////////////////////////////////////////////////////////
	public void setFormat(String strFormat)
	{
		mstrFormat = strFormat;
	}
	////////////////////////////////////////////////////////
	public String getFormat()
	{
		return mstrFormat;
	}
	////////////////////////////////////////////////////////
	// Implements
	////////////////////////////////////////////////////////
	public void insertString(int iOffset,String strInsert,AttributeSet attrib) throws BadLocationException
	{
		if(strInsert.length() == 1)
		{
			// Filter
			char ch = strInsert.charAt(0);
			if((ch < '0' || ch > '9') && ch != mchrSeperator)
				return;
			int iAdditionValue = ch - '0';

			// Get field value
			String strContent = getText(0,getLength());
			boolean bSeperatorPos = false;
			if(strContent.length() > iOffset && strContent.charAt(iOffset) == mchrSeperator)
				bSeperatorPos = true;
			Vector vtFieldList = StringUtil.toStringVector(strContent,String.valueOf(mchrSeperator));

			// Get position properties
			int iPosIndex = 0;
			int iPosOffset = 0;
			int iCurrentOffset = 0;
			String strCurrentValue = "";
			boolean bFound = false;
			while(!bFound && iPosIndex < vtFieldList.size())
			{
				iPosOffset = iOffset - iCurrentOffset;
				strCurrentValue = ((String)vtFieldList.elementAt(iPosIndex));
				iCurrentOffset += strCurrentValue.length();
				if(iOffset <= iCurrentOffset)
					bFound = true;
				else
					iPosIndex++;
				iCurrentOffset++;
			}
			if(iPosIndex >= mstrFormat.length())
				return;
			char chrPosType = mstrFormat.charAt(iPosIndex);
			if(strCurrentValue.endsWith("" + mchrSeperator) && iPosOffset >= strCurrentValue.length())
			{
				if(iPosOffset < strCurrentValue.length())
					iOffset++;
				iPosOffset = 0;
				iPosIndex++;
				if(iPosIndex >= mstrFormat.length())
					return;
				chrPosType = mstrFormat.charAt(iPosIndex);
			}
			else if((chrPosType == 'D' || chrPosType == 'M') && iPosOffset == 2)
			{
				iPosOffset = 0;
				iPosIndex++;
				if(iPosIndex >= mstrFormat.length())
					return;
				chrPosType = mstrFormat.charAt(iPosIndex);
				iOffset++;
			}
			else if(chrPosType == 'Y' && iPosOffset == 4)
			{
				iPosOffset = 0;
				iPosIndex++;
				if(iPosIndex >= mstrFormat.length())
					return;
				chrPosType = mstrFormat.charAt(iPosIndex);
				iOffset++;
			}

			// Test case
			iDValue = getFieldByType('D',vtFieldList);
			iMValue = getFieldByType('M',vtFieldList);
			iYValue = getFieldByType('Y',vtFieldList);
			String strValue = "";
			if(iPosIndex < vtFieldList.size())
				strValue = vtFieldList.elementAt(iPosIndex).toString();
			if(chrPosType == 'D')
			{
				if(iDValue < 0)
				{
					if(ch == mchrSeperator)
					{
						if(iOffset > getLength())
							super.insertString(getLength(),strInsert,attrib);
						return;
					}
					else
						iDValue = iAdditionValue;
				}
				else if(ch != mchrSeperator)
				{
					String strNewDValue = strValue.substring(0,iPosOffset) + iAdditionValue + strValue.substring(iPosOffset,strValue.length());
					int iNewDValue = Integer.parseInt(strNewDValue);
					boolean bEOF = false;
					if(iPosOffset < strValue.length())
					{
						if(iNewDValue > 31)
						{
							strNewDValue = strValue.substring(0,iPosOffset) + iAdditionValue;
							if(iPosOffset < strValue.length() - 1)
								strNewDValue += strValue.substring(iPosOffset + 1,strValue.length());
							else
								bEOF = true;
							iNewDValue = Integer.parseInt(strNewDValue);
						}
						if(iNewDValue > 31)
						{
							iNewDValue = iAdditionValue;
							strNewDValue = String.valueOf(iAdditionValue);
						}
					}
					else
						bEOF = true;

					if(iNewDValue <= 31)
					{
						if(strNewDValue.length() > 2)
						{
							strNewDValue = strNewDValue.substring(0,2);
							iNewDValue = Integer.parseInt(strNewDValue);
						}
						if(!strNewDValue.equals("00"))
						{
							iDValue = iNewDValue;
							vtFieldList.setElementAt(strNewDValue,iPosIndex);
						}
					}
					else if(bEOF && iPosIndex < mstrFormat.length() - 1)
					{
						if(iOffset == getLength())
							super.insertString(iOffset,"" + mchrSeperator,null);
						insertString(iOffset + 1,strInsert,null);
						return;
					}
				}
				else
				{
					if(bSeperatorPos && iOffset < getLength())
						txtParent.setCaretPosition(txtParent.getCaretPosition() + 1);
					else if(iOffset >= getLength() && iPosIndex < mstrFormat.length() - 1)
						super.insertString(getLength(),"" + mchrSeperator,null);
					else if(iOffset > 0)
					{
						// Let try one case
						String str = strContent.substring(0,iOffset) + strInsert + strContent.substring(iOffset,strContent.length());
						vtFieldList = StringUtil.toStringVector(str,String.valueOf(mchrSeperator));

						// Test case
						iDValue = getFieldByType('D',vtFieldList);
						iMValue = getFieldByType('M',vtFieldList);
						iYValue = getFieldByType('Y',vtFieldList);
						if(vtFieldList.size() <= 3 && iDValue < 31 && iMValue < 12 && iYValue < 10000)
							super.insertString(iOffset,strInsert,null);
					}
					return;
				}
			}
			else if(chrPosType == 'M')
			{
				if(iMValue < 0)
				{
					if(ch == mchrSeperator)
					{
						if(iOffset > getLength())
							super.insertString(getLength(),strInsert,attrib);
						return;
					}
					else
						iMValue = iAdditionValue;
				}
				else if(ch != mchrSeperator)
				{
					String strNewMValue = strValue.substring(0,iPosOffset) + iAdditionValue + strValue.substring(iPosOffset,strValue.length());
					int iNewMValue = Integer.parseInt(strNewMValue);
					boolean bEOF = false;
					if(iPosOffset < strValue.length())
					{
						if(iNewMValue > 12)
						{
							strNewMValue = strValue.substring(0,iPosOffset) + iAdditionValue;
							if(iPosOffset < strValue.length() - 1)
								strNewMValue += strValue.substring(iPosOffset + 1,strValue.length());
							else
								bEOF = true;
							iNewMValue = Integer.parseInt(strNewMValue);
						}
						if(iNewMValue > 12)
						{
							iNewMValue = iAdditionValue;
							strNewMValue = String.valueOf(iAdditionValue);
						}
					}
					else
						bEOF = true;

					if(iNewMValue <= 12)
					{
						if(strNewMValue.length() > 2)
						{
							strNewMValue = strNewMValue.substring(0,2);
							iNewMValue = Integer.parseInt(strNewMValue);
						}
						if(!strNewMValue.equals("00"))
						{
							iMValue = iNewMValue;
							vtFieldList.setElementAt(strNewMValue,iPosIndex);
						}
					}
					else if(bEOF && iPosIndex < mstrFormat.length() - 1)
					{
						if(iOffset == getLength())
							super.insertString(iOffset,"" + mchrSeperator,null);
						insertString(iOffset + 1,strInsert,null);
						return;
					}
				}
				else
				{
					if(bSeperatorPos && iOffset < getLength())
						txtParent.setCaretPosition(txtParent.getCaretPosition() + 1);
					else if(iOffset >= getLength() && iPosIndex < mstrFormat.length() - 1)
						super.insertString(getLength(),"" + mchrSeperator,null);
					else if(iOffset > 0)
					{
						// Let try one case
						String str = strContent.substring(0,iOffset) + strInsert + strContent.substring(iOffset,strContent.length());
						vtFieldList = StringUtil.toStringVector(str,String.valueOf(mchrSeperator));

						// Test case
						iDValue = getFieldByType('D',vtFieldList);
						iMValue = getFieldByType('M',vtFieldList);
						iYValue = getFieldByType('Y',vtFieldList);
						if(vtFieldList.size() <= 3 && iDValue < 31 && iMValue < 12 && iYValue < 10000)
							super.insertString(iOffset,strInsert,null);
					}
					return;
				}
			}
			else if(chrPosType == 'Y')
			{
				if(iYValue < 0)
				{
					if(ch == mchrSeperator)
					{
						if(iOffset > getLength())
							super.insertString(getLength(),strInsert,attrib);
						return;
					}
					else
						iYValue = iAdditionValue;
				}
				else if(ch != mchrSeperator)
				{
					String strNewYValue = strValue.substring(0,iPosOffset) + iAdditionValue + strValue.substring(iPosOffset,strValue.length());
					int iNewYValue = Integer.parseInt(strNewYValue);
					boolean bEOF = false;
					if(iPosOffset < strValue.length())
					{
						if(iNewYValue > 10000)
						{
							strNewYValue = strValue.substring(0,iPosOffset) + iAdditionValue;
							if(iPosOffset < strValue.length() - 1)
								strNewYValue += strValue.substring(iPosOffset + 1,strValue.length());
							else
								bEOF = true;
							iNewYValue = Integer.parseInt(strNewYValue);
						}
						if(iNewYValue > 10000)
						{
							iNewYValue = iAdditionValue;
							strNewYValue = String.valueOf(iAdditionValue);
						}
					}
					else
						bEOF = true;

					if(iNewYValue <= 10000)
					{
						if(strNewYValue.length() > 4)
						{
							strNewYValue = strNewYValue.substring(0,4);
							iNewYValue = Integer.parseInt(strNewYValue);
						}
						if(!strNewYValue.equals("0000"))
						{
							iYValue = iNewYValue;
							vtFieldList.setElementAt(strNewYValue,iPosIndex);
						}
					}
					else if(bEOF && iPosIndex < mstrFormat.length() - 1)
					{
						if(iOffset == getLength())
							super.insertString(iOffset,"" + mchrSeperator,null);
						insertString(iOffset + 1,strInsert,null);
						return;
					}
				}
				else
				{
					if(bSeperatorPos && iOffset < getLength())
						txtParent.setCaretPosition(txtParent.getCaretPosition() + 1);
					else if(iOffset >= getLength() && iPosIndex < mstrFormat.length() - 1)
						super.insertString(getLength(),"" + mchrSeperator,null);
					else if(iOffset > 0)
					{
						// Let try one case
						String str = strContent.substring(0,iOffset) + strInsert + strContent.substring(iOffset,strContent.length());
						vtFieldList = StringUtil.toStringVector(str,String.valueOf(mchrSeperator));

						// Test case
						iDValue = getFieldByType('D',vtFieldList);
						iMValue = getFieldByType('M',vtFieldList);
						iYValue = getFieldByType('Y',vtFieldList);
						if(vtFieldList.size() <= 3 && iDValue < 31 && iMValue < 12 && iYValue < 10000)
							super.insertString(iOffset,strInsert,null);
					}
					return;
				}
			}

			strInsert = fillText(iDValue,iMValue,iYValue,vtFieldList);
			if(ch == mchrSeperator && iOffset >= getLength())
			{
				strInsert += ch;
				iOffset++;
			}
			super.remove(0,getLength());
			super.insertString(0,strInsert,attrib);
			if(iOffset < getLength())
				txtParent.setCaretPosition(iOffset + 1);
		}
		else
		{
			// Get field value
			String strContent = getText(0,iOffset) + strInsert + getText(iOffset,getLength() - iOffset);
			Vector vtFieldList = StringUtil.toStringVector(strContent,String.valueOf(mchrSeperator));

			// Test case
			iDValue = getFieldByType('D',vtFieldList);
			iMValue = getFieldByType('M',vtFieldList);
			iYValue = getFieldByType('Y',vtFieldList);

			strInsert = fillText(iDValue,iMValue,iYValue,vtFieldList);
			super.remove(0,getLength());
			super.insertString(0,strInsert,attrib);
		}
	}
	////////////////////////////////////////////////////////
	// Method
	////////////////////////////////////////////////////////
	private String fillText(int iDValue,int iMValue,int iYValue,Vector vtFieldList)
	{
		if(iDValue > 31)
			iDValue = 31;
		if(iMValue > 12)
			iMValue = 12;
		if(iYValue > 9999)
			iYValue = 9999;
		String strReturn = "";
		String strValue;
		int iOffset = 0;
		for(int iIndex = 0;iIndex < mstrFormat.length();iIndex++)
		{
			if(iIndex < vtFieldList.size())
				strValue = (String)vtFieldList.elementAt(iIndex);
			else
				strValue = "";
			int iLength = strValue.length();
			char chr = mstrFormat.charAt(iIndex);
			if(chr == 'D')
			{
				if(iDValue < 0)
					return strReturn;
				if(iIndex > 0)
				{
					strReturn += mchrSeperator;
					iOffset++;
				}
				if(strValue.length() > 0)
					strValue = FORMAT_00.format(iDValue);
				else
					strValue = String.valueOf(iDValue);
				while(strValue.length() < iLength && strValue.length() < 2)
					strValue = "0" + strValue;
				strReturn += strValue;
				iOffset += strValue.length();
			}
			else if(chr == 'M')
			{
				if(iMValue < 0)
					return strReturn;
				if(iIndex > 0)
				{
					strReturn += mchrSeperator;
					iOffset++;
				}
				if(strValue.length() > 0)
					strValue = FORMAT_00.format(iMValue);
				else
					strValue = String.valueOf(iMValue);
				while(strValue.length() < iLength && strValue.length() < 2)
					strValue = "0" + strValue;
				strReturn += strValue;
				iOffset += strValue.length();
			}
			if(chr == 'Y')
			{
				if(iYValue < 0)
					return strReturn;
				if(iIndex > 0)
				{
					strReturn += mchrSeperator;
					iOffset++;
				}
				strValue = String.valueOf(iYValue);
				while(strValue.length() < iLength && strValue.length() < 4)
					strValue = "0" + strValue;
				strReturn += strValue;
				iOffset += strValue.length();
			}
		}
		return strReturn;
	}
	////////////////////////////////////////////////////////
	private int getFieldByType(char chrFieldType,Vector vtFieldList)
	{
		int iFieldIndex = mstrFormat.indexOf(chrFieldType);
		if(iFieldIndex < 0 || iFieldIndex >= vtFieldList.size())
			return -1;
		String strFieldValue = (String)vtFieldList.elementAt(iFieldIndex);
		if(strFieldValue.length() > 0 && strFieldValue.charAt(0) == mchrSeperator)
			strFieldValue = strFieldValue.substring(1,strFieldValue.length());
		if(strFieldValue.length() > 0 && strFieldValue.charAt(strFieldValue.length() - 1) == mchrSeperator)
			strFieldValue = strFieldValue.substring(0,strFieldValue.length() - 1);
		if(strFieldValue.length() == 0)
			return -1;
		try
		{
			return Integer.parseInt(strFieldValue);
		}
		catch(Exception e)
		{
		}
		return -1;
	}
}
