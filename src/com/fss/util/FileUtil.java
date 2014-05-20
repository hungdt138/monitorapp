package com.fss.util;

import java.io.*;

/**
 * <p>Title: FileUtil</p>
 * <p>Description: Utility for file processing</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class FileUtil
{
	////////////////////////////////////////////////////////
	// Constant
	////////////////////////////////////////////////////////
	public static final int BUFFER_SIZE = 65536; // 64K
	public static final int MAX_SAMALL_FILE_SIZE = 524288; // 512K
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strCurrenDir String
	 * @param strFileName String
	 * @return String
	 */
	////////////////////////////////////////////////////////
	public static String getAbsolutePath(String strCurrenDir,String strFileName)
	{
		if(!strFileName.startsWith("/") && !strFileName.startsWith("\\"))
		{
			if(!strCurrenDir.endsWith("/") && !strCurrenDir.endsWith("\\"))
				return strCurrenDir + "/" + strFileName;
			return strCurrenDir + strFileName;
		}
		return strFileName;
	}
	////////////////////////////////////////////////////////
	/**
	 * Create folder if it's not exist
	 * @param strFolder folder to create if it does not exist
	 * @throws IOException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static void forceFolderExist(String strFolder) throws IOException
	{
		File flTemp = new File(strFolder);
		if(!flTemp.exists())
		{
			if(!flTemp.mkdirs())
				throw new IOException("Could not create folder " + strFolder);
		}
		else if(!flTemp.isDirectory())
			throw new IOException("A file with name"  + strFolder + " already exist");
	}
	////////////////////////////////////////////////////////
	/**
	 * Rename file from source to destination
	 * @param strSrc source file
	 * @param strDest destination file
	 * @param deleteIfExist if true and file exist then replace file
	 * @throws IOException if error occured
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static void renameFile(String strSrc,String strDest, boolean deleteIfExist) throws IOException
	{
		File flSrc = new File(strSrc);
		File flDest = new File(strDest);
		if(flSrc.getAbsolutePath().equals(flDest.getAbsolutePath()))
		   return;
		if(flDest.exists())
		{
			if(deleteIfExist)
				flDest.delete();
			else
				throw new IOException("Destination file existing, can not rename file");
		}
		if(!flSrc.renameTo(flDest))
			throw new IOException("Can not rename file, unknown error");
	}
	////////////////////////////////////////////////////////
	/**
	 * Rename file from src to des, override if des exist
	 * @param strSrc source file
	 * @param strDest destination file
	 * @return true if succees, otherswise false
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static boolean renameFile(String strSrc,String strDest)
	{
		File flSrc = new File(strSrc);
		File flDest = new File(strDest);
		if(flSrc.getAbsolutePath().equals(flDest.getAbsolutePath()))
		   return true;
		if(flDest.exists())
			flDest.delete();
		return flSrc.renameTo(flDest);
	}
	////////////////////////////////////////////////////////
	/**
	 * Copy file from src to des, override if des exist
	 * @param strSrc source file
	 * @param strDest destination file
	 * @return true if succees, otherswise false
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static boolean copyFile(String strSrc,String strDest)
	{
		FileInputStream isSrc = null;
		FileOutputStream osDest = null;
		try
		{
			File flDest = new File(strDest);
			if(flDest.exists()) flDest.delete();

			File flSrc = new File(strSrc);
			if(!flSrc.exists()) return false;

			isSrc = new FileInputStream(flSrc);
			osDest = new FileOutputStream(flDest);

			byte btData[] = new byte[BUFFER_SIZE];
			int iLength;
			while((iLength = isSrc.read(btData)) != -1)
				osDest.write(btData,0,iLength);

			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			safeClose(isSrc);
			safeClose(osDest);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Delete file
	 * @param strSrc file to delete
	 * @return true if succees, otherswise false
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static boolean deleteFile(String strSrc)
	{
		File flSrc = new File(strSrc);
		return flSrc.delete();
	}
	////////////////////////////////////////////////////////
	/**
	 * Copy resource to file
	 * @param cls Class with valid priviledge
	 * @param strResSource resource path
	 * @param strFile file to copy to
	 * @return true if succees, otherswise false
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static boolean copyResource(Class cls,String strResSource,String strFile)
	{
		InputStream isSrc = null;
		FileOutputStream osDest = null;
		try
		{
			isSrc = cls.getResourceAsStream(strResSource);
			if(isSrc == null) throw new IOException("Resource " + strResSource + " not found");
			osDest = new FileOutputStream(strFile);

			byte btData[] = new byte[BUFFER_SIZE];
			int iLength;
			while((iLength = isSrc.read(btData)) != -1)
				osDest.write(btData,0,iLength);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			safeClose(isSrc);
			safeClose(osDest);
		}
		return true;
	}
	////////////////////////////////////////////////////////
	/**
	 * Delete unused file
	 * @param strPath Path to scan
	 * @param strWildcard scan wildcard
	 * @param iOffset miliseconds to determinate old file
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static void deleteOldFile(String strPath,String strWildcard,int iOffset)
	{
		if(!strPath.endsWith("/"))
			strPath += "/";
		File flFolder = new File(strPath);
		if(!flFolder.exists())
			return;
		String strFileList[] = flFolder.list(new WildcardFilter(strWildcard));
		if(strFileList != null && strFileList.length > 0)
		{
			long lCurrentTime = (new java.util.Date()).getTime();
			for(int iFileIndex = 0;iFileIndex < strFileList.length;iFileIndex++)
			{
				File fl = new File(strPath + strFileList[iFileIndex]);
				if(lCurrentTime - fl.lastModified() >= iOffset)
					fl.delete();
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * @param strFileName String File to check
	 * @param iMaxSize max size
	 * @param iRemainSize remain size
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public static void backup(String strFileName,int iMaxSize,int iRemainSize) throws Exception
	{
		if(iMaxSize <= iRemainSize)
			throw new IllegalArgumentException();
		final java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
		File flSource = new File(strFileName);
		if(flSource.length() > iMaxSize)
		{
			String strNewName = strFileName + "." + fmt.format(new java.util.Date());
			renameFile(strFileName,strNewName);
			RandomAccessFile fl = null;
			FileOutputStream os = null;
			try
			{
				os = new FileOutputStream(strFileName);
				fl = new RandomAccessFile(strNewName,"rw");
				fl.seek(fl.length() - iRemainSize);
				byte bt[] = new byte[iRemainSize];
				int iByteRead = fl.read(bt);
				if(iByteRead != iRemainSize)
					throw new IOException();
				os.write(bt,0,iByteRead);
				fl.setLength(fl.length() - iRemainSize);
			}
			finally
			{
				FileUtil.safeClose(fl);
				FileUtil.safeClose(os);
			}
		}
	}
	////////////////////////////////////////////////////////
	public static void backup(String strFileName,int iMaxSize)
	{
		final java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
		File flSource = new File(strFileName);
		if(flSource.length() > iMaxSize)
		{
			String strNewName = "";
			if(strFileName.indexOf(".") >= 0)
				strNewName = strFileName.substring(0,strFileName.lastIndexOf(".")) +
							 fmt.format(new java.util.Date()) +
							 strFileName.substring(strFileName.lastIndexOf("."));
			else
				strNewName = strFileName + fmt.format(new java.util.Date());
			renameFile(strFileName,strNewName);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Backup used for file relation thread
	 * @param strSourcePath String (Must have '/' at last string)
	 * @param strBackupPath String (Must have '/' at last string)
	 * @param strSourceFile String
	 * @param strBackupFile String
	 * @param strBackupStyle String
	 * @throws Exception
	 * @return String
	 */
	////////////////////////////////////////////////////////
	public static String backup(String strSourcePath,String strBackupPath,String strSourceFile,String strBackupFile,String strBackupStyle) throws Exception
	{
		return backup(strSourcePath,strBackupPath,strSourceFile,strBackupFile,strBackupStyle,"");
	}
	////////////////////////////////////////////////////////
	public static String backup(String strSourcePath,String strBackupPath,String strSourceFile,String strBackupFile,String strBackupStyle,String strAdditionPath) throws Exception
	{
		// Backup file
		if(strBackupStyle.equals("Delete file"))
		{
			if(!FileUtil.deleteFile(strSourcePath + strSourceFile))
				throw new Exception("Cannot delete file " + strSourcePath + strSourceFile);
		}
		else if(strBackupPath.length() > 0)
		{
			// Backup source file
			String strCurrentDate = "";
			if(strBackupStyle.equals("Daily"))
				strCurrentDate = StringUtil.format(new java.util.Date(),"yyyyMMdd") + "/";
			else if(strBackupStyle.equals("Monthly"))
				strCurrentDate = StringUtil.format(new java.util.Date(),"yyyyMM") + "/";
			else if(strBackupStyle.equals("Yearly"))
				strCurrentDate = StringUtil.format(new java.util.Date(),"yyyy") + "/";
			FileUtil.forceFolderExist(strBackupPath + strCurrentDate + strAdditionPath);
			if(!FileUtil.renameFile(strSourcePath + strSourceFile,strBackupPath + strCurrentDate + strAdditionPath + strBackupFile))
				throw new Exception("Cannot rename file " + strSourcePath + strSourceFile + " to " + strBackupPath + strCurrentDate + strBackupFile);
			return strBackupPath + strCurrentDate + strBackupFile;
		}
		return "";
	}
	////////////////////////////////////////////////////////
	/**
	 * Close object safely
	 * @param is InputStream
	 */
	////////////////////////////////////////////////////////
	public static void safeClose(InputStream is)
	{
		try
		{
			if(is != null)
				is.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Close object safely
	 * @param os OutputStream
	 */
	////////////////////////////////////////////////////////
	public static void safeClose(OutputStream os)
	{
		try
		{
			if(os != null)
				os.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Close object safely
	 * @param fl RandomAccessFile
	 */
	////////////////////////////////////////////////////////
	public static void safeClose(RandomAccessFile fl)
	{
		try
		{
			if(fl != null)
				fl.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Get sequence value from file
	 * @param strFileName String
	 * @return long
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public static long getSequenceValue(String strFileName) throws Exception
	{
		return getSequenceValue(new File(strFileName));
	}
	////////////////////////////////////////////////////////
	/**
	 * Get sequence value from file
	 * @param fl File
	 * @return long
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public static long getSequenceValue(File fl) throws Exception
	{
		long l = 0;
		try
		{
			String str = FileUtil.readSmallFile(fl);
			l = Long.parseLong(str);
		}
		catch(Exception e)
		{
		}
		FileUtil.writeSmallFile(fl,String.valueOf(l + 1));
		return l;
	}
	////////////////////////////////////////////////////////
	/**
	 * Read small content from file
	 * @param strFileName String
	 * @return String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public static String readSmallFile(String strFileName) throws Exception
	{
		return readSmallFile(new File(strFileName));
	}
	////////////////////////////////////////////////////////
	public static String readSmallFile(File fl) throws Exception
	{
		if(fl.length() > MAX_SAMALL_FILE_SIZE)
			throw new Exception("File content too large");
		FileInputStream is = null;
		try
		{
			is = new FileInputStream(fl);
			return StreamUtil.readStream(is);
		}
		finally
		{
			safeClose(is);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Write small content to file
	 * @param strFileName String
	 * @param strContent String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public static void writeSmallFile(String strFileName,String strContent) throws Exception
	{
		writeSmallFile(new File(strFileName),strContent);
	}
	////////////////////////////////////////////////////////
	/**
	 * Write small content to file
	 * @param fl File
	 * @param strContent String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public static void writeSmallFile(File fl,String strContent) throws Exception
	{
		if(strContent.length() > MAX_SAMALL_FILE_SIZE)
			throw new Exception("Content too large");
		FileOutputStream os = null;
		try
		{
			os = new FileOutputStream(fl);
			os.write(strContent.getBytes());
		}
		finally
		{
			safeClose(os);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Format file name
	 * @param strFileName String
	 * @param strFileFormat String
	 * @throws AppException
	 * @return String
	 */
	////////////////////////////////////////////////////////
	public static String formatFileName(String strFileName,String strFileFormat) throws AppException
	{
		if(strFileName == null || strFileName.length() == 0 ||
		   strFileFormat == null || strFileFormat.length() == 0)
			return strFileName;
		int iExtIndex = strFileName.lastIndexOf('.');
		if(iExtIndex < 0)
			iExtIndex = strFileName.length();
		int iBaseIndex = strFileName.lastIndexOf('/');
		if(iBaseIndex < 0)
			iBaseIndex = strFileName.lastIndexOf('\\');
		if(iBaseIndex < 0)
			iBaseIndex = 0;
		String strBaseFileName = strFileName.substring(iBaseIndex,iExtIndex);
		String strFileExtension = "";
		if(iExtIndex < strFileName.length() - 1)
			strFileExtension = strFileName.substring(iExtIndex + 1,strFileName.length());
		strFileFormat = StringUtil.replaceAll(strFileFormat,"$FileName",strFileName);
		strFileFormat = StringUtil.replaceAll(strFileFormat,"$BaseFileName",strBaseFileName);
		strFileFormat = StringUtil.replaceAll(strFileFormat,"$FileExtension",strFileExtension);
		return strFileFormat;
	}
}
