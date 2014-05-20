package com.fss.sql;

import java.sql.*;
import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: BatchStatement is used to
 * insert or update multi sql statment</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Nguyen Truong Giang
 * @version 1.0
 */

public class BatchStatement
{
	private Connection cn = null;
	private PreparedStatement pstmt = null;
	private Vector vtData = new Vector();
	private Vector vtError = new Vector();
	private Vector vtRow = new Vector();
	private String strSQL = "";
	int rowError = 0;
	/////////////////////////////////////////////////////////////////
	/** Creates new BatchStatment instance
	  * @param cnDB Active connection
	  */
	/////////////////////////////////////////////////////////////////
	public BatchStatement(Connection cnDB)
	{
		cn = cnDB;
	}
	/////////////////////////////////////////////////////////////////
	/** Creates new BatchStatement instance
	  * @param cnDB Active connection
	  * @param prepareSql SQL command
	  * @throws SQLException when preparing sql statment
	  * @author Nguyen Truong Giang - Date: 22/02/2004
	  */
	/////////////////////////////////////////////////////////////////
	public BatchStatement(Connection cnDB, String prepareSql) throws SQLException
	{
		cn = cnDB;
		strSQL = prepareSql;
		pstmt = cn.prepareStatement(strSQL);
	}
	/////////////////////////////////////////////////////////////////
	/** Reset error counter
	  * @author Nguyen Truong Giang - Date: 22/02/2004
	  */
	/////////////////////////////////////////////////////////////////
	public void resetCounter()
	{
		rowError = 0;
	}
	/////////////////////////////////////////////////////////////////
	/** Get number of error
	  * @return Number of error when executing SQL statment
	  * @author Nguyen Truong Giang - Date: 22/02/2004
	  */
	/////////////////////////////////////////////////////////////////
	public int getErrorCount()
	{
		return rowError;
	}
	/////////////////////////////////////////////////////////////////
	/** Preparing SQL statment
	  * @param prepareSql SQL command
	  * @throws SQLException when preparing sql statment
	  * @author Nguyen Truong Giang - Date: 22/02/2004
	  */
	/////////////////////////////////////////////////////////////////
	public void prepareStatement(String prepareSql) throws SQLException
	{
		strSQL = prepareSql;
		pstmt = cn.prepareStatement(strSQL);
	}
	/////////////////////////////////////////////////////////////////
	/** Add SQL statment into batch
	  * @throws SQLException when adding into batch
	  * @author Nguyen Truong Giang - Date: 22/02/2004
	  */
	/////////////////////////////////////////////////////////////////
	public void addBatch() throws SQLException
	{
		pstmt.addBatch();
		vtRow.trimToSize();
		vtData.addElement(vtRow);
		vtRow = new Vector();
	}
	/////////////////////////////////////////////////////////////////
	/** Executes batch of statment
	  * @throws Exception when executing batch
	  * @author Nguyen Truong Giang - Date: 22/02/2004
	  */
	/////////////////////////////////////////////////////////////////
	public Vector executeBatch() throws Exception
	{
		boolean bFirstTime = true;
		int updatedCount = 0;
		String strError = null;
		clearError();

		if(strSQL == null || strSQL.length() == 0)
		   throw new Exception("No SQL statement found");

		while(vtData != null && vtData.size() > 0)
		{
			//neu ko phai lan dau tien ti lay gia tri va addBatch lai
			for(int i = 0; !bFirstTime && i < vtData.size(); i++)
			{
				vtRow = (Vector)vtData.elementAt(i);
				for(int j = 0; j < vtRow.size(); j ++)
				{
					BatchField batchField = (BatchField)vtRow.elementAt(j);
					if(batchField.getFieldType() == BatchField.TYPE_STRING) //string
					{
						pstmt.setString(batchField.getFieldIndex(), (String)batchField.getFieldValue());
					}
					else if(batchField.getFieldType() == BatchField.TYPE_INTEGER) //int
					{
						pstmt.setInt(batchField.getFieldIndex(),Integer.parseInt((String)batchField.getFieldValue()));
					}
					else if(batchField.getFieldType() == BatchField.TYPE_LONG) //long
					{
						pstmt.setLong(batchField.getFieldIndex(), Long.parseLong((String)batchField.getFieldValue()));
					}
					else if(batchField.getFieldType() == BatchField.TYPE_DOUBLE) //double
					{
						pstmt.setDouble(batchField.getFieldIndex(), Double.parseDouble((String)batchField.getFieldValue()));
					}
					else if(batchField.getFieldType() == BatchField.TYPE_FLOAT) //float
					{
						pstmt.setFloat(batchField.getFieldIndex(), Float.parseFloat((String)batchField.getFieldValue()));
					}
					else if(batchField.getFieldType() == BatchField.TYPE_DATE) //date
					{
						pstmt.setDate(batchField.getFieldIndex(), (java.sql.Date)batchField.getFieldValue());
					}
				}
				pstmt.addBatch();
			}
			try
			{
				pstmt.executeBatch();
			}
			catch(BatchUpdateException e)
			{
				strError = e.getMessage();
			}
			catch(SQLException e)
			{
				strError = e.getMessage();
			}
			bFirstTime = false;
			updatedCount = pstmt.getUpdateCount();
			if(strError != null)
			{
				rowError += updatedCount + 1;
				vtError.addElement(new BatchError(rowError,strError,vtData.elementAt(updatedCount)));
				strError = null;
			}
			if(updatedCount == vtData.size())
			{
				vtData.clear();
			}
			else
			{
				for(int i = 0;i <= updatedCount; i++)
				{
					vtData.removeElementAt(0);
				}
			}
			vtData.trimToSize();
		}
		return vtError;
	}
	/////////////////////////////////////////////////////////////////
	/** Close and release object
	  * @author Nguyen Truong Giang - Date: 22/02/2004
	  */
	/////////////////////////////////////////////////////////////////
	public void close()
	{
		Database.closeObject(pstmt);
		try
		{
			vtData.clear();
			vtError.clear();
			vtRow.clear();
		}
		catch(Exception e){}
	}
	/////////////////////////////////////////////////////////////////
	/** Clear error list
	  * @author Nguyen Truong Giang - Date: 22/02/2004
	  */
	/////////////////////////////////////////////////////////////////
	public void clearError()
	{
		vtError.clear();
	}
	/////////////////////////////////////////////////////////////////
	/** Bind string value into sql statment
	  * @param fieldIndex Index of field
	  * @param fieldValue Value of field
	  * @throws SQLException when binding value
	  * @author Nguyen Truong Giang - Date: 22/02/2004
	  */
	/////////////////////////////////////////////////////////////////
	public void setString(int fieldIndex, String fieldValue) throws SQLException
	{
		pstmt.setString(fieldIndex,fieldValue);
		vtRow.addElement(new BatchField(BatchField.TYPE_STRING,fieldIndex,fieldValue));
	}
	/////////////////////////////////////////////////////////////////
	/** Bind integer value into sql statment
	  * @param fieldIndex Index of field
	  * @param fieldValue Value of field
	  * @throws SQLException when binding value
	  * @author Nguyen Truong Giang - Date: 22/02/2004
	  */
	/////////////////////////////////////////////////////////////////
	public void setInt(int fieldIndex, int fieldValue) throws SQLException
	{
		pstmt.setInt(fieldIndex,fieldValue);
		vtRow.addElement(new BatchField(BatchField.TYPE_INTEGER,fieldIndex,fieldValue));
	}
	/////////////////////////////////////////////////////////////////
	/** Bind float value into sql statment
	  * @param fieldIndex Index of field
	  * @param fieldValue Value of field
	  * @throws SQLException when binding value
	  * @author Nguyen Truong Giang - Date: 22/02/2004
	  */
	/////////////////////////////////////////////////////////////////
	public void setFloat(int fieldIndex, float fieldValue) throws SQLException
	{
		pstmt.setFloat(fieldIndex,fieldValue);
		vtRow.addElement(new BatchField(BatchField.TYPE_FLOAT,fieldIndex,fieldValue));
	}
	/////////////////////////////////////////////////////////////////
	/** Bind double value into sql statment
	  * @param fieldIndex Index of field
	  * @param fieldValue Value of field
	  * @throws SQLException when binding value
	  * @author Nguyen Truong Giang - Date: 22/02/2004
	  */
	/////////////////////////////////////////////////////////////////
	public void setDouble(int fieldIndex, double fieldValue) throws SQLException
	{
		pstmt.setDouble(fieldIndex,fieldValue);
		vtRow.addElement(new BatchField(BatchField.TYPE_DOUBLE,fieldIndex,fieldValue));
	}
	/////////////////////////////////////////////////////////////////
	/** Bind date value into sql statment
	  * @param fieldIndex Index of field
	  * @param fieldValue Value of field
	  * @throws SQLException when binding value
	  * @author Nguyen Truong Giang - Date: 22/02/2004
	  */
	/////////////////////////////////////////////////////////////////
	public void setDate(int fieldIndex, java.sql.Date fieldValue) throws SQLException
	{
		pstmt.setDate(fieldIndex,fieldValue);
		vtRow.addElement(new BatchField(BatchField.TYPE_DATE,fieldIndex,fieldValue));
	}
	/////////////////////////////////////////////////////////////////
	/** Bind long value into sql statment
	  * @param fieldIndex Index of field
	  * @param fieldValue Value of field
	  * @throws SQLException when binding value
	  * @author Nguyen Truong Giang - Date: 22/02/2004
	  */
	/////////////////////////////////////////////////////////////////
	public void setLong(int fieldIndex, long fieldValue) throws SQLException
	{
		pstmt.setLong(fieldIndex,fieldValue);
		vtRow.addElement(new BatchField(BatchField.TYPE_LONG,fieldIndex,fieldValue));
	}
}
