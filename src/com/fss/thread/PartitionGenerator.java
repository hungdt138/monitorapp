package com.fss.thread;

import java.util.*;
import java.sql.*;
import java.io.*;

import com.fss.sql.*;
import com.fss.util.*;
import java.text.SimpleDateFormat;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 * Notes:
 */

public class PartitionGenerator extends ManageableThread
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	protected String mstrDBUrl;
	protected String mstrDBUserName;
	protected String mstrDBPassword;
	protected String mstrStorageDir;
	protected String mstrStorageStyle;
	protected int miExtendedSize;
	protected int miRemainFileSize;
	protected int miReservedSize;
	protected String mstrReservedDate;
	protected Vector mvtPartitionSetting;
	////////////////////////////////////////////////////////
	public Vector getParameterDefinition()
	{
		Vector vtReturn = new Vector();

		vtReturn.addElement(createParameterDefinition("DBUrl","",ParameterType.PARAM_TEXTBOX_MAX,"256","Connection url of database"));
		vtReturn.addElement(createParameterDefinition("DBUserName","",ParameterType.PARAM_TEXTBOX_MAX,"256","DBA user name"));
		vtReturn.addElement(createParameterDefinition("DBPassword","",ParameterType.PARAM_PASSWORD,"100","Password of DBA user name"));
		vtReturn.addElement(createParameterDefinition("StorageDir","",ParameterType.PARAM_TEXTBOX_MAX,"256","Path to directory contain data file (usually is root dir)"));
		Vector vtValue = new Vector();
		vtValue.addElement("Directly");
		vtValue.addElement("Daily");
		vtValue.addElement("Monthly");
		vtValue.addElement("Yearly");
		vtReturn.addElement(createParameterDefinition("StorageStyle","",ParameterType.PARAM_COMBOBOX,vtValue,""));
		Vector vtDefinition = new Vector();
		vtDefinition.addElement(createParameterDefinition("TableName","",ParameterType.PARAM_TEXTBOX_MAX,"256","Table to generate partition","0"));
		vtValue = new Vector();
		vtValue.addElement("Hourly");
		vtValue.addElement("Daily");
		vtValue.addElement("Monthly");
		vtValue.addElement("Yearly");
		vtDefinition.addElement(createParameterDefinition("PartitionType","",ParameterType.PARAM_COMBOBOX,vtValue,"","1"));
		vtDefinition.addElement(createParameterDefinition("PartitionFormat","",ParameterType.PARAM_TEXTBOX_MAX,"256","Format of partition name, can use $Date, $TablespaceName, $TableName as parameter","2"));
		vtDefinition.addElement(createParameterDefinition("PartitionDateFormat","",ParameterType.PARAM_TEXTBOX_MAX,"256","Format of $Date for partition name","3"));
		vtDefinition.addElement(createParameterDefinition("TablespaceFormat","",ParameterType.PARAM_TEXTBOX_MAX,"256","Format of tablespace name, can use $Date, $TableName as parameter","4"));
		vtDefinition.addElement(createParameterDefinition("TablespaceDateFormat","",ParameterType.PARAM_TEXTBOX_MAX,"256","Format of $Date for tablespace name","5"));
		vtDefinition.addElement(createParameterDefinition("TablespaceFirstDatePos","",ParameterType.PARAM_TEXTBOX_MASK,"99","First position of $Date in tablespace name","6"));
		vtDefinition.addElement(createParameterDefinition("TablespaceLastDatePos","",ParameterType.PARAM_TEXTBOX_MASK,"99","Last position of $Date in tablespace name","7"));
		vtReturn.addElement(createParameterDefinition("PartitionSetting","",ParameterType.PARAM_TABLE,vtDefinition,"Contain partition information"));
		vtReturn.addElement(createParameterDefinition("ExtendedSize","",ParameterType.PARAM_TEXTBOX_MASK,"9990","Size of extending data file (in MB)"));
		vtReturn.addElement(createParameterDefinition("RemainFileSize","",ParameterType.PARAM_TEXTBOX_MASK,"9990","Remain size of each data file (In MB)"));
		vtReturn.addElement(createParameterDefinition("ReservedSize","",ParameterType.PARAM_TEXTBOX_MASK,"9990","If size of a table space less than this value (in MB), it will be extended automatically"));
		vtReturn.addElement(createParameterDefinition("ReservedDate","",ParameterType.PARAM_TEXTBOX_MASK,"990","Number of day partition will be automatically generated before the partition date"));
		vtReturn.addAll(super.getParameterDefinition());

		for(int iIndex = vtReturn.size() - 1;iIndex >= 0;iIndex--)
		{
			String strParameterName = (String)((Vector)vtReturn.elementAt(iIndex)).elementAt(0);
			if(strParameterName.equals("ConnectDB"))
				vtReturn.removeElementAt(iIndex);
		}

		return vtReturn;
	}
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public void fillParameter() throws AppException
	{
		mstrDBUrl = loadMandatory("DBUrl");
		mstrDBUserName = loadMandatory("DBUserName");
		mstrDBPassword = loadMandatory("DBPassword");
		mstrStorageDir = loadDirectory("StorageDir",false,true);
		mstrStorageStyle = StringUtil.nvl(mprtParam.get("StorageStyle"),"");
		miExtendedSize = loadUnsignedInteger("ExtendedSize");
		miRemainFileSize = 0;
		if(StringUtil.nvl(mprtParam.get("RemainFileSize"),"").length() > 0)
			miRemainFileSize = loadUnsignedInteger("RemainFileSize");
		miReservedSize = loadUnsignedInteger("ReservedSize");
		mstrReservedDate = loadMandatory("ReservedDate");
		Object obj = mprtParam.get("PartitionSetting");
		if(obj != null && obj instanceof Vector)
			mvtPartitionSetting = (Vector)obj;
		else
			mvtPartitionSetting = new Vector();
		if(mvtPartitionSetting == null)
			mvtPartitionSetting = new Vector();
		super.fillParameter();
		mbAutoConnectDB = false;
	}
	////////////////////////////////////////////////////////
	public void validateParameter() throws Exception
	{
		super.validateParameter();
		if(mvtPartitionSetting == null)
			return;
		Vector vtUnique = new Vector();
		for(int iIndex = 0;iIndex < mvtPartitionSetting.size();iIndex++)
		{
			Vector vtRow = (Vector)mvtPartitionSetting.elementAt(iIndex);
			if(vtUnique.indexOf((String)vtRow.elementAt(0)) >= 0)
				throw new AppException("FSS-00010","PartitionManager.validateParameter","PartitionSetting.TableName");
			vtUnique.addElement((String)vtRow.elementAt(0));
			loadMandatory("PartitionSetting.TableName",(String)vtRow.elementAt(0));
			loadMandatory("PartitionSetting.PartitionType",(String)vtRow.elementAt(1));
			loadMandatory("PartitionSetting.PartitionFormat",(String)vtRow.elementAt(2));
			loadMandatory("PartitionSetting.PartitionDateFormat",(String)vtRow.elementAt(3));
			loadMandatory("PartitionSetting.TablespaceFormat",(String)vtRow.elementAt(4));
			loadMandatory("PartitionSetting.TablespaceDateFormat",(String)vtRow.elementAt(5));
			int iTablespaceFirstDatePos = loadUnsignedInteger("PartitionSetting.TablespaceFirstDatePos",(String)vtRow.elementAt(6));
			int iTablespaceLastDatePos = loadUnsignedInteger("PartitionSetting.TablespaceLastDatePos",(String)vtRow.elementAt(7));
			if(iTablespaceLastDatePos <= iTablespaceFirstDatePos)
				throw new AppException("FSS-00003","PartitionManager.validateParameter","PartitionSetting.TablespaceLastDatePos,PartitionSetting.TablespaceFirstDatePos");
		}
	}
	////////////////////////////////////////////////////////
	public void processSession() throws java.lang.Exception
	{
		String strSQL = "";
		Connection cn = null;
		try
		{
			// Create database connection
			cn = Database.getConnection(mstrDBUrl,mstrDBUserName,mstrDBPassword);
			Statement stmt = cn.createStatement();
			for(int iIndex = 0;iIndex < mvtPartitionSetting.size();iIndex++)
			{
				// Get table name
				Vector vtPartitionInfo = (Vector)mvtPartitionSetting.elementAt(iIndex);
				String strTableName = (String)vtPartitionInfo.elementAt(0);
				String strPartitionType = (String)vtPartitionInfo.elementAt(1);
				String strPartitionFormat = (String)vtPartitionInfo.elementAt(2);
				String strPartitionDateFormat = (String)vtPartitionInfo.elementAt(3);
				String strTablespaceFormat = (String)vtPartitionInfo.elementAt(4);
				String strTablespaceDateFormat = (String)vtPartitionInfo.elementAt(5);
				int iTablespaceFirstDatePos = Integer.parseInt((String)vtPartitionInfo.elementAt(6));
				int iTablespaceLastDatePos = Integer.parseInt((String)vtPartitionInfo.elementAt(7));

				// Check table space size
				strSQL = "SELECT TS.TABLESPACE_NAME,SUM(NVL(FS.BYTES,0))" +
						 " FROM USER_TABLESPACES TS,USER_FREE_SPACE FS" +
						 " WHERE TS.TABLESPACE_NAME=FS.TABLESPACE_NAME(+)" +
						 " AND STATUS NOT IN ('OFFLINE','READ ONLY')" +
						 " GROUP BY TS.TABLESPACE_NAME" +
						 " HAVING SUM(NVL(FS.BYTES,0)) < (1024*1024*" + String.valueOf(miReservedSize) +
						 " + 1024*1024*(SELECT COUNT(*) FROM GV$DATAFILE,GV$TABLESPACE" +
						 " WHERE GV$DATAFILE.TS#=GV$TABLESPACE.TS#" +
						 " AND GV$TABLESPACE.NAME=TS.TABLESPACE_NAME)*" + String.valueOf(miRemainFileSize) + ")" +
						 " AND TS.TABLESPACE_NAME IN (SELECT DISTINCT TABLESPACE_NAME" +
						 " FROM USER_TAB_PARTITIONS WHERE UPPER(TABLE_NAME)=UPPER('" + strTableName + "'))";
				ResultSet rs = stmt.executeQuery(strSQL);
				Vector vtTableSpace = new Vector();
				while(rs.next())
					vtTableSpace.addElement(rs.getString(1));

				// Extends tablespace if limit
				for(int iTableSpaceIndex = 0;iTableSpaceIndex < vtTableSpace.size();iTableSpaceIndex++)
				{
					// Get tablespacedate
					String strTableSpace = (String)vtTableSpace.elementAt(iTableSpaceIndex);
					java.util.Date dtTableSpace = null;
					try
					{
						SimpleDateFormat fmt = new SimpleDateFormat(strTablespaceDateFormat);
						fmt.setLenient(false);
						dtTableSpace = fmt.parse(strTableSpace.substring(iTablespaceFirstDatePos,iTablespaceLastDatePos));
					}
					catch(Exception e)
					{
						throw new AppException("Could not get tablespace date from tablespace name, please ensure that parameter TablespaceFirstDatePos, TablespaceLastDatePos and TablespaceDateFormat are correct. Error is " + e.getMessage());
					}

					// Get child folder date
					String strCurrentDate = "";
					if(mstrStorageStyle.equals("Daily"))
						strCurrentDate = StringUtil.format(dtTableSpace,"yyyyMMdd") + "/";
					else if(mstrStorageStyle.equals("Monthly"))
						strCurrentDate = StringUtil.format(dtTableSpace,"yyyyMM") + "/";
					else if(mstrStorageStyle.equals("Yearly"))
						strCurrentDate = StringUtil.format(dtTableSpace,"yyyy") + "/";
					String strDataFile = mstrStorageDir + strCurrentDate + strTableSpace + "_" + getDataFileIndex(cn,strTableSpace,"0000");
					FileUtil.forceFolderExist(mstrStorageDir + strCurrentDate);

					// Create table space
					strSQL = "ALTER TABLESPACE " + strTableSpace + " ADD DATAFILE '" +
							 strDataFile + ".dbf' SIZE " + String.valueOf(miExtendedSize) + "M";
					logMonitor("Start extending tablespace " + strTableSpace);
					stmt.executeUpdate(strSQL);
					logMonitor("Extending tablespace " + strTableSpace + " completed.\r\n\t" +
							   "Data file " + strDataFile + " was created.");
				}
				rs.close();

				// Get last partition high value
				strSQL = "SELECT HIGH_VALUE FROM USER_TAB_PARTITIONS TP" +
						 " WHERE PARTITION_POSITION=(SELECT MAX(PARTITION_POSITION)" +
						 " FROM USER_TAB_PARTITIONS TP1 WHERE TP1.TABLE_NAME=TP.TABLE_NAME)" +
						 " AND UPPER(TP.TABLE_NAME)=UPPER('" + strTableName + "')";
				rs = stmt.executeQuery(strSQL);
				String strHighValue = "";
				if(rs.next())
					strHighValue = rs.getString(1);
				rs.close();

				// Bang khong phai partition
				if(strHighValue.length() == 0)
					logMonitor("Table '" + strTableName + "' isn't a partition table");
				else
				{
					// Create partition for table
					java.util.Date dtTableSpace = null;
					while(strHighValue.length() > 0)
					{
						// Get next partition high value
						if(strPartitionType.equals("Hourly"))
							strSQL = "SELECT 'TO_DATE(' || TO_CHAR(" + strHighValue + " + 1/24,'YYYYMMDDHH24') || ',''YYYYMMDDHH24'')'," +
									 "TO_CHAR(" + strHighValue + ",'YYYYMMDDHH24MISS') FROM DUAL WHERE (" + strHighValue + " + 1/24)<=(SYSDATE + " + mstrReservedDate + ")";
						else if(strPartitionType.equals("Daily"))
							strSQL = "SELECT 'TO_DATE(' || TO_CHAR(" + strHighValue + " + 1,'YYYYMMDD') || ',''YYYYMMDD'')'," +
									 "TO_CHAR(" + strHighValue + ",'YYYYMMDDHH24MISS') FROM DUAL WHERE (" + strHighValue + " + 1)<=(SYSDATE + " + mstrReservedDate + ")";
						else if(strPartitionType.equals("Monthly"))
							strSQL = "SELECT 'TO_DATE(' || TO_CHAR(ADD_MONTHS(" + strHighValue + ",1),'YYYYMM') || ',''YYYYMM'')'," +
									 "TO_CHAR(" + strHighValue + ",'YYYYMMDDHH24MISS') FROM DUAL WHERE ADD_MONTHS(" + strHighValue + ",1)<=(SYSDATE + " + mstrReservedDate + ")";
						else
							strSQL = "SELECT 'TO_DATE(' || TO_CHAR(ADD_MONTHS(" + strHighValue + ",12),'YYYY') || ',''YYYY'')'," +
									 "TO_CHAR(" + strHighValue + ",'YYYYMMDDHH24MISS') FROM DUAL WHERE ADD_MONTHS(" + strHighValue + ",12)<=(SYSDATE + " + mstrReservedDate + ")";
						rs = stmt.executeQuery(strSQL);
						if(rs.next())
						{
							strHighValue = rs.getString(1);
							SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
							fmt.setLenient(false);
							dtTableSpace = fmt.parse(rs.getString(2));
						}
						else
							strHighValue = "";
						rs.close();

						if(strHighValue.length() > 0)
						{
							// Format table space name
							String strTablespaceName = formatTablespaceName(strTablespaceFormat,
								dtTableSpace,strTablespaceDateFormat,strTableName);

							// Create table space if not exist
							boolean bExist = false;
							strSQL = "SELECT 1 FROM USER_TABLESPACES" +
									 " WHERE UPPER(TABLESPACE_NAME)=UPPER('" + strTablespaceName + "')";
							rs = stmt.executeQuery(strSQL);
							if(rs.next())
								bExist = true;
							rs.close();
							if(!bExist)
							{
								// Get child folder date
								String strCurrentDate = "";
								if(mstrStorageStyle.equals("Daily"))
									strCurrentDate = StringUtil.format(dtTableSpace,"yyyyMMdd") + "/";
								else if(mstrStorageStyle.equals("Monthly"))
									strCurrentDate = StringUtil.format(dtTableSpace,"yyyyMM") + "/";
								else if(mstrStorageStyle.equals("Yearly"))
									strCurrentDate = StringUtil.format(dtTableSpace,"yyyy") + "/";
								String strDataFile = mstrStorageDir + strCurrentDate + strTablespaceName + "_" + getDataFileIndex(cn,strTablespaceName,"0000");
								FileUtil.forceFolderExist(mstrStorageDir + strCurrentDate);

								// Create table space
								logMonitor("Start creating table space " + strTablespaceName);
								strSQL = "CREATE TABLESPACE " + strTablespaceName + " DEFAULT STORAGE" +
										 " (INITIAL 10M NEXT 10M MAXEXTENTS UNLIMITED)" +
										 " DATAFILE '" +  strDataFile +
										 ".dbf' SIZE " + String.valueOf(miExtendedSize) + "M";
								stmt.executeUpdate(strSQL);
								logMonitor("Creating table space " + strTablespaceName + " completed");
							}

							// Add partition
							String strPartition = formatPartitionName(strPartitionFormat,
																	  dtTableSpace,
																	  strPartitionDateFormat,
																	  strTableName,
																	  strTablespaceName)
												  + getPartitionIndex(cn,strTableName,strTablespaceName);
							logMonitor("Start creating partition " + strPartition + " for table " + strTableName);
							strSQL = "ALTER TABLE " + strTableName + " ADD PARTITION " + strPartition +
									 " VALUES LESS THAN (" + strHighValue + ")" +
									 " STORAGE (INITIAL 2M NEXT 2M MAXEXTENTS UNLIMITED)" +
									 " TABLESPACE " + strTablespaceName;
							stmt.executeUpdate(strSQL);
							logMonitor("Creating partition " + strPartition + " for table " + strTableName + " completed");
						}
					}
				}
			}
			stmt.close();
		}
		finally
		{
			Database.closeObject(cn);
		}
	}
	////////////////////////////////////////////////////////
	protected String formatTablespaceName(String strFormat,
										  java.util.Date dt,
										  String strDateFormat,
										  String strTableName) throws Exception
	{
		String strReturn = strFormat;
		strReturn = StringUtil.replaceAll(strReturn,"$Date",StringUtil.format(dt,strDateFormat));
		strReturn = StringUtil.replaceAll(strReturn,"$TableName",strTableName);
		return strReturn;
	}
	////////////////////////////////////////////////////////
	protected String formatPartitionName(String strFormat,
										 java.util.Date dt,
										 String strDateFormat,
										 String strTableName,
										 String strTablespaceName) throws Exception
	{
		String strReturn = strFormat;
		strReturn = StringUtil.replaceAll(strReturn,"$Date",StringUtil.format(dt,strDateFormat));
		strReturn = StringUtil.replaceAll(strReturn,"$TableName",strTableName);
		strReturn = StringUtil.replaceAll(strReturn,"$TablespaceName",strTablespaceName);
		return strReturn;
	}
	////////////////////////////////////////////////////////
	private static String getDataFileIndex(Connection cn,String strDataFileName,String strFormat) throws Exception
	{
		int iReturn = 1;
		boolean bExist = true;
		String strSQL = "SELECT 1 FROM GV$DATAFILE WHERE INSTR(UPPER(NAME)," +
						"UPPER('" + strDataFileName + "' || '_' || TRIM(TO_CHAR(?,'" + strFormat + "'))))>0";
		PreparedStatement stmt = cn.prepareStatement(strSQL);
		int iMax = (int)Math.pow(10,strFormat.length());
		while(bExist)
		{
			stmt.setInt(1,iReturn);
			ResultSet rs = stmt.executeQuery();
			if(!rs.next())
				bExist = false;
			else
				iReturn++;
			rs.close();
			if(iReturn >= iMax)
				throw new Exception("Unlimited loop found");
		}
		stmt.close();

		strSQL = "SELECT TRIM(TO_CHAR(?,'" + strFormat + "')) FROM DUAL";
		stmt = cn.prepareStatement(strSQL);
		stmt.setInt(1,iReturn);
		ResultSet rs = stmt.executeQuery();
		String strReturn = "";
		if(rs.next())
			strReturn = rs.getString(1);
		rs.close();
		stmt.close();
		return strReturn;
	}
	////////////////////////////////////////////////////////
	private static String getPartitionIndex(Connection cn,String strTableName,String strPartitionName) throws Exception
	{
		int iReturn = 0;
		boolean bExist = true;
		String strSQL = "SELECT 1 FROM USER_TAB_PARTITIONS WHERE" +
						" UPPER(TABLE_NAME)=UPPER('" + strTableName + "') AND" +
						" UPPER(PARTITION_NAME)=UPPER('" + strPartitionName + "' || ?)";

		PreparedStatement stmt = cn.prepareStatement(strSQL);
		while(bExist)
		{
			if(iReturn > 0)
				stmt.setInt(1,iReturn);
			else
				stmt.setString(1,"");
			ResultSet rs = stmt.executeQuery();
			if(!rs.next())
				bExist = false;
			else
				iReturn++;
			rs.close();
		}
		stmt.close();

		if(iReturn > 0)
			return String.valueOf(iReturn);
		return "";
	}
}
