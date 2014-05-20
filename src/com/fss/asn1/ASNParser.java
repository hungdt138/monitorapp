package com.fss.asn1;

import java.io.*;
import com.fss.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class ASNParser
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	private static final int EXTEND_SIZE = 4194304;
	private static final int REMAIN_SIZE = 8192;
	private ASNDefinition mdefRoot;
	private ASNDataHandler mhandler;
	private InputStream mis;
	private byte[] mbtBuffer;
	private int miBufferIndex;
	private int miBufferLength;
	private int miPurgedLength;
	private boolean mbAvaiable = true;
	public byte btTerminatedSymbol = 0;
	////////////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////////////
	public ASNParser(ASNDefinition def,ASNDataHandler handler)
	{
		mdefRoot = def;
		mhandler = handler;
		mbtBuffer = new byte[0];
	}
	////////////////////////////////////////////////////////
	// Member function
	////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////
	/**
	 * Purge parsed data
	 * @throws IOException
	 * @return int
	 */
	////////////////////////////////////////////////////////
	private int purge() throws IOException
	{
		if(miBufferIndex > miBufferLength)
			miBufferIndex = miBufferLength;
		else if(miBufferIndex < 0)
			miBufferIndex = 0;
		byte[] bt = new byte[miBufferLength - miBufferIndex];
		System.arraycopy(mbtBuffer,miBufferIndex,bt,0,bt.length);
		int iPurged = miBufferIndex;
		miPurgedLength += iPurged;
		miBufferLength -= iPurged;
		miBufferIndex = 0;
		mbtBuffer = bt;
		return iPurged;
	}
	////////////////////////////////////////////////////////
	/**
	 * Append new data folow current data
	 * @param bt byte[]
	 * @param iOffset int
	 * @param iLength int
	 * @throws Exception
	 * @return int
	 */
	////////////////////////////////////////////////////////
	private int append(byte[] bt,int iOffset,int iLength) throws Exception
	{
		if(iOffset >= bt.length || iOffset < 0)
			throw new IllegalArgumentException();
		if(iLength + iOffset > bt.length)
			iLength = bt.length - iOffset;
		byte[] btNew = new byte[miBufferLength + iLength];
		System.arraycopy(mbtBuffer,0,btNew,0,miBufferLength);
		System.arraycopy(bt,iOffset,btNew,miBufferLength,iLength);
		miBufferLength += iLength;
		mbtBuffer = btNew;
		return iLength;
	}
	////////////////////////////////////////////////////////
	/**
	 * Read next data if available
	 * @return boolean
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	private boolean readNext() throws Exception
	{
		if(miBufferLength - miBufferIndex >= REMAIN_SIZE)
			return true;
		if(!mbAvaiable)
			return (miBufferLength > miBufferIndex);
		purge();
		int iBytesRead = 0;
		byte bt[] = new byte[EXTEND_SIZE];
		do
		{
			iBytesRead = mis.read(bt);
			if(iBytesRead > 0)
				append(bt,0,iBytesRead);
			else
				mbAvaiable = false;
		}
		while(iBytesRead >= 0 && miBufferLength - miBufferIndex < REMAIN_SIZE);
		return (miBufferLength > miBufferIndex);
	}
	////////////////////////////////////////////////////////
	/**
	 * Read next data if available
	 * @param iMinimumLength int
	 * @throws Exception
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	private boolean readNext(int iMinimumLength) throws Exception
	{
		if(!readNext())
			return false;
		if(iMinimumLength <= miBufferLength - miBufferIndex)
			return true;
		if(!mbAvaiable)
			return false;
		purge();
		int iTotalBytesRead = miBufferLength;
		int iBytesRead = 0;
		byte bt[] = new byte[EXTEND_SIZE];
		while((iBytesRead = mis.read(bt)) >= 0)
		{
			append(bt,0,iBytesRead);
			iTotalBytesRead += iBytesRead;
			if(iTotalBytesRead >= iMinimumLength)
				return true;
		}
		mbAvaiable = false;
		return false;
	}
	////////////////////////////////////////////////////////
	/**
	 * Parse one node
	 * @param iEndLocation int
	 * @param def ASNDefinition
	 * @return int
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	private int parse(int iEndLocation,ASNDefinition def) throws Exception
	{
		// Variables
		byte btTemp;
		boolean bFound;

		while(true)
		{
			// Gap ky tu ket thuc
			if(!readNext())
				return ASNDataHandler.STATUS_END;
			if(btTerminatedSymbol >= 0)
			{
				if(mbtBuffer[miBufferIndex] == btTerminatedSymbol)
					return ASNDataHandler.STATUS_CONTINUE;
			}

			// Get indentifier field
			btTemp = mbtBuffer[miBufferIndex++];

			// 2 bit dau.Class ID
			byte btClass = (byte)((btTemp & 0xC0) >>> 6); // 11|000000

			// Bit thu 3.Contructed or primitive
			byte btConstructed = (byte)((btTemp & 0x20) >>> 5); // 001|00000

			// Xoa ba bit dau
			btTemp &= 0x1F; // 00011111

			// 5 bit cuoi.Tag ID
			int iTagID = btTemp;
			if(iTagID >= 31)
			{
				// Dat lai tagid & flag
				iTagID = 0;
				bFound = false;
				while(!bFound) // Bit dau tien khac 0
				{
					btTemp = mbtBuffer[miBufferIndex++];
					bFound = ((btTemp >>> 7) == 0); // First bit = 0

					// Xoa bit dau tien
					btTemp &= 0x7F; // 01111111

					// Tang tagid
					iTagID <<= 7;
					iTagID |= btTemp;
				}
			}

			// Add new Child
			ASNDefinition defChild = null;
			if(def != null)
				defChild = def.getChild(iTagID);

			// Begin
			int iResult = mhandler.beginNode(iTagID,btClass,btConstructed,defChild);
			try
			{
				if(iResult == ASNDataHandler.STATUS_END)
					return iResult;

				// Phan tich length field
				btTemp = mbtBuffer[miBufferIndex++];
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
						iLength += mbtBuffer[miBufferIndex++] & 0xFF;
						btTemp--;
					}
				}
				else
					iLength = btTemp & 0xFF;

				// Phan tich content field
				if(btConstructed == 1) // Du lieu co cau truc
				{
					if(iResult == ASNDataHandler.STATUS_CONTINUE)
					{
						if(iLength < 0) // Content field ket thuc boi EOC
							iResult = parse( -1,defChild);
						else
							iResult = parse(miBufferIndex + miPurgedLength + iLength,defChild);
					}
					else
					{
						if(iLength < 0) // Content field ket thuc boi EOC
							iResult = ignore();
						else
						{
							if(!readNext(iLength))
								return ASNDataHandler.STATUS_END;
							else
								miBufferIndex += iLength;
						}
					}
				}
				else // Du lieu co ban
				{
					if(iLength < 0) // Content field ket thuc boi EOC
					{
						int iLastIndex = miBufferIndex;
						int iLastPurged = miPurgedLength;
						while(readNext())
						{
							while(miBufferIndex < miBufferLength &&
								  mbtBuffer[miBufferIndex] != 0)
								miBufferIndex++;
							if(miBufferIndex < mbtBuffer.length)
							{
								if(miBufferIndex + 1 < mbtBuffer.length &&
								   mbtBuffer[miBufferIndex] == 0)
									miBufferIndex++;
								if(iResult == ASNDataHandler.STATUS_CONTINUE)
								{
									byte[] btData = BufferUtil.getData(mbtBuffer,iLastIndex - miPurgedLength + iLastPurged,miBufferIndex);
									mhandler.dataNode(btData);
								}
								break;
							}
						}
						return ASNDataHandler.STATUS_END;
					}
					else
					{
						if(!readNext(iLength))
							return ASNDataHandler.STATUS_END;
						if(iResult == ASNDataHandler.STATUS_CONTINUE)
						{
							byte[] btData = BufferUtil.getData(mbtBuffer,miBufferIndex,miBufferIndex += iLength);
							mhandler.dataNode(btData);
						}
					}
				}

				// End event
				if(iResult == ASNDataHandler.STATUS_END)
					return iResult;
			}
			finally
			{
				mhandler.endNode();
			}

			// Kiem tra xem da den luc ket thuc chua
			if(!readNext())
				return ASNDataHandler.STATUS_END;
			else
			{
				if(iEndLocation > 0 && miBufferIndex + miPurgedLength >= iEndLocation) // Ket thuc boi do dai
					return ASNDataHandler.STATUS_CONTINUE;
				else
				{
					btTemp = mbtBuffer[miBufferIndex];
					if(btTemp == 0) // Ket thuc boi EOC
					{
						miBufferIndex++;
						if(readNext())
						{
							if(mbtBuffer[miBufferIndex] == 0)
								miBufferIndex++;
						}
						return ASNDataHandler.STATUS_CONTINUE;
					}
				}
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Ignore one node
	 * @return int
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	private int ignore() throws Exception
	{
		// Variables
		byte btTemp;
		while(true)
		{
			// Gap ky tu ket thuc
			if(btTerminatedSymbol >= 0)
			{
				if(!readNext() || mbtBuffer[miBufferIndex] == btTerminatedSymbol)
					return ASNDataHandler.STATUS_END;
			}

			// Get indentifier field
			btTemp = mbtBuffer[miBufferIndex++];

			// Bit thu 3.Contructed or primitive
			byte btConstructed = (byte)((btTemp & 0x20) >>> 5); // 00100000

			// Bo qua identifier & tag
			if((btTemp & 0x1F) >= 31)
			{
				// Tim cho toi khi bit dau tien != 0
				miBufferIndex++;
				while((mbtBuffer[miBufferIndex] >>> 7) != 0)
					miBufferIndex++;
			}

			// Phan tich length field
			btTemp = mbtBuffer[miBufferIndex++];
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
					iLength += mbtBuffer[miBufferIndex++] & 0xFF;
					btTemp--;
				}
			}
			else
				iLength = btTemp & 0xFF;

			// Phan tich content field
			if(btConstructed == 1) // Du lieu co cau truc
			{
				if(iLength < 0) // Content field ket thuc boi EOC
				{
					if(ignore() == ASNDataHandler.STATUS_END)
						return ASNDataHandler.STATUS_END;
				}
				else
				{
					if(!readNext(iLength))
						return ASNDataHandler.STATUS_END;
					else
						miBufferIndex += iLength;
				}
			}
			else // Du lieu co ban
			{
				if(iLength < 0) // Content field ket thuc boi EOC
				{
					while(readNext())
					{
						while(miBufferIndex < miBufferLength &&
							  mbtBuffer[miBufferIndex] != 0)
							miBufferIndex++;
						if(miBufferIndex < mbtBuffer.length)
						{
							if(miBufferIndex + 1 < mbtBuffer.length &&
							   mbtBuffer[miBufferIndex] == 0)
								miBufferIndex++;
							break;
						}
					}
					return ASNDataHandler.STATUS_END;
				}
				else
				{
					if(!readNext(iLength))
						return ASNDataHandler.STATUS_END;
					miBufferIndex += iLength;
				}
			}

			// Kiem tra xem da den luc ket thuc chua
			if(!readNext())
				return ASNDataHandler.STATUS_END;
			else
			{
				btTemp = mbtBuffer[miBufferIndex];
				if(btTemp == 0) // Ket thuc boi EOC
				{
					miBufferIndex++;
					if(readNext())
					{
						if(mbtBuffer[miBufferIndex] == 0)
							miBufferIndex++;
					}
					return ASNDataHandler.STATUS_CONTINUE;
				}
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Parse asndata from inputstream
	 * @param is InputStream
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void parse(InputStream is) throws Exception
	{
		synchronized(is)
		{
			mis = is;
			miBufferIndex = 0;
			miPurgedLength = 0;
			mbAvaiable = true;
			while(readNext())
			{
				// Gap ky tu key thuc
				if(btTerminatedSymbol >= 0)
				{
					miBufferIndex = BufferUtil.lastIndexOfSequence(mbtBuffer,miBufferIndex,btTerminatedSymbol);
					if(miBufferIndex < 0)
						miBufferIndex = miBufferLength;
				}

				// Parse content
				parse(0,mdefRoot);
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Parse asndata from file
	 * @param strFileName String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void parse(String strFileName) throws Exception
	{
		FileInputStream is = null;
		try
		{
			is = new FileInputStream(strFileName);
			parse(is);
		}
		finally
		{
			FileUtil.safeClose(is);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Make original structure
	 * @param is InputStream
	 * @param os OutputStream
	 * @param fmt ASNFormat
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public static void storeOriginalStructure(InputStream is,OutputStream os,ASNFormat fmt) throws Exception
	{
		class ASNDataHandlerEx extends DefaultASNDataHandler
		{
			private OutputStream mos;
			private ASNFormat mfmt;
			public ASNDataHandlerEx(ASNFormat fmt,OutputStream os)
			{
				mos = os;
				mfmt = fmt;
			}
			public void processRecord(ASNData dat) throws Exception
			{
				dat.storeText(mos,"",mfmt);
			}
		}
		ASNParser parser = new ASNParser(fmt.mdefRoot,new ASNDataHandlerEx(fmt,os));
		parser.parse(is);
	}
	////////////////////////////////////////////////////////
	public static void storeOriginalStructure(String strSrcFile,String strDesFile,ASNFormat fmt) throws Exception
	{
		FileInputStream is = null;
		FileOutputStream os = null;
		try
		{
			os = new FileOutputStream(strDesFile);
			is = new FileInputStream(strSrcFile);
			storeOriginalStructure(is,os,fmt);
		}
		finally
		{
			FileUtil.safeClose(is);
			if(os != null)
				os.close();
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Make text file using cursor list
	 * @param is InputStream
	 * @param os OutputStream
	 * @param defRoot ASNDefinition
	 * @param cur ASNCursor[]
	 * @param strSeperator String
	 * @param bIgnore boolean
	 * @return int[]
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public static int[] storeText(InputStream is,OutputStream os,ASNDefinition defRoot,ASNCursor[] cur,String strSeperator,boolean bIgnore) throws Exception
	{
		int[] iReturn = new int[cur.length];
		ASNParser parser = null;
		if(bIgnore)
		{
			defRoot.fillIgnore(cur);
			parser = new ASNParser(defRoot,new IgnorableSTDataHandler(os,cur,strSeperator,iReturn));
		}
		else
			parser = new ASNParser(defRoot,new STDataHandler(os,cur,strSeperator,iReturn));
		parser.parse(is);
		return iReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 * Make text file using cursor list
	 * @param is InputStream
	 * @param os OutputStream
	 * @param defRoot ASNDefinition
	 * @param cur ASNCursor[]
	 * @param strSeperator String
	 * @return int[]
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public static int[] storeText(InputStream is,OutputStream os,ASNDefinition defRoot,ASNCursor[] cur,String strSeperator) throws Exception
	{
		return storeText(is,os,defRoot,cur,strSeperator,true);
	}
	////////////////////////////////////////////////////////
	public static int[] storeText(String strSrcFile,String strDesFile,ASNDefinition defRoot,ASNCursor[] cur,String strSeperator,boolean bIgnore) throws Exception
	{
		FileInputStream is = null;
		FileOutputStream os = null;
		try
		{
			os = new FileOutputStream(strDesFile);
			is = new FileInputStream(strSrcFile);
			return storeText(is,os,defRoot,cur,strSeperator,bIgnore);
		}
		finally
		{
			FileUtil.safeClose(is);
			if(os != null)
				os.close();
		}
	}
	////////////////////////////////////////////////////////
	public static int[] storeText(String strSrcFile,String strDesFile,ASNDefinition defRoot,ASNCursor[] cur,String strSeperator) throws Exception
	{
		return storeText(strSrcFile,strDesFile,defRoot,cur,strSeperator,true);
	}
}
////////////////////////////////////////////////////////
class STDataHandler extends DefaultASNDataHandler
{
	private OutputStream mos;
	private ASNCursor[] mcur;
	private String mstrSeperator;
	private int[] miReturn;
	////////////////////////////////////////////////////////
	public STDataHandler(OutputStream os,ASNCursor[] cur,String strSeperator,int[] iReturn)
	{
		mos = os;
		mcur = cur;
		mstrSeperator = strSeperator;
		miReturn = iReturn;
	}
	////////////////////////////////////////////////////////
	public void processRecord(ASNData dat) throws Exception
	{
		for(int iCursorIndex = 0;iCursorIndex < mcur.length;iCursorIndex++)
		{
			ASNCursor curTemp = mcur[iCursorIndex];
			curTemp.mdatCursor[0] = dat;
			curTemp.miLevel = 0;
			miReturn[iCursorIndex] += curTemp.storeText(mos,mstrSeperator);
		}
	}
}
////////////////////////////////////////////////////////
class IgnorableSTDataHandler extends STDataHandler
{
	////////////////////////////////////////////////////////
	public IgnorableSTDataHandler(OutputStream os,ASNCursor[] cur,String strSeperator,int[] iReturn)
	{
		super(os,cur,strSeperator,iReturn);
	}
	////////////////////////////////////////////////////////
	public int beginNode(int iTagID,byte btClass,byte btConstructed,ASNDefinition def) throws Exception
	{
		int iResult = super.beginNode(iTagID,btClass,btConstructed,def);
		if(def.mbIgnore)
			return STATUS_IGNORE;
		return iResult;
	}
}
