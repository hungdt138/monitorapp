package com.fss.asn1;

import java.util.*;
import java.text.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class ASNUtil
{
	////////////////////////////////////////////////////////
	// Constant
	////////////////////////////////////////////////////////
	public static DecimalFormat FORMAT_00 = new DecimalFormat("00");
	public static DecimalFormat FORMAT_SHARP = new DecimalFormat("#");
	public static SimpleDateFormat fmtDateTime = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
	////////////////////////////////////////////////////////
	// Format integer
	////////////////////////////////////////////////////////
	public static String formatInteger(byte[] btValue,int iOffset,int iLength)
	{
		int iLastOffset = iOffset + iLength;
		if(btValue.length < iLastOffset || iLength < 1)
			return "";
		int iValue = 0;
		for(int iIndex = iOffset;iIndex < iLastOffset;iIndex++)
		{
			iValue <<= 8;
			iValue |= (btValue[iIndex] & 0xFF);
		}
		return String.valueOf(iValue);
	}
	////////////////////////////////////////////////////////
	// Format boolean
	////////////////////////////////////////////////////////
	public static String formatBoolean(byte btValue)
	{
		if(btValue == -1)
			return "TRUE";
		return "FALSE";
	}
	////////////////////////////////////////////////////////
	// Format Hex
	////////////////////////////////////////////////////////
	public static String formatHEX(byte[] btValue,int iOffset,int iLength)
	{
		int iLastOffset = iOffset + iLength;
		if(btValue.length < iLastOffset || iLength < 1)
			return "";

		StringBuffer value = new StringBuffer();
		for(int i = iOffset;i < iLastOffset;i++)
		{
			byte l,h;
			h = (byte)((btValue[i] & 0xF0) >>> 4);
			if(h < 10) h = (byte)('0' + h);
			else h = (byte)('A' + h - 10);

			l = (byte)((btValue[i] & 0x0F));
			if(l < 10) l = (byte)('0' + l);
			else l = (byte)('A' + l - 10);

			value.append((char)h);
			value.append((char)l);
		}
		return value.toString();
	}
	////////////////////////////////////////////////////////
	// Format BCD
	////////////////////////////////////////////////////////
	public static String formatBCD(byte[] btValue,int iOffset,int iLength)
	{
		int iLastOffset = iOffset + iLength;
		if(btValue.length < iLastOffset || iLength < 1)
			return "";

		StringBuffer value = new StringBuffer();
		for(int i = iOffset;i < iLastOffset;i++)
		{
			byte l,h;
			h = (byte)((btValue[i] & 0xF0) >>> 4);
			if(h < 10) h = (byte)('0' + h);
			else h = (byte)('A' + h - 10);

			l = (byte)((btValue[i] & 0x0F));
			if(l < 10) l = (byte)('0' + l);
			else l = (byte)('A' + l - 10);

			value.append((char)h);
			value.append((char)l);
		}
		return value.toString();
	}
	////////////////////////////////////////////////////////
	// Format TBCD
	////////////////////////////////////////////////////////
	public static String formatTBCD(byte[] btValue,int iOffset,int iLength)
	{
		int iLastOffset = iOffset + iLength;
		if(btValue.length < iLastOffset || iLength < 1)
			return "";

		byte l,h;
		StringBuffer value = new StringBuffer();
		for(int i = iOffset;i < iLastOffset;i++)
		{
			h = (byte)((btValue[i] & 0xF0) >>> 4);
			if(h < 10) h = (byte)('0' + h);
			else h = (byte)('A' + h - 10);

			l = (byte)((btValue[i] & 0x0F));
			if(l < 10) l = (byte)('0' + l);
			else l = (byte)('A' + l - 10);

			if(l != 'F') value.append((char)l);
			if(h != 'F') value.append((char)h);
		}
		return value.toString();
	}
	////////////////////////////////////////////////////////
	// Format address string
	////////////////////////////////////////////////////////
	public static String formatAddressString(byte[] btValue,int iOffset,int iLength)
	{
		return formatTBCD(btValue,iOffset + 1,iLength - 1);
	}
	////////////////////////////////////////////////////////
	// Format Date
	////////////////////////////////////////////////////////
	public static String formatDate(byte[] btValue,int iOffset,int iLength)
	{
		if((btValue.length < iOffset + iLength) || (iLength < 4))
			return "";
		return getBCDString(btValue[iOffset + 3]) + "/" + getBCDString(btValue[iOffset + 2]) + "/" + getBCDString(btValue[iOffset + 0]) + getBCDString(btValue[iOffset + 1]);
	}
	////////////////////////////////////////////////////////
	// Format time
	////////////////////////////////////////////////////////
	public static String formatTime(byte[] btValue,int iOffset,int iLength)
	{
		if((btValue.length < iOffset + iLength) || (iLength < 3))
			return "";
		return getBCDString(btValue[iOffset + 0]) + ":" + getBCDString(btValue[iOffset + 1]) + ":" + getBCDString(btValue[iOffset + 2]);
	}
	////////////////////////////////////////////////////////
	// Format time stamp
	////////////////////////////////////////////////////////
	public static String formatTimeStamp(byte[] btValue,int iOffset,int iLength,int iUtcOffset)
	{
		if((btValue.length < iOffset + iLength) || (iLength < 9))
			return "";
		Date dtReturn = null;
		long lOffset = 0;
		if(btValue[iOffset + 2] == '+' || btValue[iOffset + 2] == '-')
		{
			dtReturn = new Date(getBCDValue(btValue[iOffset + 8]),
								getBCDValue(btValue[iOffset + 7]) - 1,
								getBCDValue(btValue[iOffset + 6]),
								getBCDValue(btValue[iOffset + 5]),
								getBCDValue(btValue[iOffset + 4]),
								getBCDValue(btValue[iOffset + 3]));
			lOffset = getBCDValue(btValue[iOffset + 1]) * 3600;
			lOffset += getBCDValue(btValue[iOffset]) * 60;
			lOffset *= 1000;
			if(btValue[iOffset + 2] == '-')
				lOffset = -lOffset;
			dtReturn.setTime(dtReturn.getTime() - lOffset + iUtcOffset);
			return fmtDateTime.format(dtReturn);
		}

		dtReturn = new Date(getBCDValue(btValue[iOffset]),
							getBCDValue(btValue[iOffset + 1]) - 1,
							getBCDValue(btValue[iOffset + 2]),
							getBCDValue(btValue[iOffset + 3]),
							getBCDValue(btValue[iOffset + 4]),
							getBCDValue(btValue[iOffset + 5]));
		lOffset = getBCDValue(btValue[iOffset + 7]) * 3600;
		lOffset += getBCDValue(btValue[iOffset + 8]) * 60;
		lOffset *= 1000;
		if(btValue[iOffset + 6] == '-')
			lOffset = -lOffset;
		dtReturn.setTime(dtReturn.getTime() - lOffset + iUtcOffset);
		return fmtDateTime.format(dtReturn);
	}
	////////////////////////////////////////////////////////
	// Format ip address
	////////////////////////////////////////////////////////
	public static String formatIPAddress(byte[] btValue,int iOffset,int iLength)
	{
		int iLastOffset = iOffset + iLength;
		if(btValue.length < iLastOffset || iLength < 1)
			return "";
		StringBuffer strReturn = new StringBuffer();
		for(int i = iOffset;i < iLastOffset;i++)
		{
			strReturn.append('.');
			strReturn.append(FORMAT_SHARP.format(btValue[i] & 0xFF));
		}
		return strReturn.substring(1,strReturn.length());
	}
	////////////////////////////////////////////////////////
	public static String formatIPAddress(ASNData dat)
	{
		if(dat.mpFirstChild != null)
		{
			dat = dat.mpFirstChild;
			if(dat.mpFirstChild.miTagID == 0 || dat.mpFirstChild.miTagID == 1)
				return formatIPAddress(dat.mbtData,0,dat.mbtData.length);
			else if(dat.mpFirstChild.miTagID == 2 || dat.mpFirstChild.miTagID == 3)
				return new String(dat.mbtData);
		}
		return "";
	}
	////////////////////////////////////////////////////////
	// Parse integer
	////////////////////////////////////////////////////////
	public static byte[] parseInteger(String strData)
	{
		int iValue = Integer.parseInt(strData);
		byte[] btValue = new byte[4];
		int iIndex = 0;
		while(iValue > 0)
		{
			btValue[iIndex++] = (byte)(iValue & 0xFF);
			iValue >>= 8;
		}
		if(iIndex == 0)
			return new byte[1];

		int iCount = iIndex;
		byte[] btReturn = new byte[iCount];
		while(iIndex > 0)
			btReturn[iCount - iIndex--] = btValue[iIndex];
		return btReturn;
	}
	////////////////////////////////////////////////////////
	// Parse BCD
	////////////////////////////////////////////////////////
	public static byte[] parseBCD(String strData)
	{
		return new byte[0];
	}
	////////////////////////////////////////////////////////
	// Parse date
	////////////////////////////////////////////////////////
	public static byte[] parseDate(String strData)
	{
		return new byte[0];
	}
	////////////////////////////////////////////////////////
	// Parse Time
	////////////////////////////////////////////////////////
	public static byte[] parseTime(String strData)
	{
		return new byte[0];
	}
	////////////////////////////////////////////////////////
	// Get BCD string
	////////////////////////////////////////////////////////
	public static String getBCDString(byte btValue)
	{
		byte l,h;
		h = (byte)((btValue & 0xF0) >>> 4);
		if(h < 10) h = (byte)('0' + h);
		else h = (byte)('A' + h - 10);

		l = (byte)((btValue & 0x0F));
		if(l < 10) l = (byte)('0' + l);
		else l = (byte)('A' + l - 10);

		return String.valueOf((char)h) + String.valueOf((char)l);
	}
	////////////////////////////////////////////////////////
	// Get BCD Value
	////////////////////////////////////////////////////////
	public static int getBCDValue(byte btValue)
	{
		int iReturn;
		iReturn = ((btValue & 0xF0) >>> 4) * 10;
		iReturn += ((btValue & 0x0F));
		return iReturn;
	}
}
