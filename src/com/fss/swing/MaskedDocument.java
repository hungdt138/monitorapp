package com.fss.swing;

import java.util.*;
import javax.swing.text.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class MaskedDocument extends LimitedDocument
{
	////////////////////////////////////////////////////////
	// Inner class
	////////////////////////////////////////////////////////
	private class MatchArgument
	{
		public String mstrReserved = null;
		public int miLastPatternIndex = 0;
		public int miLastSourceIndex = 0;
		public String mstrPattern = null;
		public String mstrSource = null;
		public String mstrRemain = null;
		public MatchArgument(String strSource,String strPattern)
		{
			mstrPattern = strPattern;
			mstrSource = strSource;
		}
	}
	////////////////////////////////////////////////////////
	private static final String REQUIRED_PATTERN = "+0LA&";
	private static final String OPTIONAL_PATTERN = "-9la?";
	protected String mstrMask = null;
	private String mstrFilter = "";
	private Vector mvtLowerFilter = new Vector();
	private Vector mvtUpperFilter = new Vector();
	private String mstrAllowance = "";
	private Vector mvtLowerAllowance = new Vector();
	private Vector mvtUpperAllowance = new Vector();
	////////////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////////////
	public MaskedDocument()
	{
	}
	////////////////////////////////////////////////////////
	public MaskedDocument(String strMask)
	{
		setMask(strMask);
	}
	////////////////////////////////////////////////////////
	// Mask property
	////////////////////////////////////////////////////////
	public void setMask(String strMask)
	{
		mstrMask = strMask;
	}
	////////////////////////////////////////////////////////
	public String getMask()
	{
		return mstrMask;
	}
	////////////////////////////////////////////////////////
	/**
	 * Filter implementation
	 */
	////////////////////////////////////////////////////////
	public void setFilter(String strFilter)
	{
		mstrFilter = strFilter;
	}
	////////////////////////////////////////////////////////
	public void addFilterBoundary(char cLowerBound,char cUpperBound) throws IllegalArgumentException
	{
		if(cUpperBound < cLowerBound)
			throw new IllegalArgumentException("Upper bound ("  + cUpperBound + ") < lower bound ("  + cLowerBound + ")");
		mvtLowerFilter.addElement(new Character(cLowerBound));
		mvtUpperFilter.addElement(new Character(cUpperBound));
	}
	////////////////////////////////////////////////////////
	public void removeFilterBoundary(int iBoudaryIndex)
	{
		mvtLowerFilter.removeElementAt(iBoudaryIndex);
		mvtUpperFilter.removeElementAt(iBoudaryIndex);
	}
	////////////////////////////////////////////////////////
	public int getFilterBoundaryCount()
	{
		return mvtLowerFilter.size();
	}
	////////////////////////////////////////////////////////
	public char getLowerFilterBoundary(int iBoudaryIndex)
	{
		return ((Character)mvtLowerFilter.elementAt(iBoudaryIndex)).charValue();
	}
	////////////////////////////////////////////////////////
	public char getUpperFilterBoundary(int iBoudaryIndex)
	{
		return ((Character)mvtUpperFilter.elementAt(iBoudaryIndex)).charValue();
	}
	////////////////////////////////////////////////////////
	public String getFilter()
	{
		return mstrFilter;
	}
	////////////////////////////////////////////////////////
	/**
	 * Allowance implementation
	 */
	////////////////////////////////////////////////////////
	public void setAllowance(String strAllowance)
	{
		mstrAllowance = strAllowance;
	}
	////////////////////////////////////////////////////////
	public void addAllowanceBoundary(char cLowerBound,char cUpperBound)
	{
		if(cUpperBound < cLowerBound)
			throw new IllegalArgumentException("Upper bound ("  + cUpperBound + ") < lower bound ("  + cLowerBound + ")");
		mvtLowerAllowance.addElement(new Character(cLowerBound));
		mvtUpperAllowance.addElement(new Character(cUpperBound));
	}
	////////////////////////////////////////////////////////
	public void removeAllowanceBoundary(int iBoudaryIndex)
	{
		mvtLowerAllowance.removeElementAt(iBoudaryIndex);
		mvtUpperAllowance.removeElementAt(iBoudaryIndex);
	}
	////////////////////////////////////////////////////////
	public int getAllowanceBoundaryCount()
	{
		return mvtLowerAllowance.size();
	}
	////////////////////////////////////////////////////////
	public char getLowerAllowanceBoundary(int iBoudaryIndex)
	{
		return ((Character)mvtLowerAllowance.elementAt(iBoudaryIndex)).charValue();
	}
	////////////////////////////////////////////////////////
	public char getUpperAllowanceBoundary(int iBoudaryIndex)
	{
		return ((Character)mvtUpperAllowance.elementAt(iBoudaryIndex)).charValue();
	}
	////////////////////////////////////////////////////////
	public String getAllowance()
	{
		return mstrFilter;
	}
	////////////////////////////////////////////////////////
	// Implements
	////////////////////////////////////////////////////////
	public void insertString(int iOffset,String strInsert,AttributeSet attrib) throws BadLocationException
	{
		if(strInsert == null)
			return;
		StringBuffer str = new StringBuffer(strInsert);
		if(mstrFilter != null && mstrFilter.length() > 0)
		{
			for(int iIndex = 0;iIndex < str.length();iIndex++)
			{
				if(mstrFilter.indexOf(str.charAt(iIndex)) >= 0)
				{
					str.delete(iIndex,iIndex + 1);
					iIndex--;
				}
			}
		}
		for(int iFilterIndex = 0;iFilterIndex < getFilterBoundaryCount();iFilterIndex++)
		{
			char cLower = getLowerFilterBoundary(iFilterIndex);
			char cUpper = getUpperFilterBoundary(iFilterIndex);
			for(int iIndex = 0;iIndex < str.length();iIndex++)
			{
				if(str.charAt(iIndex) >= cLower && str.charAt(iIndex) <= cUpper &&
				   mstrAllowance.indexOf(str.charAt(iIndex)) < 0)
				{
					str.delete(iIndex,iIndex + 1);
					iIndex--;
				}
			}
		}
		for(int iAllowanceIndex = 0;iAllowanceIndex < getAllowanceBoundaryCount();iAllowanceIndex++)
		{
			char cLower = getLowerAllowanceBoundary(iAllowanceIndex);
			char cUpper = getUpperAllowanceBoundary(iAllowanceIndex);
			for(int iIndex = 0;iIndex < str.length();iIndex++)
			{
				if((str.charAt(iIndex) < cLower || str.charAt(iIndex) > cUpper) &&
				   mstrAllowance.indexOf(str.charAt(iIndex)) < 0)
				{
					str.delete(iIndex,iIndex + 1);
					iIndex--;
				}
			}
		}
		if(mstrAllowance != null && mstrAllowance.length() > 0)
		{
			for(int iIndex = 0;iIndex < str.length();iIndex++)
			{
				if(mstrAllowance.indexOf(str.charAt(iIndex)) < 0)
				{
					str.delete(iIndex,iIndex + 1);
					iIndex--;
				}
			}
		}
		strInsert = str.toString();
		if(strInsert.length() == 0)
			return;
		if(mstrMask != null)
		{
			String strContent = getText(0,iOffset) + strInsert + getText(iOffset,getLength() - iOffset);
			MatchArgument arg = new MatchArgument(strContent,mstrMask);
			if(matchPattern(arg,0,0))
				super.insertString(iOffset,strInsert,attrib);
			else
			{
				if(arg.miLastPatternIndex < arg.mstrPattern.length())
				{
					boolean bFound = false;
					int iIndex = arg.miLastPatternIndex;
					while(!bFound && iIndex < arg.mstrPattern.length())
					{
						char chrPattern = arg.mstrPattern.charAt(iIndex);
						bFound = (isRequiredPattern(chrPattern) || isOptionalPattern(chrPattern));
						if(!bFound)
							iIndex++;
					}
					iIndex -= arg.miLastPatternIndex;

					if(iOffset >= getLength())
					{
						// Simulate new content
						strInsert = arg.mstrPattern.substring(arg.miLastPatternIndex,arg.miLastPatternIndex + iIndex) + strInsert;
						strContent = getText(0,iOffset) + strInsert + getText(iOffset,getLength() - iOffset);
						arg = new MatchArgument(strContent,mstrMask);
						if(matchPattern(arg,0,0))
							super.insertString(iOffset,strInsert,attrib);
					}
					else if(iOffset < arg.miLastSourceIndex)
					{
						// Simulate new content
						strContent = strContent.substring(0,arg.miLastSourceIndex) +
									 strContent.substring(arg.miLastSourceIndex + 1,strContent.length());
						arg = new MatchArgument(strContent,mstrMask);
						if(matchPattern(arg,0,0))
						{
							super.remove(iOffset,1);
							super.insertString(iOffset,strInsert,attrib);
						}
					}
					else
					{
						// Simulate new content
						strContent = getText(0,arg.miLastSourceIndex + iIndex) + strInsert +
									 getText(arg.miLastSourceIndex + iIndex + 1,getLength() - (arg.miLastSourceIndex + iIndex + 1));
						iOffset = arg.miLastSourceIndex;
						arg = new MatchArgument(strContent,mstrMask);
						if(matchPattern(arg,0,0))
						{
							strInsert = getText(iOffset,iIndex) + strInsert;
							super.remove(iOffset,iIndex + 1);
							super.insertString(iOffset,strInsert,attrib);
						}
					}
				}
				else if(iOffset < arg.miLastSourceIndex)
				{
					// Simulate new content
					strContent = strContent.substring(0,arg.miLastSourceIndex) +
								 strContent.substring(arg.miLastSourceIndex + 1,strContent.length());
					arg = new MatchArgument(strContent,mstrMask);
					if(matchPattern(arg,0,0))
					{
						super.remove(iOffset,1);
						super.insertString(iOffset,strInsert,attrib);
					}
				}
			}
		}
		else
			super.insertString(iOffset,strInsert,attrib);
	}
	////////////////////////////////////////////////////////
	public void remove(int iOffset,int iLength) throws BadLocationException
	{
		if(mstrMask != null)
		{
			if(iLength > 0)
			{
				String strContent = getText(0,iOffset) + getText(iOffset + iLength,getLength() - iOffset - iLength);
				MatchArgument arg = new MatchArgument(strContent,mstrMask);
				if(matchPattern(arg,0,0))
					super.remove(iOffset,iLength);
			}
		}
		else
			super.remove(iOffset,iLength);
	}
	////////////////////////////////////////////////////////
	// Purpose: Compare string with pattern
	// Inputs: String and pattern to compare
	// Outputs: True if string match pattern, otherwise false
	// Author: Thai Hoang Hiep
	// Date: 18/01/2003
	////////////////////////////////////////////////////////
	private static boolean matchPattern(MatchArgument arg,int iSourceIndex,int iPatternIndex)
	{
		// Validate process
		while(iSourceIndex < arg.mstrSource.length())
		{
			// Get data
			arg.miLastPatternIndex = iPatternIndex;
			arg.miLastSourceIndex = iSourceIndex;
			if(iPatternIndex >= arg.mstrPattern.length())
				return false;
			char chrPattern = arg.mstrPattern.charAt(iPatternIndex);
			if(isOptionalPattern(chrPattern))
			{
				if(matchPattern(arg,iSourceIndex,iPatternIndex + 1))
					return true;
				if(!matchPattern(arg.mstrSource.charAt(iSourceIndex),chrPattern))
					return false;
				iSourceIndex++;
				iPatternIndex++;
			}
			else
			{
				if(!matchPattern(arg.mstrSource.charAt(iSourceIndex),chrPattern))
					return false;
				iSourceIndex++;
				iPatternIndex++;
			}
		}
		arg.miLastPatternIndex = iPatternIndex;
		arg.miLastSourceIndex = iSourceIndex;
		return true;
	}
	////////////////////////////////////////////////////////
	// Purpose: Compare char with pattern
	// Inputs: Char and pattern to compare
	// Outputs: True if char match pattern, otherwise false
	// Author: Thai Hoang Hiep
	// Date: 18/01/2003
	////////////////////////////////////////////////////////
	private static boolean matchPattern(char chrSource,char chrPattern)
	{
		if(chrPattern == '0' || chrPattern == '9')
		{
			if(chrSource < '0' || chrSource > '9') return false;
		}
		else if(chrPattern == 'L' || chrPattern == 'l')
		{
			if((chrSource < 'A' || chrSource > 'Z') &&
			   (chrSource < 'a' || chrSource > 'z')) return false;
		}
		else if(chrPattern == 'A' || chrPattern == 'a')
		{
			if((chrSource < '0' || chrSource > '9') &&
			   (chrSource < 'A' || chrSource > 'Z') &&
			   (chrSource < 'a' || chrSource > 'z')) return false;
		}
		else if(chrPattern == '&' || chrPattern == '?')
		{
			if(chrSource < ' ') return false;
		}
		else if(chrPattern == '+' || chrPattern == '-')
		{
			if(chrSource != '+' && chrSource != '-') return false;
		}
		else if(chrPattern != chrSource) return false;
		return true;
	}
	////////////////////////////////////////////////////////
	// Purpose: Return type of pattern
	// Inputs: pattern
	// Outputs: True if pattern is optional, otherwise false
	// Author: Thai Hoang Hiep
	// Date: 18/01/2003
	////////////////////////////////////////////////////////
	private static boolean isOptionalPattern(char chrPattern)
	{
		return OPTIONAL_PATTERN.indexOf(chrPattern) >= 0;
	}
	////////////////////////////////////////////////////////
	// Purpose: Return type of pattern
	// Inputs: pattern
	// Outputs: True if pattern is required, otherwise false
	// Author: Thai Hoang Hiep
	// Date: 18/01/2003
	////////////////////////////////////////////////////////
	private static boolean isRequiredPattern(char chrPattern)
	{
		return REQUIRED_PATTERN.indexOf(chrPattern) >= 0;
	}
}
