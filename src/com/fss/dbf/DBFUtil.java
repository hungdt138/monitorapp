package com.fss.dbf;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.text.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class DBFUtil
{
	////////////////////////////////////////////////////////
	// DBF constant
	////////////////////////////////////////////////////////
	static final int BULK_SIZE = 32;
	static final int FLAG_INDEX = 0;
	static final int DATE_INDEX = 1;
	static final int NUMBER_OF_REC_INDEX = 4;
	static final int LENGTH_OF_HEADER_INDEX = 8;
	static final int LENGTH_OF_REC_INDEX = 10;
	static final int END_OF_HEADER_SYMBOL = 13;
	static final int END_OF_DATA_SYMBOL = 26;
	static final int RECORD_IS_DELETED = '*';
	static final int RECORD_IS_NOT_DELETED = ' ';
	static final byte RECORD_IS_NULL = 0;
	static final byte ACTION_NONE = 0;
	static final byte ACTION_ADD = 1;
	static final byte ACTION_UPDATE = 2;
	public static final java.text.SimpleDateFormat DBF_DATE_FORMAT = new java.text.SimpleDateFormat("yyyyMMdd");
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	public RandomAccessFile mflMain; // For open DBF file
	private byte mbtVersion; //00	FoxBase+, FoxPro, dBaseIII+, dBaseIV, no memo - 0x03
					//FoxBase+, dBaseIII+ with memo - 0x83
					//FoxPro with memo - 0xF5
					//dBaseIV with memo - 0x8B
					//dBaseIV with SQL Table - 0x8E
	private byte mbtYY,mbtMM,mbtDD; //01-03   Last update, format YYMMDD
	private long mlRecordCount; //byte 04 - 07
	private int miHeaderLength,miRecordLength; //byte 08 - 11
	private boolean mbUpdateHeader = true;
	private boolean mbRecordStatusAvaiable = true;
	// 12-13	Reserved, fill with 0x00
	//private byte mbtTransactionFlag,mbtEncryptFlag; //byte 14 - 15 (dBaseIV)
	// 16-27   dBaseIV multi-user environment use
	//private byte mbtIndexExist,mbtLanguageID; //byte 28 - 29 (dBaseIV)
	// 30-31   Reserved fill with 0x00

	////////////////////////////////////////////////////////
	// DBF column
	////////////////////////////////////////////////////////
	public static final int ALIGN_LEFT = 0;
	public static final int ALIGN_RIGHT = 1;
	////////////////////////////////////////////////////////
	public class DBFColumn
	{
		static final int NAME_INDEX = 0;
		static final int TYPE_INDEX = 11;
		static final int LENGTH_INDEX = 16;
		static final int PRECISION_INDEX = 17;

		public String strFieldName; //0-10	Field Name ASCII padded with 0x00
		public int iFieldType; // 11	Field Type Identifier
						// C	   Character
						// D	   Date, format YYYYMMDD
						// F	   Floating Point
						// L	   Logical, T:t,F:f,Y:y,N:n,?-not initialized
						// N	   Numeric
		public int iFieldLength; //16	Field length in bytes
		public int iPrecision; //17	Field decimal places
		public int iPosition;
		public int iAlign;
		////////////////////////////////////////////////////////
		public DBFColumn(byte[] btInfo)
		{
			if(btInfo[0] != END_OF_HEADER_SYMBOL)
			{
				// get field name
				int iIndex = NAME_INDEX;
				while((iIndex < 10) && (btInfo[iIndex] != 0)) iIndex++;
				strFieldName = (new String(btInfo,0,iIndex)).trim();

				// get field type
				iIndex = TYPE_INDEX;
				if(btInfo[iIndex] == 'C') iFieldType = Types.CHAR;
				else if(btInfo[iIndex] == 'N') iFieldType = Types.NUMERIC;
				else if(btInfo[iIndex] == 'D') iFieldType = Types.DATE;
				else if(btInfo[iIndex] == 'L') iFieldType = Types.BIT;
				else if(btInfo[iIndex] == 'M') iFieldType = Types.BINARY;
				else iFieldType = Types.BINARY;

				// get field length
				iIndex = LENGTH_INDEX;
				iFieldLength = fixSignedByte(btInfo[iIndex]);

				// get field precision
				iIndex = PRECISION_INDEX;
				iPrecision = fixSignedByte(btInfo[iIndex]);
			}
		}
	};
	////////////////////////////////////////////////////////
	// Column List
	////////////////////////////////////////////////////////
	private Vector mvtColumn;
	////////////////////////////////////////////////////////
	// For record navigation
	////////////////////////////////////////////////////////
	private long mlRecordIndex;
	private long mlFilePosition;
	private byte mbtRecordData[];
	private byte mbtAction = ACTION_NONE;
	////////////////////////////////////////////////////////
	public DBFUtil(String strFileName,boolean blnReadOnly) throws IOException
	{
		try
		{
			if(blnReadOnly)
				mflMain = new RandomAccessFile(strFileName,"r");
			else
				mflMain = new RandomAccessFile(strFileName,"rw");
			loadHeader();
		}
		catch(IOException e)
		{
			try
			{
				close();
			}
			catch(Exception e1)
			{
			}
			throw e;
		}
	}
	////////////////////////////////////////////////////////
	public DBFUtil(String strHeaderFile,String strDataFile,int iDataOffset,boolean blnReadOnly) throws IOException
	{
		try
		{
			load(strHeaderFile,strDataFile,iDataOffset,-1,blnReadOnly);
		}
		catch(IOException e)
		{
			try
			{
				close();
			}
			catch(Exception e1)
			{
			}
			throw e;
		}
	}
	////////////////////////////////////////////////////////
	public DBFUtil(String strHeaderFile,String strDataFile,int iDataOffset,int iRecordCount,boolean blnReadOnly) throws IOException
	{
		try
		{
			load(strHeaderFile,strDataFile,iDataOffset,iRecordCount,blnReadOnly);
		}
		catch(IOException e)
		{
			try
			{
				close();
			}
			catch(Exception e1)
			{
			}
			throw e;
		}
	}
	////////////////////////////////////////////////////////
	private void load(String strHeaderFile,String strDataFile,int iDataOffset,int iRecordCount,boolean blnReadOnly) throws IOException
	{
		// Fix some variable for text file
		mbUpdateHeader = false;
		mbRecordStatusAvaiable = false;

		// Load header
		mflMain = new RandomAccessFile(strHeaderFile,"r");
		loadHeader();
		close();

		if(blnReadOnly)
			mflMain = new RandomAccessFile(strDataFile,"r");
		else
		{
			mflMain = new RandomAccessFile(strDataFile,"rw");
			if(mflMain.length() < iDataOffset)
				mflMain.setLength(iDataOffset);
		}

		// Recalculate some variables
		miHeaderLength = iDataOffset;
		if(iRecordCount > 0)
			mlRecordCount = iRecordCount;
		else
			mlRecordCount = (mflMain.length() - miHeaderLength) / miRecordLength;

		// Check sum
		long lExpected = mlRecordCount * miRecordLength + miHeaderLength;
		if(lExpected > mflMain.length() || lExpected < mflMain.length() - miRecordLength / 2)
			throw new IOException("File size (" + mflMain.length() + ") does not equals to calculated size (" + lExpected + ")");

		// Default
		mlRecordIndex = -1;
		mlFilePosition = miHeaderLength + mlRecordIndex * miRecordLength;
	}
	////////////////////////////////////////////////////////
	public void close() throws IOException
	{
		try
		{
			update();
		}
		finally
		{
			mflMain.close();
		}
	}
	////////////////////////////////////////////////////////
	private void loadHeader() throws IOException
	{
		// get the version flag
		mflMain.seek(FLAG_INDEX);
		mbtVersion = mflMain.readByte();

		// get the last update date
		mflMain.seek(DATE_INDEX);
		mbtYY = mflMain.readByte();
		mbtMM = mflMain.readByte();
		mbtDD = mflMain.readByte();

		// get record count
		byte btRecordCount[] = new byte[4];
		mflMain.seek(NUMBER_OF_REC_INDEX);
		mflMain.readFully(btRecordCount);
		mlRecordCount = (long)byteToDword(btRecordCount);

		// get length of header
		byte btHeaderLength[] = new byte[2];
		mflMain.seek(LENGTH_OF_HEADER_INDEX);
		mflMain.readFully(btHeaderLength);
		miHeaderLength = (int)byteToWord(btHeaderLength);

		// get length of record
		byte btRecordLength[] = new byte[2];
		mflMain.seek(LENGTH_OF_REC_INDEX);
		mflMain.readFully(btRecordLength);
		miRecordLength = (int)byteToWord(btRecordLength);
		if(!mbRecordStatusAvaiable)
			miRecordLength--;
		mbtRecordData = new byte[miRecordLength];

		// Check sum
		long lExpected = mlRecordCount * miRecordLength + miHeaderLength;
		if(lExpected > mflMain.length() || lExpected < mflMain.length() - miRecordLength / 2)
			throw new IOException("File size (" + mflMain.length() + ") does not equals to calculated size (" + lExpected + ")");

		// Load column definition
		byte btColumnInfo[] = new byte[32];
		int iFieldPosition = 1; // Record status is byte 0
		if(!mbRecordStatusAvaiable)
			iFieldPosition = 0;
		mvtColumn = new Vector();
		mflMain.seek(BULK_SIZE);
		do
		{
			mflMain.read(btColumnInfo);
			if(btColumnInfo[0] != END_OF_HEADER_SYMBOL)
			{
				DBFColumn colTmp = new DBFColumn(btColumnInfo);
				colTmp.iPosition = iFieldPosition;
				iFieldPosition += colTmp.iFieldLength;
				mvtColumn.addElement(colTmp);
			}
		}
		while(btColumnInfo[0] != 13);

		// Default
		mlRecordIndex = -1;
		mlFilePosition = miHeaderLength + mlRecordIndex * miRecordLength;
	}
	////////////////////////////////////////////////////////
	// Navigate function
	////////////////////////////////////////////////////////
	public boolean isEOF()
	{
		if(mlRecordCount == 0 || mlRecordIndex >= mlRecordCount) return true;
		return false;
	}
	public boolean isBOF()
	{
		if(mlRecordCount == 0 || mlRecordIndex < 0) return true;
		return false;
	}
	public boolean moveFirst() throws IOException
	{
		update();
		return move(0);
	}
	public boolean movePrevious() throws IOException
	{
		update();
		if(mlRecordIndex <= 0) return false;
		mlRecordIndex --;
		mlFilePosition -= miRecordLength;
		mbtRecordData[0] = RECORD_IS_NULL;
		return true;
	}
	public boolean moveNext() throws IOException
	{
		update();
		if(mlRecordIndex >= mlRecordCount - 1) return false;
		mlRecordIndex++;
		mlFilePosition += miRecordLength;
		mbtRecordData[0] = RECORD_IS_NULL;
		return true;
	}
	public boolean moveLast() throws IOException
	{
		update();
		return move(mlRecordCount - 1);
	}
	public boolean move(long lRecordIndex) throws IOException
	{
		update();
		if(lRecordIndex >= mlRecordCount || lRecordIndex < 0)
			return false;

		mlRecordIndex = lRecordIndex;
		mlFilePosition = miHeaderLength + mlRecordIndex * miRecordLength;
		mbtRecordData[0] = RECORD_IS_NULL;

		return true;
	}
	public void goTop()
	{
		mlRecordIndex = -1;
		mlFilePosition = miHeaderLength + mlRecordIndex * miRecordLength;
		mbtRecordData[0] = RECORD_IS_NULL;
	}
	public long getRecordIndex()
	{
		return mlRecordIndex;
	}
	////////////////////////////////////////////////////////
	// Field info
	////////////////////////////////////////////////////////
	public DBFColumn getFieldInfo(int iFieldIndex)
	{
		return (DBFColumn)mvtColumn.elementAt(iFieldIndex);
	}
	////////////////////////////////////////////////////////
	public int getFieldCount()
	{
		return mvtColumn.size();
	}
	public String getFieldName(int iFieldIndex)
	{
		return getFieldInfo(iFieldIndex).strFieldName;
	}
	public int getFieldType(int iFieldIndex)
	{
		return getFieldInfo(iFieldIndex).iFieldType;
	}
	public int getFieldIndex(String strFieldName)
	{
		int iFieldCount = getFieldCount();
		for(int iFieldIndex = 0;iFieldIndex < iFieldCount;iFieldIndex++)
		{
			if(getFieldName(iFieldIndex).equals(strFieldName))
				return iFieldIndex;
		}
		return -1;
	}
	////////////////////////////////////////////////////////
	// Record info
	////////////////////////////////////////////////////////
	public long getRecordCount()
	{
		return mlRecordCount;
	}
	////////////////////////////////////////////////////////
	// Data manipulate
	////////////////////////////////////////////////////////
	public void clearData() throws IOException
	{
		mflMain.setLength(this.miHeaderLength);

		// Default
		mlRecordIndex = -1;
		mlFilePosition = miHeaderLength + mlRecordIndex * miRecordLength;
	}
	////////////////////////////////////////////////////////
	public String getFieldValue(int iFieldIndex) throws IOException
	{
		if(mbtRecordData[0] == RECORD_IS_NULL)
		{
			mflMain.seek(mlFilePosition);
			mflMain.readFully(mbtRecordData);
		}
		DBFColumn colTmp = getFieldInfo(iFieldIndex);
		String strReturn = new String(mbtRecordData,colTmp.iPosition,colTmp.iFieldLength);
		return strReturn.trim();
	}
	////////////////////////////////////////////////////////
	public void setFieldAlign(int iAlign,int iFieldIndex)
	{
		DBFColumn colTmp = getFieldInfo(iFieldIndex);
		colTmp.iAlign = iAlign;
	}
	////////////////////////////////////////////////////////
	public void setFieldValue(String strFieldData,int iFieldIndex) throws IOException
	{
		DBFColumn colTmp = getFieldInfo(iFieldIndex);
		if(strFieldData.length() > colTmp.iFieldLength)
			throw new IOException("Value '" + strFieldData + "' too large for column '" + colTmp.strFieldName + "'");
		if(mbtAction == ACTION_NONE) mbtAction = ACTION_UPDATE;
		if(strFieldData != null)
		{
			byte btFieldData[] = strFieldData.getBytes();
			fillArray(mbtRecordData,btFieldData,colTmp.iPosition,colTmp.iFieldLength,(byte)' ',colTmp.iAlign);
		}
		else
			fillArray(mbtRecordData,(byte)' ',colTmp.iPosition,colTmp.iFieldLength);
	}
	////////////////////////////////////////////////////////
	public void addRow() throws IOException
	{
		update();
		mlRecordIndex = mlRecordCount;
		mlRecordCount++;
		mlFilePosition = miHeaderLength + mlRecordIndex * miRecordLength;
		mbtAction = ACTION_ADD;
		Arrays.fill(mbtRecordData,(byte)' ');
	}
	////////////////////////////////////////////////////////
	public void update() throws IOException
	{
		if(mbtAction != ACTION_NONE)
		{
			if(mbUpdateHeader && mbtAction == ACTION_ADD)
			{
				mflMain.seek(NUMBER_OF_REC_INDEX);
				mflMain.write(dwordToByte(mlRecordCount));
			}
			mflMain.seek(mlFilePosition);
			mflMain.write(mbtRecordData);
			if(mbUpdateHeader && mbtAction == ACTION_ADD)
				mflMain.write(END_OF_DATA_SYMBOL);
			mbtAction = ACTION_NONE;
		}
	}
	////////////////////////////////////////////////////////
	public void setString(String strFieldData,int iFieldIndex) throws IOException
	{
		DBFColumn col = this.getFieldInfo(iFieldIndex);
		int iFieldType = col.iFieldType;
		if(iFieldType == Types.DATE)
		{
			try
			{
				DBF_DATE_FORMAT.parse(strFieldData);
			}
			catch(Exception e)
			{
				 throw new IOException("Invalid field type");
			}
		}
		else if(iFieldType == Types.NUMERIC)
		{
			try
			{
				Double.parseDouble(strFieldData);
			}
			catch(Exception e)
			{
				throw new IOException("Invalid field type");
			}
			int iIndex = strFieldData.indexOf('.');
			if(iIndex >= 0 && (strFieldData.length() - iIndex - 1) > col.iPrecision)
				throw new IOException("Precision too large");
		}
		setFieldValue(strFieldData,iFieldIndex);
	}
	////////////////////////////////////////////////////////
	public void setDate(String strFieldData, int iFieldIndex) throws IOException
	{
		java.util.Date dtFieldData = null;
		int iFieldType = getFieldType(iFieldIndex);
		try
		{
			dtFieldData = DBF_DATE_FORMAT.parse(strFieldData);
		}
		catch(ParseException ex)
		{
		}
		if(iFieldType == Types.DATE)
			setFieldValue(DBF_DATE_FORMAT.format(dtFieldData),iFieldIndex);
		else throw new IOException("Invalid field type");
	}

	public void setDate(java.util.Date dtFieldData,int iFieldIndex) throws IOException
	{
		int iFieldType = getFieldType(iFieldIndex);
		if(iFieldType == Types.CHAR || iFieldType == Types.DATE) setFieldValue(DBF_DATE_FORMAT.format(dtFieldData),iFieldIndex);
		else throw new IOException("Invalid field type");
	}
	////////////////////////////////////////////////////////
	public void setLong(long lFieldData,int iFieldIndex) throws IOException
	{
		int iFieldType = getFieldType(iFieldIndex);
		if(iFieldType == Types.CHAR || iFieldType == Types.NUMERIC) setFieldValue(Long.toString(lFieldData),iFieldIndex);
		else throw new IOException("Invalid field type");
	}
	////////////////////////////////////////////////////////
	public void setDouble(double dblFieldData,int iFieldIndex) throws IOException
	{
		DBFColumn col = this.getFieldInfo(iFieldIndex);
		int iFieldType = col.iFieldType;
		if(iFieldType == Types.CHAR) setFieldValue(Double.toString(dblFieldData),iFieldIndex);
		else if(iFieldType == Types.NUMERIC)
		{
			String strValue = Double.toString(dblFieldData);
			int iDotIndex = strValue.indexOf('.');
			if(iDotIndex > 0)
			{
				int iPrecision = strValue.length() - iDotIndex - 1;
				if(iPrecision > col.iPrecision) strValue = strValue.substring(0,iDotIndex + col.iPrecision + 1);
			}
			setFieldValue(strValue,iFieldIndex);
		}
		else throw new IOException("Invalid field type");
	}
	////////////////////////////////////////////////////////
	public static short fixSignedByte(byte btValue)
	{
		if(btValue < 0) return (short)(btValue + 256);
		return btValue;
	}
	////////////////////////////////////////////////////////
	public static long byteToWord(byte btValue[])
	{
		return (long)(fixSignedByte(btValue[0]) + fixSignedByte(btValue[1]) * 256);
	}
	////////////////////////////////////////////////////////
	public static double byteToDword(byte btValue[])
	{
		return fixSignedByte(btValue[0]) + (fixSignedByte(btValue[1]) << 8 ) + (fixSignedByte(btValue[2]) << 16) + (fixSignedByte(btValue[3]) << 24);
	}
	////////////////////////////////////////////////////////
	public static byte[] wordToByte(long lValue)
	{
		byte[] btReturn = new byte[2];
		for(int i = 0; i < 2; i++)
		{
			btReturn[i] =(byte)(lValue % 256);
			lValue =(short)(lValue / 256);
		}
		return btReturn;
	}
	////////////////////////////////////////////////////////
	public static byte[] dwordToByte(double dblValue)
	{
		byte[] btReturn = new byte[4];
		for(int i = 0; i < 4; i++)
		{
			btReturn[i] =(byte)(dblValue % 256);
			dblValue = dblValue / 256;
		}
		return btReturn;
	}
	////////////////////////////////////////////////////////
	public static void fillArray(byte[] btArray,byte[] btValue,int iStartPosition,int iLength,byte btAlternateValue,int iAlign)
	{
		int iFillLength = btValue.length;
		if(iFillLength > iLength)
			iFillLength = iLength;
		int iAlternateLength = iLength - iFillLength;
		if(iAlign == ALIGN_RIGHT)
		{
			int iDataPosition = iStartPosition + iAlternateLength;
			Arrays.fill(btArray,iStartPosition,iDataPosition,btAlternateValue);
			System.arraycopy(btValue,0,btArray,iDataPosition,iFillLength);
		}
		else
		{
			int iAlternatePosition = iFillLength + iStartPosition;
			System.arraycopy(btValue,0,btArray,iStartPosition,iFillLength);
			Arrays.fill(btArray,iAlternatePosition,iAlternatePosition + iAlternateLength,btAlternateValue);
		}
	}
	////////////////////////////////////////////////////////
	public static void fillArray(byte[] btArray,byte btValue,int iStartPosition,int iLength)
	{
		for(int iIndex = 0;iIndex < iLength;iIndex++)
			btArray[iIndex + iStartPosition] = btValue;
	}
}
