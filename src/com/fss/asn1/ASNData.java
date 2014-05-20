package com.fss.asn1;

import java.io.*;
import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class ASNData
{
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	public int miTagID;
	public int miLength;
	public byte mbtClass;
	public byte mbtConstructed;
	public byte[] mbtData;
	public ASNData mpFirstChild,mpLastChild;
	public ASNData mpNext;
	public ASNDefinition mpDefinition;
	public byte btTerminatedSymbol = 0;
	////////////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////////////
	public ASNData()
	{
	}
	////////////////////////////////////////////////////////
	public ASNData(InputStream in) throws IOException
	{
		load(in);
	}
	////////////////////////////////////////////////////////
	// Purpose: Load asn file into memory
	// Inputs: Input stream store binary data
	// Outputs: Exception throw if error occured
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public void load(InputStream in) throws IOException
	{
		Buffer buf = new Buffer(in);
		buf.miBufferIndex = 0;
		while(buf.miBufferIndex < buf.miLength)
		{
			load(buf,0);
			if(buf.miBufferIndex < buf.miLength)
			{
				// Gap ky tu key thuc
				if(btTerminatedSymbol >= 0)
				{
					buf.miBufferIndex = buf.lastIndexOfSymbol(buf.miBufferIndex,(char)btTerminatedSymbol);
					if(buf.miBufferIndex < 0)
						buf.miBufferIndex = buf.miLength;
				}
			}
		}
	}
	////////////////////////////////////////////////////////
	// Purpose: Load asn file into memory
	// Inputs: Memory buffer store binary data, end position
	// Outputs: Exception throw if error occured
	// Author: HiepTH
	////////////////////////////////////////////////////////
	private void load(Buffer buf,int iEndLocation)
	{
		// Variables
		ASNData pChild;
		byte btTemp;
		boolean bFound;

		while(true)
		{
			// Gap ky tu ket thuc
			if(btTerminatedSymbol >= 0)
			{
				if(buf.mbtBuffer[buf.miBufferIndex] == btTerminatedSymbol)
					return;
			}

			// Create new node
			pChild = new ASNData();

			/////////////////////////////////////////////////////////////////////////////
			// Phan tich identifier field
			/////////////////////////////////////////////////////////////////////////////
			// Get indentifier field
			btTemp = buf.mbtBuffer[buf.miBufferIndex++];

			// 2 bit dau.Class ID
			pChild.mbtClass = (byte)((btTemp & 0xC0) >>> 6);
			btTemp &= 0x3F; // 00111111

			// Bit thu 3.Contructed or primitive
			pChild.mbtConstructed = (byte)((btTemp & 0xE0) >>> 5);
			btTemp &= 0x1F; // 00011111

			// 5 bit cuoi.Tag ID
			pChild.miTagID = btTemp;
			if(pChild.miTagID >= 31)
			{
				// Reset tagid & flag
				pChild.miTagID = 0;
				bFound = false;
				while(!bFound) // Bit dau tien khac 0
				{
					btTemp = buf.mbtBuffer[buf.miBufferIndex++];
					bFound = ((btTemp >>> 7) == 0); // First bit = 0

					// Xoa bit dau tien
					btTemp &= 0x7F; // 01111111

					// Tang tagid
					pChild.miTagID <<= 7;
					pChild.miTagID |= btTemp;
				}
			}

			// Add new Child
			addChild(pChild);

			/////////////////////////////////////////////////////////////////////////////
			// Phan tich length field
			/////////////////////////////////////////////////////////////////////////////
			btTemp = buf.mbtBuffer[buf.miBufferIndex++];
			int iLength = 0;
			if(btTemp == -128)
				iLength = -1;
			else if(btTemp < 0) // Long definite form
			{
				// Xoa bit dau tien
				btTemp &= 0x7F; // 01111111

				// Lay gia tri cua length field
				while(btTemp > 0)
				{
					iLength <<= 8;
					iLength += buf.mbtBuffer[buf.miBufferIndex++] & 0xFF;
					btTemp--;
				}
			}
			else
				iLength = btTemp & 0xFF;

			/////////////////////////////////////////////////////////////////////////////
			// Phan tich content field
			/////////////////////////////////////////////////////////////////////////////
			if(pChild.mbtConstructed == 1) // Du lieu co cau truc
			{
				if(iLength < 0) // Content field ket thuc boi EOC
					pChild.load(buf,-1);
				else
					pChild.load(buf,buf.miBufferIndex + iLength);
			}
			else // Du lieu co ban
				pChild.mbtData = buf.getData(buf.miBufferIndex,buf.miBufferIndex += iLength);

			// Kiem tra xem da den luc ket thuc chua
			if(buf.miBufferIndex >= buf.miLength)
				return;
			else
			{
				if(iEndLocation > 0 && buf.miBufferIndex >= iEndLocation) // Ket thuc boi do dai
					return;
				else
				{
					btTemp = buf.mbtBuffer[buf.miBufferIndex];
					if(btTemp == 0) // Ket thuc boi EOC
					{
						buf.miBufferIndex++;
						if(buf.miBufferIndex < buf.miLength)
						{
							if(buf.mbtBuffer[buf.miBufferIndex] == 0)
								buf.miBufferIndex++;
						}
						return;
					}
				}
			}
		}
	}
	////////////////////////////////////////////////////////
	// Purpose: Calculate length of child fields
	// Outputs: iLength is correct
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public void computeLength()
	{
		ASNData pChild = mpFirstChild;
		miLength = 0;
		while(pChild != null)
		{
			// Compute child length
			if(pChild.mbtConstructed != 1)
			{
				if(pChild.mbtData != null)
					pChild.miLength = pChild.mbtData.length;
				else
					pChild.miLength = 0;
			}
			else
				pChild.computeLength();

			// Identifier field -> length = 1
			miLength += 1;
			if(pChild.miTagID >= 31)
			{
				if(pChild.miTagID < 128) // Additional 1 bytes for tag id
					miLength += 1;
				else // Additional 2 bytes for tag id
					miLength += 2;
			}

			// Length field
			if(pChild.miLength < 128) // Short definition form -> length = 1
				miLength += 1;
			else if(pChild.miLength < 256) // Long definition form -> length = 2
				miLength += 2;
			else // Terminated symbol form -> length = 3
				miLength += 3;

			// Content field -> length = length of content field
			miLength += pChild.miLength;

			pChild = pChild.mpNext;
		}
	}
	////////////////////////////////////////////////////////
	// Purpose: Store loaded asn struct into output stream
	// Inputs: Output stream to write to
	// Outputs: Exception throw when error occured
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public void store(OutputStream out) throws IOException
	{
		computeLength();
		Buffer buf = new Buffer(miLength);
		buf.miLength = 0;
		store(buf);
		buf.store(out);
	}
	////////////////////////////////////////////////////////
	// Purpose: Store loaded asn struct into memory buffer
	// Inputs: Memory buffer to write to
	// Author: HiepTH
	////////////////////////////////////////////////////////
	private void store(Buffer buf)
	{
		ASNData pChild = mpFirstChild;
		char cTemp;

		while(pChild != null)
		{
			/////////////////////////////////////////////////////////////////////////////
			// Tao identifier field
			/////////////////////////////////////////////////////////////////////////////
			// 2 bit dau.Class ID
			cTemp = (char)pChild.mbtClass;

			// Bit thu 3.Contructed or primitive
			if(pChild.mbtConstructed == 1)
			{
				cTemp <<= 1;
				cTemp += 1;
				cTemp <<= 5;
			}
			else
				cTemp <<= 6;

			// 5 bit cuoi.Tag ID
			if(pChild.miTagID >= 31)
			{
				cTemp += 31;
				buf.append((byte)cTemp);

				if(pChild.miTagID < 128)
					buf.append((byte)pChild.miTagID);
				else
				{
					cTemp = (char)(pChild.miTagID << 2);
					buf.append((byte)(128 + (cTemp >> 9)));

					cTemp <<= 7;
					cTemp >>= 9;
					buf.append((byte)cTemp);
				}
			}
			else
			{
				cTemp += pChild.miTagID;
				buf.append((byte)cTemp);
			}

			/////////////////////////////////////////////////////////////////////////////
			// Tao length field
			/////////////////////////////////////////////////////////////////////////////
			if(pChild.miLength < 128)
				buf.append((byte)pChild.miLength);
			else if(pChild.miLength < 256)
			{
				buf.append((byte)129);
				buf.append((byte)pChild.miLength);
			}
			else // 10 00 00 00 BOC
				buf.append((byte)128);

			if(pChild.mbtConstructed != 1) // Kieu du lieu co ban
			{
				if(pChild.mbtData != null)
					buf.append(pChild.mbtData);
			}
			else // Kieu du lieu co cau truc
			{
				pChild.store(buf);

				/////////////////////////////////////////////////////////////////////////////
				// Ghi du lieu vao bufParent
				/////////////////////////////////////////////////////////////////////////////
				if(pChild.miLength > 256)
				{
					// 00 00 00 00 00 00 00 00 EOC
					buf.append((byte)0);
					buf.append((byte)0);
				}
			}
			pChild = pChild.mpNext;
		}
	}
	////////////////////////////////////////////////////////
	// Purpose: Add new element into last of child list
	// Inputs: ASN data to add
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public void addChild(ASNData pChild)
	{
		if(mpFirstChild == null)
			mpFirstChild = pChild;
		else
			mpLastChild.mpNext = pChild;

		mpLastChild = pChild;
		mpLastChild.mpNext = null;
	}
	////////////////////////////////////////////////////////
	// Purpose: Fill definition for child list, base on
	// definition of this object
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public void fillDefinition()
	{
		ASNData pChild = mpFirstChild;
		while(pChild != null)
		{
			pChild.mpDefinition = mpDefinition.getChild(pChild.miTagID);
			if(pChild.mpDefinition != null)
				pChild.fillDefinition();
			pChild = pChild.mpNext;
		}
	}
	////////////////////////////////////////////////////////
	// Purpose: Getchild by tagid
	// Inputs: Tagid of child field
	// Outputs: ASNData child field, null if not found
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public ASNData getChild(int iTagID)
	{
		ASNData pChild = mpFirstChild;
		while(pChild != null)
		{
			if(pChild.miTagID == iTagID)
				return pChild;
			pChild = pChild.mpNext;
		}
		return pChild;
	}
	////////////////////////////////////////////////////////
	// Purpose: Getchild by definition
	// Inputs: Definition of child field
	// Outputs: ASNData child field, null if not found
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public ASNData getChild(ASNDefinition def)
	{
		ASNData pChild = mpFirstChild;
		while(pChild != null)
		{
			if(pChild.mpDefinition == def)
				return pChild;
			ASNData pGrandChild = pChild.getChild(def);
			if(pGrandChild != null)
				return pGrandChild;
			pChild = pChild.mpNext;
		}
		return pChild;
	}
	////////////////////////////////////////////////////////
	// Purpose: Getchild by definition
	// Inputs: Definition of child field
	// Outputs: ASNData child field, null if not found
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public ASNData getChildOneLevel(ASNDefinition def)
	{
		ASNData pChild = mpFirstChild;
		while(pChild != null)
		{
			if(pChild.mpDefinition == def)
				return pChild;
			pChild = pChild.mpNext;
		}
		return pChild;
	}
	////////////////////////////////////////////////////////
	// Purpose: Getchild by tagid
	// Inputs: Tagid of child field
	// Outputs: ASNData child field, null if not found
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public Vector getChildList(int iTagID)
	{
		Vector vtReturn = new Vector();
		ASNData pChild = mpFirstChild;
		while(pChild != null)
		{
			if(pChild.miTagID == iTagID)
				vtReturn.addElement(pChild);
			pChild = pChild.mpNext;
		}
		return vtReturn;
	}
	////////////////////////////////////////////////////////
	// Purpose: Getchild by definition
	// Inputs: Definition of child field
	// Outputs: ASNData child field, null if not found
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public Vector getChildList(ASNDefinition def)
	{
		Vector vtReturn = new Vector();
		ASNData pChild = mpFirstChild;
		while(pChild != null)
		{
			if(pChild.mpDefinition == def)
				vtReturn.addElement(pChild);
			vtReturn.addAll(pChild.getChildList(def));
			pChild = pChild.mpNext;
		}
		return vtReturn;
	}
	////////////////////////////////////////////////////////
	// Purpose: Getchild by definition
	// Inputs: Definition of child field
	// Outputs: ASNData child field, null if not found
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public Vector getChildListOneLevel(ASNDefinition def)
	{
		Vector vtReturn = new Vector();
		ASNData pChild = mpFirstChild;
		while(pChild != null)
		{
			if(pChild.mpDefinition == def)
				vtReturn.addElement(pChild);
			pChild = pChild.mpNext;
		}
		return vtReturn;
	}
	////////////////////////////////////////////////////////
	// Purpose: Write all data (ascii format) to outputstream
	// Inputs: Definition of child field
	// Outputs: Outputstream to write to, tab indent, format library
	// Author: HiepTH
	////////////////////////////////////////////////////////
	public void storeText(OutputStream out,String strTab,ASNFormat fmt) throws IOException
	{
		ASNData dat = this;
		while(dat != null)
		{
			if(dat.mpDefinition == null)
			{
				String strLine = "TagID = '" + miTagID + "' definition not found";
				if(dat.mbtData != null)
					strLine += ": " + new String(dat.mbtData);
				strLine = strTab + strLine + "\r\n";
				out.write(strLine.getBytes());
			}
			else
			{
				String strValue = fmt.format(dat.mpDefinition,dat);
				String strLine = dat.mpDefinition.mstrName;
				if(strValue != null)
					strLine += ": " + strValue;
				strLine = strTab + strLine + "\r\n";
				out.write(strLine.getBytes());
			}
			if(dat.mpFirstChild != null)
				dat.mpFirstChild.storeText(out,strTab + "\t",fmt);
			dat = dat.mpNext;
		}
	}
}
