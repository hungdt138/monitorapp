package com.fss.asn1;

import java.io.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class ASNDefinition
{
	////////////////////////////////////////////////////////
	// Constant
	////////////////////////////////////////////////////////
	public static final int ASN_CONST = -1024;
	public static final int ASN_NULL = 0;
	public static final int ASN_CHOICE = 1;
	public static final int ASN_SET = 2;
	public static final int ASN_SET_OF = 3;
	public static final int ASN_SEQUENCE = 4;
	public static final int ASN_SEQUENCE_OF = 5;
	public static final int ASN_ENUMERATED = 6;
	public static final int ASN_TYPE_COUNT = 7;
	public static final int ASN_REFERENCE = -1;
	public static final int TAG_UNDEFINED = -1;
	public static final String ASNDataType[]=
	{
		"NULL",
		"CHOICE",
		"SET",
		"SET_OF",
		"SEQUENCE",
		"SEQUENCE_OF",
		"ENUMERATED",
	};
	////////////////////////////////////////////////////////
	// Member variable
	////////////////////////////////////////////////////////
	public String mstrName;
	public String mstrAlias;
	public String mstrValue;
	public String mstrType;
	public String mstrReference;
	public boolean mbIgnore;
	public int miLevel;
	public int miType;
	public int miTagID;
	public int miFormatID;
	public int miMaxLength;
	public ASNDefinition mpRootType;
	public ASNDefinition mpReference;
	public ASNDefinition mpFirstChild;
	public ASNDefinition mpLastChild;
	public ASNDefinition mpNext;
	////////////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////////////
	public ASNDefinition()
	{
	}
	////////////////////////////////////////////////////////
	public ASNDefinition(InputStream in) throws IOException
	{
		load(in);
	}
	////////////////////////////////////////////////////////
	// Purpose: Load asn definition from input stream (for root object)
	// Inputs: input stream store asn definition
	// Outputs: Exception throw if error occured
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public void load(InputStream in) throws IOException
	{
		// Load data from stream to buffer
		Buffer buf = new Buffer(in);

		boolean bFound = false;
		buf.miBufferIndex = 0;
		while(!bFound)
		{
			ASNDefinition pChild = new ASNDefinition();
			if(!pChild.load(buf))
				bFound = true;
			else
			{
				addChild(pChild);
				if(buf.miBufferIndex >= buf.miLength)
					bFound = true;
				else if(buf.mbtBuffer[buf.miBufferIndex] == ',')
					throw new IOException(", not allowed here (" + buf.miBufferIndex + ")");
			}
		}

		// Organize data
		fillReference(this);
		fillStruct();
		fillTagID();
		fillLevel(0);
	}
	////////////////////////////////////////////////////////
	// Purpose: Load asn definition from byte buffer
	// Inputs: byte buffer store asn definition
	// Outputs: Exception throw if error occured
	// Author: HiepTH
	////////////////////////////////////////////////////////
	private boolean load(Buffer buf) throws IOException
	{
		// Variables
		byte[] btResult;

		// Get name
		buf.miLastBufferIndex = buf.miBufferIndex;
		buf.miBufferIndex = buf.lastIndexOfSequenceSpace(buf.miLastBufferIndex);
		if(buf.miBufferIndex >= buf.miLength || !Buffer.isLetterOrDigit(buf.mbtBuffer[buf.miBufferIndex]))
			return false;
		buf.miLastBufferIndex = buf.miBufferIndex;
		buf.miBufferIndex = buf.lastIndexOfSequenceLetterOrDigit(buf.miLastBufferIndex);
		btResult = buf.getData(buf.miLastBufferIndex,buf.miBufferIndex);
		mstrName = new String(btResult);

		// Get TagID
		buf.miLastBufferIndex = buf.miBufferIndex;
		buf.miBufferIndex = buf.lastIndexOfSequenceSpace(buf.miLastBufferIndex);
		if(buf.mbtBuffer[buf.miBufferIndex] == '(')
		{
			buf.miLastBufferIndex = buf.miBufferIndex + 1;
			buf.miBufferIndex = buf.lastIndexOfSequenceLetterOrDigit(buf.miLastBufferIndex);
			btResult = buf.getData(buf.miLastBufferIndex,buf.miBufferIndex);
			if(btResult.length == 0 || !Buffer.isDigit(btResult))
				throw new IOException("TagID invalid (" + buf.miBufferIndex + ")");
			miTagID = Integer.parseInt(new String(btResult));

			buf.miLastBufferIndex = buf.miBufferIndex;
			buf.miBufferIndex = buf.lastIndexOfSequenceSpace(buf.miLastBufferIndex);
			if(buf.miBufferIndex >= buf.miLength || buf.mbtBuffer[buf.miBufferIndex] != ')')
				throw new IOException(") expected (" + buf.miBufferIndex + ")");
			buf.miLastBufferIndex = buf.miBufferIndex + 1;
			buf.miBufferIndex = buf.lastIndexOfSequenceSpace(buf.miLastBufferIndex);
		}

		// Get type
		if(buf.miBufferIndex >= buf.miLength || !Buffer.isLetterOrDigit(buf.mbtBuffer[buf.miBufferIndex]))
			throw new IOException("Field type expected (" + buf.miBufferIndex + ")");
		buf.miLastBufferIndex = buf.miBufferIndex;
		buf.miBufferIndex = buf.lastIndexOfSequenceLetterOrDigit(buf.miLastBufferIndex);
		btResult = buf.getData(buf.miLastBufferIndex,buf.miBufferIndex);
		mstrType = new String(btResult);

		// Parse type
		int iIndex = 0;
		while(iIndex < ASN_TYPE_COUNT && !ASNDataType[iIndex].equals(mstrType))
			iIndex++;
		if(iIndex < ASN_TYPE_COUNT)
			miType = iIndex;
		else miType = ASN_REFERENCE;

		buf.miLastBufferIndex = buf.miBufferIndex;
		buf.miBufferIndex = buf.lastIndexOfSequenceSpace(buf.miLastBufferIndex);
		if(miType == ASN_REFERENCE)
			mstrReference = mstrType;
		else if(miType == ASN_SEQUENCE_OF || miType == ASN_SET_OF)
		{
			if(buf.miBufferIndex >= buf.miLength || !Buffer.isLetterOrDigit(buf.mbtBuffer[buf.miBufferIndex]))
				throw new IOException("Field reference type expected (" + buf.miBufferIndex + ")");
			buf.miLastBufferIndex = buf.miBufferIndex;
			buf.miBufferIndex = buf.lastIndexOfSequenceLetterOrDigit(buf.miLastBufferIndex);
			btResult = buf.getData(buf.miLastBufferIndex,buf.miBufferIndex);
			buf.miLastBufferIndex = buf.miBufferIndex;
			buf.miBufferIndex = buf.lastIndexOfSequenceSpace(buf.miLastBufferIndex);
			mstrReference = new String(btResult);
		}
		else if(miType >= ASN_CHOICE)
		{
			// Get field list
			if(buf.mbtBuffer[buf.miBufferIndex] != '(')
				throw new IOException("( expected (" + buf.miBufferIndex + ")");
			buf.miBufferIndex++;

			boolean bFound = false;
			while(!bFound)
			{
				ASNDefinition pChild = new ASNDefinition();
				if(!pChild.load(buf))
					throw new IOException("Field name expected (" + buf.miBufferIndex + ")");
				else if(buf.mbtBuffer[buf.miBufferIndex] == ',')
				{
					buf.miBufferIndex++;
					addChild(pChild);
				}
				else if(buf.mbtBuffer[buf.miBufferIndex] == ')')
				{
					bFound = true;
					addChild(pChild);
					if(buf.miBufferIndex < buf.miLength - 1)
					{
						buf.miLastBufferIndex = buf.miBufferIndex + 1;
						buf.miBufferIndex = buf.lastIndexOfSequenceSpace(buf.miLastBufferIndex);
					}
				}
				else
					throw new IOException(") expected (" + buf.miBufferIndex + ")");
			}
		}
		return true;
	}
	////////////////////////////////////////////////////////
	// Purpose: Fill value for reference field
	// Inputs: Root definition
	// Author: HiepTH
	////////////////////////////////////////////////////////
	private void fillReference(ASNDefinition pRoot)
	{
		ASNDefinition pChild = mpFirstChild;
		while(pChild != null)
		{
			if(pChild.miType == ASN_REFERENCE ||
				pChild.miType == ASN_SET_OF ||
				pChild.miType == ASN_SEQUENCE_OF)
				pChild.mpReference = pRoot.getChild(pChild.mstrReference);
			else if(pChild.miType == ASN_CHOICE ||
					pChild.miType == ASN_SET ||
					pChild.miType == ASN_SEQUENCE)
				pChild.fillReference(pRoot);
			pChild = pChild.mpNext;
		}
	}
	////////////////////////////////////////////////////////
	// Purpose: Fill child value for reference field
	// Author: HiepTH
	////////////////////////////////////////////////////////
	private void fillStruct()
	{
		mpRootType = this;
		if(miType == ASN_REFERENCE)
		{
			ASNDefinition pTemp = mpReference;
			while(pTemp != null &&
				  pTemp.mpReference != null &&
				  pTemp.miType == ASN_REFERENCE)
				pTemp = pTemp.mpReference;
			if(pTemp != null)
			{
				mpRootType = pTemp.cloneTree();
				if(pTemp.mpFirstChild != null)
				{
					mpFirstChild = mpRootType.mpFirstChild;
					mpLastChild = mpRootType.mpLastChild;
				}
			}
		}

		ASNDefinition pChild = mpFirstChild;
		while(pChild != null)
		{
			pChild.fillStruct();
			pChild = pChild.mpNext;
		}
	}
	////////////////////////////////////////////////////////
	// Purpose: Fill tagid for unknown tag
	// Author: HiepTH
	////////////////////////////////////////////////////////
	private void fillTagID()
	{
		ASNDefinition pChild = mpFirstChild;
		while(pChild != null)
		{
			if(pChild.miType == ASN_CHOICE || pChild.miType == ASN_SET ||
				pChild.miType == ASN_SEQUENCE)
				pChild.fillTagID();
			if(pChild.miType == ASN_REFERENCE &&
				pChild.miTagID == TAG_UNDEFINED &&
				pChild.mpReference != null)
				pChild.miTagID = pChild.mpReference.miTagID;
			pChild = pChild.mpNext;
		}
	}
	////////////////////////////////////////////////////////
	// Purpose: Fill value for level field
	// Inputs: Root definition
	// Author: HiepTH
	////////////////////////////////////////////////////////
	private void fillLevel(int iLevel)
	{
		miLevel = iLevel;
		ASNDefinition pChild = mpFirstChild;
		while(pChild != null)
		{
			pChild.fillLevel(iLevel + 1);
			pChild = pChild.mpNext;
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param cur ASNCursor[]
	 */
	////////////////////////////////////////////////////////
	public void fillIgnore(ASNCursor[] cur)
	{
		int iMinLevel = -1, iMaxLevel = -1;
		for(int iCursorIndex = 0;iCursorIndex < cur.length;iCursorIndex++)
		{
			if(iMinLevel < 0 || iMinLevel > cur[iCursorIndex].mdefChoice.miLevel)
				iMinLevel = cur[iCursorIndex].mdefChoice.miLevel;
			if(iMaxLevel < 0 || iMaxLevel < cur[iCursorIndex].mdefChoice.miLevel)
				iMaxLevel = cur[iCursorIndex].mdefChoice.miLevel;
		}
		fillIgnore(cur,iMinLevel,iMaxLevel);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param cur ASNCursor[]
	 * @param iMinLevel int
	 * @param iMaxLevel int
	 */
	////////////////////////////////////////////////////////
	public void fillIgnore(ASNCursor[] cur,int iMinLevel,int iMaxLevel)
	{
		ASNDefinition pChild = mpFirstChild;
		while(pChild != null)
		{
			if(cur != null && pChild.miLevel >= iMaxLevel && pChild.miLevel <= iMaxLevel)
			{
				pChild.mbIgnore = true;
				for(int iIndex = 0;pChild.mbIgnore && iIndex < cur.length;iIndex++)
				{
					ASNDefinition def = cur[iIndex].mdefChoice;
					if(pChild == def)
						pChild.mbIgnore = false;
				}
			}
			else
				pChild.mbIgnore = false;
			if(pChild.miLevel < iMaxLevel)
				pChild.fillIgnore(cur,iMinLevel,iMaxLevel);
			pChild = pChild.mpNext;
		}
	}
	////////////////////////////////////////////////////////
	// Purpose: Store definition to output stream (for root object)
	// Inputs: Output stream to write to
	// Outputs: Exception throw if error occured
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public void store(OutputStream out) throws IOException
	{
		store(out,"","");
	}
	////////////////////////////////////////////////////////
	// Purpose: Store definition to output stream
	// Inputs: Output stream to write to, tab indent, seperator string
	// Outputs: Exception throw if error occured
	// Author: HiepTH
	////////////////////////////////////////////////////////
	private void store(OutputStream out,String strTab,String strSeperator) throws IOException
	{
		synchronized(out)
		{
			ASNDefinition pChild = mpFirstChild;
			while(pChild != null)
			{
				if(pChild.mpNext == null)
					strSeperator = "";

				String strInfo = strTab + pChild.mstrName;
				if(pChild.miTagID != TAG_UNDEFINED)
					strInfo += "\t(" + String.valueOf(pChild.miTagID) + ")";
				strInfo += "\t" + pChild.mstrType;
				if(pChild.miType == ASN_SET_OF || pChild.miType == ASN_SEQUENCE_OF)
					strInfo += "\t" + pChild.mstrReference + strSeperator + "\r\n";
				else if(pChild.miType == ASN_NULL || pChild.miType == ASN_REFERENCE)
					strInfo += strSeperator + "\r\n";
				else
				{
					strInfo += "\r\n" + strTab + "(\r\n";
					out.write(strInfo.getBytes());
					pChild.store(out,strTab + "\t",",");
					strInfo = strTab + ")" + strSeperator + "\r\n";
				}
				out.write(strInfo.getBytes());
				pChild = pChild.mpNext;
			}
		}
	}
	////////////////////////////////////////////////////////
	// Purpose: Test type of field
	// Inputs: Type to test
	// Outputs: True if field is type of strType, otherwise False
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public boolean isTypeOf(String strType)
	{
		// ASNDataType
		if(mstrType.equals(strType))
			return true;
		if(miType == ASN_REFERENCE && mpReference != null)
			return mpReference.isTypeOf(strType);
		return false;
	}
	////////////////////////////////////////////////////////
	// Purpose: Add new definition node into last child list
	// Inputs: Child field to add
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public void addChild(ASNDefinition pChild)
	{
		if(mpFirstChild == null)
			mpFirstChild = pChild;
		else
			mpLastChild.mpNext = pChild;

		mpLastChild = pChild;
		mpLastChild.mpNext = null;
	}
	////////////////////////////////////////////////////////
	// Purpose: Get child by tag id
	// Outputs: Child field if found, null if not found
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public ASNDefinition getChild(int iTagID)
	{
		// Check sequence or sequence of
		if(mpRootType != null &&
		   (mpRootType.miType == ASN_SEQUENCE_OF ||
		   mpRootType.miType == ASN_SET_OF))
		{
			ASNDefinition pReturn = mpRootType.mpReference;
			if(pReturn.miType == ASN_CHOICE)
				return pReturn.getChild(iTagID);
			return pReturn;
		}

		ASNDefinition pChild = mpFirstChild;
		while(pChild != null)
		{
			if(pChild.miType == ASN_CHOICE)
			{
				ASNDefinition pGrandChild = pChild.getChild(iTagID);
				if(pGrandChild != null)
					return pGrandChild;
			}
			else if(pChild.miTagID == iTagID)
				return pChild;
			pChild = pChild.mpNext;
		}
		return pChild;
	}
	////////////////////////////////////////////////////////
	// Purpose: Get child by field name
	// Outputs: Child field if found, null if not found
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public ASNDefinition getChild(String strPath)
	{
		// Get path and field name
		int iResult = strPath.indexOf('.');
		String strFieldName = strPath;
		if(iResult >= 0)
		{
			strFieldName = strPath.substring(0,iResult);
			strPath = strPath.substring(iResult + 1,strPath.length());
		}
		else
			strPath = "";

		// Check sequence or sequence of
		if(mpRootType != null &&
		   (mpRootType.miType == ASN_SEQUENCE_OF ||
		   mpRootType.miType == ASN_SET_OF))
		{
			ASNDefinition pReturn = mpRootType.mpReference;
			if(pReturn != null)
			{
				if(pReturn.mstrName.equals(strFieldName))
				{
					if(strPath.length() > 0)
						pReturn = pReturn.getChild(strPath);
				}
				else if(pReturn.miType == ASN_CHOICE)
				{
					pReturn = pReturn.getChild(strFieldName);
					if(pReturn != null)
						pReturn = pReturn.getChild(strPath);
				}
				else
					pReturn = null;
			}
			return pReturn;
		}

		// Find field name
		boolean bFound = false;
		ASNDefinition pChild = mpFirstChild;
		while(!bFound && pChild != null)
		{
			if(pChild.mstrName.equals(strFieldName))
				bFound = true;
			else if(pChild.miType == ASN_CHOICE)
			{
				ASNDefinition pGrandChild = pChild.getChild(strFieldName);
				if(pGrandChild != null)
				{
					pChild = pGrandChild;
					bFound = true;
				}
				else
					pChild = pChild.mpNext;
			}
			else
				pChild = pChild.mpNext;
		}

		if(strPath.length() == 0 || pChild == null)
			return pChild;
		return pChild.getChild(strPath);
	}
	////////////////////////////////////////////////////////
	// Purpose: Fill definition for asn data
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public void compile(ASNData datRoot)
	{
		datRoot.mpDefinition = this;
		datRoot.fillDefinition();
	}
	////////////////////////////////////////////////////////
	// Purpose: Clone new definition
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public ASNDefinition cloneTree()
	{
		ASNDefinition defReturn = new ASNDefinition();
		defReturn.mstrName = mstrName;
		defReturn.mstrAlias = mstrAlias;
		defReturn.mstrValue = mstrValue;
		defReturn.mstrType = mstrType;
		defReturn.mstrReference = mstrReference;
		defReturn.miType = miType;
		defReturn.miTagID = miTagID;
		defReturn.miFormatID = miFormatID;
		defReturn.miMaxLength = miMaxLength;
		defReturn.mpRootType = defReturn;
		defReturn.mpReference = mpReference;

		ASNDefinition defTemp = mpFirstChild;
		while(defTemp != null)
		{
			defReturn.addChild(defTemp.cloneTree());
			defTemp = defTemp.mpNext;
		}
		return defReturn;
	}
}
