package com.fss.util;

import java.io.*;

/**
 * <p>Title: BufferUtil</p>
 * <p>Description: Utility for buffer processing</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class BufferUtil
{
	////////////////////////////////////////////////////////
	/**
	 * Replace all occurred of string with another
	 * @param bufSrc byte[]
	 * @param bufFind byte[]
	 * @param bufReplace byte[]
	 * @return byte[]
	 * @throws IOException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static byte[] replaceAll(byte[] bufSrc,byte[] bufFind,byte[] bufReplace) throws IOException
	{
		int iLocation = 0;
		int iPrevLocation = 0;
		ByteArrayOutputStream buf = new ByteArrayOutputStream(bufSrc.length);
		while((iLocation = indexOf(bufSrc,iLocation,bufFind)) >= 0)
		{
			buf.write(bufSrc,iPrevLocation,iLocation - iPrevLocation);
			buf.write(bufReplace);
			iLocation += bufFind.length;
			iPrevLocation = iLocation;
		}
		buf.write(bufSrc,iPrevLocation,bufSrc.length - iPrevLocation);
		byte[] bufReturn = new byte[buf.size()];
		System.arraycopy(buf.toByteArray(),0,bufReturn,0,bufReturn.length);
		return bufReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 * Return first position of symbol in byte array
	 * @param btBuff buffer to search
	 * @param iOffset start offset
	 * @param btSearch symbol to search
	 * @return first position of symbol in buffer, -1 if not found
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static int indexOf(byte[] btBuff,int iOffset,byte[] btSearch)
	{
		int iLength = btBuff.length - btSearch.length + 1;
		for(int iIndex = iOffset;iIndex < iLength;iIndex++)
		{
			if(btSearch[0] == btBuff[iIndex])
			{
				int i = 1;
				while(i < btSearch.length)
				{
					if(btSearch[i] != btBuff[iIndex + i])
						break;
					i++;
				}
				if(i >= btSearch.length)
					return iIndex;
			}
		}
		return -1;
	}
	////////////////////////////////////////////////////////
	/**
	 * Return first position of symbol in byte array
	 * @param btBuff buffer to search
	 * @param iOffset start offset
	 * @param btSearch symbol to search
	 * @return first position of symbol in buffer, -1 if not found
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static int indexOf(byte[] btBuff,int iOffset,byte btSearch)
	{
		int iLength = btBuff.length;
		for(int iIndex = iOffset;iIndex < iLength;iIndex++)
		{
			if(btBuff[iIndex] == btSearch)
				return iIndex;
		}
		return -1;
	}
	////////////////////////////////////////////////////////
	/**
	 * Return first position of space (value > 32) in byte array
	 * @param btBuff buffer to search
	 * @param iOffset start offset
	 * @return first position of space in buffer, -1 if not found
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static int indexOfSpace(byte[] btBuff,int iOffset)
	{
		int iLength = btBuff.length;
		for(int iIndex = iOffset;iIndex < iLength;iIndex++)
		{
			if(btBuff[iIndex] <= 32)
				return iIndex;
		}
		return -1;
	}
	////////////////////////////////////////////////////////
	/**
	 * Return first position of letter (value > 32) in byte array
	 * @param btBuff buffer to search
	 * @param iOffset start offset
	 * @return first position of letter in buffer, -1 if not found
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static int indexOfLetter(byte[] btBuff,int iOffset)
	{
		int iLength = btBuff.length;
		for(int iIndex = iOffset;iIndex < iLength;iIndex++)
		{
			if(btBuff[iIndex] > 32)
				return iIndex;
		}
		return -1;
	}
	////////////////////////////////////////////////////////
	/**
	 * Return last position of sequence of symbol in byte array
	 * @param btBuff buffer to search
	 * @param iOffset start offset
	 * @param btSearch symbol to search
	 * @return first position of symbol in buffer, -1 if not found
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static int lastIndexOfSequence(byte[] btBuff,int iOffset,byte btSearch)
	{
		while(iOffset < btBuff.length && btBuff[iOffset] == btSearch)
			iOffset++;
		if(iOffset >= btBuff.length)
			return -1;
		return iOffset;
	}
	////////////////////////////////////////////////////////
	/**
	 * Return last position of space in byte array
	 * @param btBuff buffer to search
	 * @param iOffset start offset
	 * @return first position of symbol in buffer, -1 if not found
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static int lastIndexOfSequenceSpace(byte[] btBuff,int iOffset)
	{
		while(iOffset < btBuff.length && btBuff[iOffset] <= 32)
			iOffset++;
		if(iOffset >= btBuff.length)
			return -1;
		return iOffset;
	}
	////////////////////////////////////////////////////////
	/**
	 * Return last position of symbol in byte array
	 * @param btBuff buffer to search
	 * @param iOffset start offset
	 * @return first position of symbol in buffer, -1 if not found
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static int lastIndexOfSequenceLetter(byte[] btBuff,int iOffset)
	{
		while(iOffset < btBuff.length && btBuff[iOffset] > 32)
			iOffset++;
		if(iOffset >= btBuff.length)
			return -1;
		return iOffset;
	}
	////////////////////////////////////////////////////////
	/**
	 * Get data form existing buffer
	 * @param bt byte[]
	 * @param iFirstIndex int
	 * @param iLastIndex int
	 * @throws IllegalArgumentException
	 * @return byte[]
	 */
	////////////////////////////////////////////////////////
	public static byte[] getData(byte[] bt,int iFirstIndex,int iLastIndex) throws IllegalArgumentException
	{
		if(iLastIndex > bt.length || iFirstIndex < 0 || iFirstIndex > iLastIndex)
			throw new IllegalArgumentException();
		int iLength = iLastIndex - iFirstIndex;
		byte[] btReturn = new byte[iLength];
		System.arraycopy(bt,iFirstIndex,btReturn,0,iLength);
		return btReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 * Return first position of symbol in byte array
	 * @param btBuff buffer to search
	 * @param iOffset start offset
	 * @param btSearch symbol to search
	 * @param btEscape symbol to escape
	 * @return first position of symbol in buffer, -1 if not found
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static int indexOf(byte[] btBuff,int iOffset,byte btSearch,byte btEscape)
	{
		int iLength = btBuff.length;
		int iEscapeIndex = -1;
		for(int iIndex = iOffset;iIndex < iLength;iIndex++)
		{
			if(btBuff[iIndex] == btEscape)
			{
				if(iEscapeIndex < 0)
					iEscapeIndex = iIndex;
			}
			else if(btBuff[iIndex] == btSearch)
			{
				if(iEscapeIndex < 0 ||
				   (iIndex - iEscapeIndex) % 2 == 0)
					return iIndex;
			}
			else if(iEscapeIndex >= 0)
				iEscapeIndex = -1;
		}
		return -1;
	}
	////////////////////////////////////////////////////////
	/**
	 * Return first position of space (value > 32) in byte array
	 * @param btBuff buffer to search
	 * @param iOffset start offset
	 * @param btEscape symbol to escape
	 * @return first position of space in buffer, -1 if not found
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static int indexOfSpace(byte[] btBuff,int iOffset,byte btEscape)
	{
		int iLength = btBuff.length;
		int iEscapeIndex = -1;
		for(int iIndex = iOffset;iIndex < iLength;iIndex++)
		{
			if(btBuff[iIndex] == btEscape)
			{
				if(iEscapeIndex < 0)
					iEscapeIndex = iIndex;
			}
			else if(btBuff[iIndex] <= 32)
			{
				if(iEscapeIndex < 0 ||
				   (iIndex - iEscapeIndex) % 2 == 0)
					return iIndex;
			}
			else if(iEscapeIndex >= 0)
				iEscapeIndex = -1;
		}
		return -1;
	}
	////////////////////////////////////////////////////////
	/**
	 * Return first position of letter (value > 32) in byte array
	 * @param btBuff buffer to search
	 * @param iOffset start offset
	 * @param btEscape symbol to escape
	 * @return first position of letter in buffer, -1 if not found
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static int indexOfLetter(byte[] btBuff,int iOffset,byte btEscape)
	{
		int iLength = btBuff.length;
		int iEscapeIndex = -1;
		for(int iIndex = iOffset;iIndex < iLength;iIndex++)
		{
			if(btBuff[iIndex] == btEscape)
			{
				if(iEscapeIndex < 0)
					iEscapeIndex = iIndex;
			}
			else if(btBuff[iIndex] > 32)
			{
				if(iEscapeIndex < 0 ||
				   (iIndex - iEscapeIndex) % 2 == 0)
					return iIndex;
			}
			else if(iEscapeIndex >= 0)
				iEscapeIndex = -1;
		}
		return -1;
	}
}
