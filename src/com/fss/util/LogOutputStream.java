package com.fss.util;

import java.io.*;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class LogOutputStream extends OutputStream
{
	////////////////////////////////////////////////////////
	// Constant
	////////////////////////////////////////////////////////
	private int iMaxLogFileSize = 1048576;
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	private File mflMain;
	////////////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////////////
	public LogOutputStream(String strFileName) throws IOException
	{
		mflMain = new File(strFileName);
		if(!mflMain.exists())
			mflMain.createNewFile();
	}
	////////////////////////////////////////////////////////
	// Override function
	////////////////////////////////////////////////////////
	public void write(int i)
	{
		RandomAccessFile fl = null;
		try
		{
			fl = new RandomAccessFile(mflMain,"rw");
			fl.seek(fl.length());
			fl.write(i);
		}
		catch(Exception e)
		{
		}
		finally
		{
			FileUtil.safeClose(fl);
		}
	}
	////////////////////////////////////////////////////////
	public void write(byte[] bt)
	{
		FileUtil.backup(mflMain.getAbsolutePath(),iMaxLogFileSize);
		RandomAccessFile fl = null;
		try
		{
			fl = new RandomAccessFile(mflMain,"rw");
			fl.seek(fl.length());
			fl.write(bt);
		}
		catch(Exception e)
		{
		}
		finally
		{
			FileUtil.safeClose(fl);
		}
	}
	////////////////////////////////////////////////////////
	public void write(byte[] bt,int offset,int length)
	{
		FileUtil.backup(mflMain.getAbsolutePath(),iMaxLogFileSize);
		RandomAccessFile fl = null;
		try
		{
			fl = new RandomAccessFile(mflMain,"rw");
			fl.seek(fl.length());
			if(length > 2)
			{
				final java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd/MM HH:mm:ss");
				String strLog = fmt.format(new java.util.Date()) + " ";
				fl.write(strLog.getBytes());
			}
			fl.write(bt,offset,length);
		}
		catch(Exception e)
		{
		}
		finally
		{
			FileUtil.safeClose(fl);
		}
	}
	public void setMaxLogFileSize(int maxSize) throws Exception
	{
		if(maxSize <= 0)
			throw new Exception("FileSize must greater than 0 byte");
		iMaxLogFileSize = maxSize;
	}
	public int getMaxLogFileSize()
	{
		return iMaxLogFileSize;
	}
}
