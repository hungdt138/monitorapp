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

public class Buffer
{
	////////////////////////////////////////////////////////
	// Parameter
	////////////////////////////////////////////////////////
	public static int EXTEND_SIZE = 4096;
	////////////////////////////////////////////////////////
	// Member variable
	////////////////////////////////////////////////////////
	public int miLength = 0;
	public byte[] mbtBuffer = null;
	public int miBufferIndex = 0;
	public int miLastBufferIndex = 0;
	////////////////////////////////////////////////////////
	// CBuffer constructor
	////////////////////////////////////////////////////////
	public Buffer()
	{
		mbtBuffer = new byte[miLength];
	}
	////////////////////////////////////////////////////////
	public Buffer(int iLength)
	{
		alloc(iLength);
	}
	////////////////////////////////////////////////////////
	public Buffer(InputStream in) throws IOException
	{
		load(in);
	}
	////////////////////////////////////////////////////////
	public Buffer(byte[] btBuffer)
	{
		mbtBuffer = btBuffer;
		miLength = mbtBuffer.length;
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public static boolean isSeperatedSymbol(byte c)
	{
		return(c == ';' || c == ',');
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public static boolean isSpace(byte c)
	{
		return(c <= 32);
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public static boolean isLetter(byte c)
	{
		return c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public static boolean isDigit(byte c)
	{
		return(c >= '0' && c <= '9');
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public static boolean isLetterOrDigit(byte c)
	{
		return isLetter(c) || isDigit(c);
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public static boolean isLetter(byte[] str)
	{
		for(int iIndex = 0;iIndex < str.length;iIndex++)
		{
			if(!isLetter(str[iIndex]))
				return false;
		}
		return true;
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public static boolean isDigit(byte[] str)
	{
		for(int iIndex = 0;iIndex < str.length;iIndex++)
		{
			if(!isDigit(str[iIndex]))
				return false;
		}
		return true;
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public static boolean isLetterOrDigit(byte[] str)
	{
		for(int iIndex = 0;iIndex < str.length;iIndex++)
		{
			if(!isLetterOrDigit(str[iIndex]))
				return false;
		}
		return true;
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public void alloc(int iLength)
	{
		miLength = iLength;
		mbtBuffer = new byte[miLength];
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public void extend()
	{
		byte[] btBufferNew = new byte[mbtBuffer.length + EXTEND_SIZE];
		System.arraycopy(mbtBuffer,0,btBufferNew,0,miLength);
		mbtBuffer = btBufferNew;
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public void trim()
	{
		if(mbtBuffer.length == miLength)
			return;

		byte[] btBufferNew = new byte[miLength];
		System.arraycopy(mbtBuffer,0,btBufferNew,0,miLength);
		mbtBuffer = btBufferNew;
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public void append(byte[] btBuffer)
	{
		int iNewLength = btBuffer.length + miLength;
		while(mbtBuffer.length < iNewLength)
			extend();
		System.arraycopy(btBuffer,0,mbtBuffer,miLength,btBuffer.length);
		miLength = iNewLength;
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public void append(Buffer buf)
	{
		int iNewLength = buf.miLength + miLength;
		while(mbtBuffer.length < iNewLength)
			extend();
		System.arraycopy(buf.mbtBuffer,0,mbtBuffer,miLength,buf.miLength);
		miLength = iNewLength;
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public void append(byte btValue)
	{
		if(miLength >= mbtBuffer.length)
			extend();
		mbtBuffer[miLength] = btValue;
		miLength++;
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public void load(InputStream in) throws IOException
	{
		synchronized(in)
		{
			alloc(in.available());
			int iByteRead = 0;
			while(iByteRead < miLength)
				iByteRead += in.read(mbtBuffer,iByteRead,miLength - iByteRead);
		}
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public void store(OutputStream out) throws IOException
	{
		out.write(mbtBuffer,0,miLength);
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public int indexOfSymbol(int iFirstIndex, char c)
	{
		while(iFirstIndex < miLength && mbtBuffer[iFirstIndex] != c)
			iFirstIndex++;
		if(iFirstIndex >= miLength)
			return -1;
		return iFirstIndex;
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public int indexOfLetter(int iFirstIndex)
	{
		while(iFirstIndex < miLength && !isLetter(mbtBuffer[iFirstIndex]))
			iFirstIndex++;
		if(iFirstIndex >= miLength)
			return -1;
		return iFirstIndex;
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public int indexOfDigit(int iFirstIndex)
	{
		while(iFirstIndex < miLength && !isDigit(mbtBuffer[iFirstIndex]))
			iFirstIndex++;
		if(iFirstIndex >= miLength)
			return -1;
		return iFirstIndex;
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public int indexOfLetterOrDigit(int iFirstIndex)
	{
		while(iFirstIndex < miLength && !isLetterOrDigit(mbtBuffer[iFirstIndex]))
			iFirstIndex++;
		if(iFirstIndex >= miLength)
			return -1;
		return iFirstIndex;
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public int indexOfSpace(int iFirstIndex)
	{
		while(iFirstIndex < miLength && !isSpace(mbtBuffer[iFirstIndex]))
			iFirstIndex++;
		if(iFirstIndex >= miLength)
			return -1;
		return iFirstIndex;
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public int lastIndexOfSequenceLetter(int iFirstIndex)
	{
		if(!isLetter(mbtBuffer[iFirstIndex]))
			return iFirstIndex;
		while(iFirstIndex < miLength && isLetter(mbtBuffer[iFirstIndex]))
			iFirstIndex++;
		return iFirstIndex;
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public int lastIndexOfSequenceDigit(int iFirstIndex)
	{
		if(!isDigit(mbtBuffer[iFirstIndex]))
			return iFirstIndex;
		while(iFirstIndex < miLength && isDigit(mbtBuffer[iFirstIndex]))
			iFirstIndex++;
		return iFirstIndex;
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public int lastIndexOfSequenceLetterOrDigit(int iFirstIndex)
	{
		if(!isLetterOrDigit(mbtBuffer[iFirstIndex]))
			return iFirstIndex;
		while(iFirstIndex < miLength && isLetterOrDigit(mbtBuffer[iFirstIndex]))
			iFirstIndex++;
		return iFirstIndex;
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public int lastIndexOfSequenceSpace(int iFirstIndex)
	{
		if(!isSpace(mbtBuffer[iFirstIndex]))
			return iFirstIndex;
		while(iFirstIndex < miLength && isSpace(mbtBuffer[iFirstIndex]))
			iFirstIndex++;
		return iFirstIndex;
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public int lastIndexOfSymbol(int iFirstIndex, char c)
	{
		while(iFirstIndex < miLength && mbtBuffer[iFirstIndex] == c)
			iFirstIndex++;
		if(iFirstIndex >= miLength)
			return -1;
		return iFirstIndex;
	}
	////////////////////////////////////////////////////////
	//
	////////////////////////////////////////////////////////
	public byte[] getData(int iFirstIndex,int iLastIndex) throws IllegalArgumentException
	{
		if(iLastIndex > miLength || iFirstIndex < 0 || iFirstIndex > iLastIndex)
			throw new IllegalArgumentException();
		int iLength = iLastIndex - iFirstIndex;
		byte[] btReturn = new byte[iLength];
		System.arraycopy(mbtBuffer,iFirstIndex,btReturn,0,iLength);
		return btReturn;
	}
}
