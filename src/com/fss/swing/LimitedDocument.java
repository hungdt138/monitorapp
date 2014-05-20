package com.fss.swing;

import javax.swing.text.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class LimitedDocument extends PlainDocument
{
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	protected int miMaxLength = -1;
	////////////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////////////
	public LimitedDocument()
	{
	}
	////////////////////////////////////////////////////////
	public LimitedDocument(int iMaxLength)
	{
		setMaxLength(iMaxLength);
	}
	////////////////////////////////////////////////////////
	// Max length property
	////////////////////////////////////////////////////////
	public void setMaxLength(int iMaxLength)
	{
		miMaxLength = iMaxLength;
	}
	////////////////////////////////////////////////////////
	public int getMaxLength()
	{
		return miMaxLength;
	}
	////////////////////////////////////////////////////////
	// Implements
	////////////////////////////////////////////////////////
	public void insertString(int iOffset,String strInsert,AttributeSet attrib) throws BadLocationException
	{
		if(miMaxLength >=0)
		{
			if(strInsert != null && getLength() + strInsert.length() <= miMaxLength)
				super.insertString(iOffset,strInsert,attrib);
		}
		else
			super.insertString(iOffset,strInsert,attrib);
	}
}