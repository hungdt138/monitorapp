package com.fss.sql;

import java.sql.*;
import java.util.*;
import com.fss.util.*;

/**
 * <p>Title: </p>
 * <p>Description: Database class includes group of
 *  function using with database</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Nguyen Truong Giang
 * @version 1.0
 */

public class Database
{
	/////////////////////////////////////////////////////////////////
	/**
	 *
	 * @param cn Connection
	 * @param strSQL String
	 * @return Vector
	 * @throws Exception
	 */
	/////////////////////////////////////////////////////////////////
	public static Vector executeQuery(Connection cn,String strSQL) throws Exception
	{
		Statement stmt = cn.createStatement();
		ResultSet rs = stmt.executeQuery(strSQL);
		Vector vt = Database.convertToVector(rs);
		rs.close();
		stmt.close();
		return vt;
	}
	/////////////////////////////////////////////////////////////////
	/**
	 * Convert data from ResultSet into Vector
	 * @param rsData ResultSet
	 * @return Vector
	 * @throws Exception
	 * @author Thai Hoang Hiep - Date: 22/02/2004
	 */
	/////////////////////////////////////////////////////////////////
	public static Vector convertToVector(ResultSet rsData) throws Exception
	{
		Vector vctReturn = new Vector();
		int iColumnCount = rsData.getMetaData().getColumnCount();
		while(rsData.next())
		{
			Vector vctRow = new Vector();
			for(int i = 1;i <= iColumnCount;i++)
			{
				String strValue = rsData.getString(i);
				if(strValue == null)
					strValue = "";
				vctRow.addElement(strValue);
			}
			vctReturn.addElement(vctRow);
		}
		vctReturn.trimToSize();
		return vctReturn;
	}
	/////////////////////////////////////////////////////////////////
	/**
	 * Creates connection to oracle database
	 * @param strUrl URL to database
	 * @param strUserName Database login name
	 * @param strPassword Database login password
	 * @throws AppException when creating connection
	 * @author Nguyen Truong Giang - Date: 22/02/2004
	 * @return Connection
	 */
	/////////////////////////////////////////////////////////////////
	public static Connection getConnection(String strUrl,String strUserName,String strPassword) throws AppException
	{
		return getConnection("oracle.jdbc.driver.OracleDriver",strUrl,strUserName,strPassword);
	}
	/////////////////////////////////////////////////////////////////
	/**
	 * Creates connection to database
	 * @param strDriver driver used to connect database
	 * @param strUrl URL to database
	 * @param strUserName Database login name
	 * @param strPassword Database login password
	 * @throws AppException creating connection
	 * @return Connection
	 * @author Thai Hoang Hiep - Date: 25/02/2004
	 */
	/////////////////////////////////////////////////////////////////
	public static Connection getConnection(String strDriver,String strUrl,String strUserName,String strPassword) throws AppException
	{
		try
		{
			Properties prtConnect = new Properties();
			prtConnect.setProperty("user",strUserName);
			prtConnect.setProperty("password",strPassword);
			Driver drv = (Driver)Class.forName(strDriver).newInstance();
			return drv.connect(strUrl,prtConnect);
		}
		catch(Exception e)
		{
			throw new AppException(e,"Database.getConnection");
		}
	}
	/////////////////////////////////////////////////////////////////
	/**
	 * Execute query and get first value from database
	 * @param cn opened connection
	 * @param strTableName table to query
	 * @param strFieldName field need to get value
	 * @param strCondition condition
	 * @throws Exception
	 * @return String
	 * @author Thai Hoang Hiep
	 */
	/////////////////////////////////////////////////////////////////
	public static String getValue(Connection cn,String strTableName,String strFieldName,String strCondition) throws Exception
	{
		// Get query data
		Statement stmt = cn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT " + strFieldName + " FROM " + strTableName + " WHERE " + strCondition);

		// Validation
		if(!rs.next())
		{
			rs.close();
			stmt.close();
			throw new AppException("FSS-00009","Database.getValue",strTableName + "." + strFieldName);
		}

		String strReturn = rs.getString(1);
		rs.close();
		stmt.close();

		return strReturn;
	}
	/////////////////////////////////////////////////////////////////
	/**
	 * Get sequence value from database
	 * @param cn opened connection
	 * @param sequenceName Name of sequence
	 * @throws Exception
	 * @return String
	 * @author Thai Hoang Hiep - Date: 22/02/2004
	 */
	/////////////////////////////////////////////////////////////////
	public static String getSequenceValue(Connection cn,String sequenceName) throws Exception
	{
		return getSequenceValue(cn,sequenceName,true);
	}
	/////////////////////////////////////////////////////////////////
	public static String getSequenceValue(Connection cn,String sequenceName,boolean bAutoCreate) throws Exception
	{
		// SQL command to sequence value
		String strSQL = "SELECT " + sequenceName + ".NEXTVAL FROM DUAL";

		// Get query data
		try
		{
			Statement stmt = cn.createStatement();
			ResultSet rs = stmt.executeQuery(strSQL);
			rs.next();
			String strReturn = rs.getString(1);
			rs.close();
			stmt.close();
			return strReturn;
		}
		catch(Exception e)
		{
			if(e.getMessage() != null &&
			   e.getMessage().startsWith("ORA-02289"))
			{
				if(!bAutoCreate)
					throw new AppException("FSS-00018","Database.getSequenceValue",sequenceName);
				else
				{
					Statement stmt = cn.createStatement();
					stmt.executeUpdate("CREATE SEQUENCE " + sequenceName + " START WITH 2");
					stmt.close();
					return "1";
				}
			}
			else
				throw e;
		}
	}
	/////////////////////////////////////////////////////////////////
	/**
	 * Close objects which are used to interact with database
	 * @param obj Object need to be closed such as Statment, PrepareStatment, Connection, ResuletSet, CallableStatment, BatchStatement
	 * @author Nguyen Truong Giang - Date: 22/02/2004
	 */
	/////////////////////////////////////////////////////////////////
	public static void closeObject(CallableStatement obj)
	{
		try
		{
			if(obj != null)
				obj.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/////////////////////////////////////////////////////////////////
	/**
	 * Close objects which are used to interact with database
	 * @param obj Object need to be closed such as Statment, PrepareStatment, Connection, ResuletSet, CallableStatment, BatchStatement
	 * @author Nguyen Truong Giang - Date: 22/02/2004
	 */
	/////////////////////////////////////////////////////////////////
	public static void closeObject(Statement obj)
	{
		try
		{
			if(obj != null)
				obj.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/////////////////////////////////////////////////////////////////
	/**
	 * Close objects which are used to interact with database
	 * @param obj Object need to be closed such as Statment, PrepareStatment, Connection, ResuletSet, CallableStatment, BatchStatement
	 * @author Nguyen Truong Giang - Date: 22/02/2004
	 */
	/////////////////////////////////////////////////////////////////
	public static void closeObject(ResultSet obj)
	{
		try
		{
			if(obj != null)
				obj.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/////////////////////////////////////////////////////////////////
	/**
	 * Close objects which are used to interact with database
	 * @param obj Object need to be closed such as Statment, PrepareStatment, Connection, ResuletSet, CallableStatment, BatchStatement
	 * @author Nguyen Truong Giang - Date: 22/02/2004
	 */
	/////////////////////////////////////////////////////////////////
	public static void closeObject(BatchStatement obj)
	{
		try
		{
			if(obj != null)
				obj.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/////////////////////////////////////////////////////////////////
	/**
	 * Close objects which are used to interact with database
	 * @param obj Object need to be closed such as Statment, PrepareStatment, Connection, ResuletSet, CallableStatment, BatchStatement
	 * @author Nguyen Truong Giang - Date: 22/02/2004
	 */
	/////////////////////////////////////////////////////////////////
	public static void closeObject(Connection obj)
	{
		try
		{
			if(obj != null)
			{
				if(!obj.isClosed())
				{
					if(!obj.getAutoCommit())
						obj.rollback();
					obj.close();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
