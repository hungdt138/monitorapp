package com.fss.util;

import java.sql.*;
import java.util.*;

import com.fss.sql.*;

/**
 * <p>Title: DBTableLogUtil</p>
 * <p>Description: implements LogInterface,
 * using table database to store log data</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class DBTableLogUtil implements LogInterface
{
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	private Connection mcn;
	private String mstrIPAddress;
	private String mstrTableUser = "SEC_USER";
	private String mstrUserIdField = "USER_ID";
	private String mstrUserNameField = "USER_NAME";
	private String mstrTableModule = "SEC_MODULE";
	private String mstrModuleIdField = "MODULE_ID";
	private String mstrModuleNameField = "MODULE_NAME";
	private PreparedStatement mstmtInsertColumnLog = null;
	private PreparedStatement mstmtInsertTableLog = null;
	////////////////////////////////////////////////////////
	/**
	 * Set connection used to manipulate database
	 * @param cn connection to database
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void setConnection(Connection cn)
	{
		mcn = cn;
		mstmtInsertColumnLog = null;
		mstmtInsertTableLog = null;
	}
	////////////////////////////////////////////////////////
	/**
	 * Get log connection
	 * @return Connection
	 */
	////////////////////////////////////////////////////////
	public Connection getConnection()
	{
		return mcn;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strIPAddress String
	 */
	////////////////////////////////////////////////////////
	public void setIPAddress(String strIPAddress)
	{
		mstrIPAddress = strIPAddress;
	}
	////////////////////////////////////////////////////////
	/**
	 * Set user data table info
	 * @param strTableName User table name, default SEC_USER
	 * @param strIdField Name of user id field, default USER_ID
	 * @param strNameField Name of user name field, default USER_NAME
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void setTableUserData(String strTableName,String strIdField,String strNameField)
	{
		mstrTableUser = strTableName;
		mstrUserIdField = strIdField;
		mstrUserNameField = strNameField;
	}
	////////////////////////////////////////////////////////
	/**
	 * Set module data table info
	 * @param strTableName User module name, default SEC_MODULE
	 * @param strIdField Name of module id field, default MODULE_ID
	 * @param strNameField Name of module name field, default MODULE_NAME
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void setTableModuleData(String strTableName,String strIdField,String strNameField)
	{
		mstrTableModule = strTableName;
		mstrModuleIdField = strIdField;
		mstrModuleNameField = strNameField;
	}
	////////////////////////////////////////////////////////
	// Override function
	////////////////////////////////////////////////////////
	public String logHeader(String strModuleName,String strUserName,String strActionType) throws Exception
	{
		String strSQL = "";
		String strLogID = "";
		String strModuleID = "";
		String strUserID = "";
		PreparedStatement stmt = null;
		try
		{
			// Log id
			strLogID = Database.getSequenceValue(getConnection(),"MODULE_LOG_SEQ");

			// Get module id
			strSQL = "SELECT " + mstrModuleIdField + " FROM " + mstrTableModule + " WHERE UPPER(" + mstrModuleNameField + ") = UPPER(?)";
			stmt = getConnection().prepareStatement(strSQL);
			stmt.setString(1,strModuleName);
			ResultSet rs = stmt.executeQuery();
			if(!rs.next())
				throw new AppException("FSS-00017","DBTableLogUtil.logHeader");
			strModuleID = rs.getString(1);
			rs.close();
			stmt.close();

			// get user id
			strSQL = "SELECT " + mstrUserIdField + " FROM " + mstrTableUser + " WHERE UPPER(" + mstrUserNameField + ")=UPPER(?)";
			stmt = getConnection().prepareStatement(strSQL);
			stmt.setString(1,strUserName);
			rs = stmt.executeQuery();
			if(!rs.next())
				throw new AppException("FSS-00016","DBTableLogUtil.logHeader");
			strUserID = rs.getString(1);
			rs.close();
			stmt.close();

			// Insert into database
			strSQL = "INSERT INTO SEC_MODULE_LOG(LOG_ID, MODULE_ID, USER_ID, LOG_DATE, ACTION_TYPE, IP_ADDRESS) " +
					 "VALUES(?,?,?,SYSDATE,?,?)";
			stmt = getConnection().prepareStatement(strSQL);
			stmt.setString(1,strLogID);
			stmt.setString(2,strModuleID);
			stmt.setString(3,strUserID);
			stmt.setString(4,strActionType);
			stmt.setString(5,mstrIPAddress);
			stmt.executeUpdate();
		}
		finally
		{
			Database.closeObject(stmt);
		}
		return strLogID;
	}
	////////////////////////////////////////////////////////
	public Vector logBeforeUpdate(String strLogID,String strTableName,String strCondition) throws Exception
	{
		// Return value
		Vector vtReturn = new Vector();
		String strSQL = null;
		Statement stmt = null;
		try
		{
			// Select row id
			strSQL = "SELECT ROWID FROM " + strTableName;
			if(strCondition != null && strCondition.length() > 0)
				strSQL += " WHERE " + strCondition;
			stmt = getConnection().createStatement();
			ResultSet rsRowID = stmt.executeQuery(strSQL);
			while(rsRowID.next())
			{
				// get row id
				String strRowID = rsRowID.getString(1);

				// Log table change
				String strChangeID = logTableChange(strLogID,strTableName,strRowID,"Update");
				vtReturn.addElement(strChangeID);

				// Log old value
				logOldValue(strChangeID,strTableName,strRowID);
			}
			rsRowID.close();
		}
		finally
		{
			Database.closeObject(stmt);
		}

		// Return ChangeIDList
		return vtReturn;
	}
	////////////////////////////////////////////////////////
	public void logAfterUpdate(Vector vtChangeID) throws Exception
	{
		String strChangeID = null;
		for(int iIndex = 0;iIndex < vtChangeID.size();iIndex++)
		{
			// get row id
			strChangeID = vtChangeID.elementAt(iIndex).toString();

			// Log new value
			updateNewValue(getConnection(),strChangeID);

			// Clean duplicate value
			cleanLogValue(getConnection(),strChangeID);
		}
	}
	////////////////////////////////////////////////////////
	public void logAfterInsert(String strLogID,String strTableName,String strCondition) throws Exception
	{
		String strSQL = null;
		Statement stmt = null;
		String strChangeID = null;
		try
		{
			// Select row id
			strSQL = "SELECT ROWID FROM " + strTableName;
			if(strCondition != null && strCondition.length() > 0)
				strSQL += " WHERE " + strCondition;
			stmt = getConnection().createStatement();
			ResultSet rsRowID = stmt.executeQuery(strSQL);
			while(rsRowID.next())
			{
				// get row id
				String strRowID = rsRowID.getString(1);

				// Log table change
				strChangeID = logTableChange(strLogID,strTableName,strRowID,"Insert");

				// Log new value
				logNewValue(strChangeID,strTableName,strRowID);

				// Clean duplicate value
				cleanLogValue(getConnection(),strChangeID);
			}
			rsRowID.close();
		}
		finally
		{
			Database.closeObject(stmt);
		}
	}
	////////////////////////////////////////////////////////
	public void logBeforeDelete(String strLogID,String strTableName,String strCondition) throws Exception
	{
		String strSQL = null;
		Statement stmt = null;
		String strChangeID = null;
		try
		{
			// Select row id
			strSQL = "SELECT ROWID FROM " + strTableName;
			if(strCondition != null && strCondition.length() > 0)
				strSQL += " WHERE " + strCondition;
			stmt = getConnection().createStatement();
			ResultSet rsRowID = stmt.executeQuery(strSQL);
			while(rsRowID.next())
			{
				// get row id
				String strRowID = rsRowID.getString(1);

				// Log table change
				strChangeID = logTableChange(strLogID,strTableName,strRowID,"Delete");

				// Log new value
				logOldValue(strChangeID,strTableName,strRowID);

				// Clean duplicate value
				cleanLogValue(getConnection(),strChangeID);
			}
			rsRowID.close();
		}
		finally
		{
			Database.closeObject(stmt);
		}
	}
	////////////////////////////////////////////////////////
	public void logColumnChange(String strChangeID,String strColumnName,String strOldValue,String strNewValue) throws Exception
	{
		// Remove duplicate
		if(strOldValue == null || strNewValue == null)
		{
			if(strOldValue == null && strNewValue == null)
				return;
		}
		else if(strOldValue.equals(strNewValue))
			return;

		// Log column change
		initialInsertColumnLogStatement();
		mstmtInsertColumnLog.setString(1,strChangeID);
		mstmtInsertColumnLog.setString(2,strColumnName);
		mstmtInsertColumnLog.setString(3,strOldValue);
		mstmtInsertColumnLog.setString(4,strNewValue);
		mstmtInsertColumnLog.executeUpdate();
	}
	////////////////////////////////////////////////////////
	private void initialInsertColumnLogStatement() throws Exception
	{
		if(mstmtInsertColumnLog == null)
		{
			String strSQL = "INSERT INTO SEC_COLUMN_LOG(CHANGE_ID,COLUMN_NAME," +
							"OLD_VALUE,NEW_VALUE) VALUES(?,?,?,?)";
			mstmtInsertColumnLog = getConnection().prepareStatement(strSQL);
		}
	}
	////////////////////////////////////////////////////////
	public String logTableChange(String strLogID,String strTableName,String strRowID,String strActionType) throws Exception
	{
		// Change id
		String strChangeID = Database.getSequenceValue(getConnection(),"MODULE_LOG_SEQ");

		// Log table change
		if(mstmtInsertTableLog == null)
		{
			String strSQL = "INSERT INTO SEC_TABLE_LOG(CHANGE_ID,LOG_ID,TABLE_NAME," +
							"ROW_ID,ACTION_TYPE) VALUES(?,?,?,?,?)";
			mstmtInsertTableLog = getConnection().prepareStatement(strSQL);
		}
		mstmtInsertTableLog.setString(1,strChangeID);
		mstmtInsertTableLog.setString(2,strLogID);
		mstmtInsertTableLog.setString(3,strTableName);
		mstmtInsertTableLog.setString(4,strRowID);
		mstmtInsertTableLog.setString(5,strActionType);
		mstmtInsertTableLog.executeUpdate();
		return strChangeID;
	}
	////////////////////////////////////////////////////////
	private void logOldValue(String strChangeID,String strTableName,String strRowID) throws Exception
	{
		PreparedStatement stmtOldValue = null;
		try
		{
			// Select old values
			String strSQL = "SELECT * FROM " + strTableName + " WHERE ROWID=?";
			stmtOldValue = getConnection().prepareStatement(strSQL);
			stmtOldValue.setString(1,strRowID);
			ResultSet rsOldValue = stmtOldValue.executeQuery();
			if(!rsOldValue.next())
				throw new SQLException("Row with rowid='" + strRowID + " does not exist");
			ResultSetMetaData rsmOldValueInfo = rsOldValue.getMetaData();

			// Log column change
			initialInsertColumnLogStatement();
			mstmtInsertColumnLog.setString(1,strChangeID);
			mstmtInsertColumnLog.setString(4,"");
			int iColumnCount = rsmOldValueInfo.getColumnCount();
			for(int iColumnIndex = 1;iColumnIndex <= iColumnCount;iColumnIndex++)
			{
				mstmtInsertColumnLog.setString(2,rsmOldValueInfo.getColumnName(iColumnIndex));
				if (!rsmOldValueInfo.getColumnTypeName(iColumnIndex).equals("LONG") )
					mstmtInsertColumnLog.setString(3,rsOldValue.getString(iColumnIndex));
				else
					mstmtInsertColumnLog.setString(3,"LONG");
				mstmtInsertColumnLog.executeUpdate();
			}

			// Release
			rsOldValue.close();
		}
		finally
		{
			Database.closeObject(stmtOldValue);
		}
	}
	////////////////////////////////////////////////////////
	private void logNewValue(String strChangeID,String strTableName,String strRowID) throws Exception
	{
		String strSQL = null;
		PreparedStatement stmtNewValue = null;
		try
		{
			// Select new values
			strSQL = "SELECT * FROM " + strTableName + " WHERE ROWID=?";
			stmtNewValue = getConnection().prepareStatement(strSQL);
			stmtNewValue.setString(1,strRowID);
			ResultSet rsNewValue = stmtNewValue.executeQuery();
			if(!rsNewValue.next()) throw new SQLException("Row with rowid='" + strRowID + " does not exist");
			ResultSetMetaData rsmNewValueInfo = rsNewValue.getMetaData();

			// Log column change
			initialInsertColumnLogStatement();
			mstmtInsertColumnLog.setString(1,strChangeID);
			mstmtInsertColumnLog.setString(3,"");
			int iColumnCount = rsmNewValueInfo.getColumnCount();
			for(int iColumnIndex = 1;iColumnIndex <= iColumnCount;iColumnIndex++)
			{
				mstmtInsertColumnLog.setString(2,rsmNewValueInfo.getColumnName(iColumnIndex));
				if (!rsmNewValueInfo.getColumnTypeName(iColumnIndex).equals("LONG"))
					mstmtInsertColumnLog.setString(4,rsNewValue.getString(iColumnIndex));
				else
					mstmtInsertColumnLog.setString(4,"LONG");
				mstmtInsertColumnLog.executeUpdate();
			}

			// Release
			rsNewValue.close();
		}
		finally
		{
			Database.closeObject(stmtNewValue);
		}
	}
	////////////////////////////////////////////////////////
	private static void updateNewValue(Connection cn,String strChangeID) throws Exception
	{
		String strSQL = null;
		PreparedStatement stmtTableName = null;
		PreparedStatement stmtNewValue = null;
		PreparedStatement stmtLogColumn = null;
		try
		{
			// Select table name and row id
			strSQL = "SELECT TABLE_NAME,ROW_ID FROM SEC_TABLE_LOG WHERE CHANGE_ID=?";
			stmtTableName = cn.prepareStatement(strSQL);
			stmtTableName.setString(1,strChangeID);
			ResultSet rsTableName = stmtTableName.executeQuery();
			if(!rsTableName.next()) throw new SQLException("Table change log with change_id='" + strChangeID + " does not exist");
			String strTableName = rsTableName.getString(1);
			String strRowID = rsTableName.getString(2);
			rsTableName.close();

			// Select new values
			strSQL = "SELECT * FROM " + strTableName + " WHERE ROWID=?";
			stmtNewValue = cn.prepareStatement(strSQL);
			stmtNewValue.setString(1,strRowID);
			ResultSet rsNewValue = stmtNewValue.executeQuery();
			if(!rsNewValue.next()) throw new SQLException("Row with rowid='" + strRowID + " does not exist");
			ResultSetMetaData rsmNewValueInfo = rsNewValue.getMetaData();

			// Log column change
			strSQL = "UPDATE SEC_COLUMN_LOG SET NEW_VALUE=? WHERE CHANGE_ID=? AND COLUMN_NAME=?";
			stmtLogColumn = cn.prepareStatement(strSQL);
			stmtLogColumn.setString(2,strChangeID);
			int iColumnCount = rsmNewValueInfo.getColumnCount();
			for(int iColumnIndex = 1;iColumnIndex <= iColumnCount;iColumnIndex++)
			{
				stmtLogColumn.setString(3,rsmNewValueInfo.getColumnName(iColumnIndex));
				if (!rsmNewValueInfo.getColumnTypeName(iColumnIndex).equals("LONG"))
					stmtLogColumn.setString(1,rsNewValue.getString(iColumnIndex));
				else
					stmtLogColumn.setString(1,"LONG");
				stmtLogColumn.executeUpdate();
			}

			// Release
			rsNewValue.close();
		}
		finally
		{
			Database.closeObject(stmtTableName);
			Database.closeObject(stmtNewValue);
			Database.closeObject(stmtLogColumn);
		}
	}
	////////////////////////////////////////////////////////
	private static void cleanLogValue(Connection cn,String strChangeID) throws Exception
	{
		String strSQL = null;
		Statement stmt = null;
		try
		{
			// Delete duplicate value
			stmt = cn.createStatement();
			strSQL = "DELETE SEC_COLUMN_LOG WHERE CHANGE_ID=" + strChangeID;
			strSQL += " AND (OLD_VALUE = NEW_VALUE OR (OLD_VALUE IS NULL AND NEW_VALUE IS NULL))";
			stmt.executeUpdate(strSQL);

			// Delete empty table change
			strSQL = "DELETE SEC_TABLE_LOG WHERE CHANGE_ID=" + strChangeID +
					 " AND NOT EXISTS (SELECT * FROM SEC_COLUMN_LOG SCL" +
					 " WHERE SCL.CHANGE_ID=" + strChangeID + ")";
			stmt.executeUpdate(strSQL);
		}
		finally
		{
			Database.closeObject(stmt);
		}
	}
}
