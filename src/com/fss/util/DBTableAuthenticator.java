package com.fss.util;

import java.sql.*;
import java.util.*;
import java.net.*;
import java.io.*;

import com.fss.sql.*;

/**
 * <p>Title: DBTableAuthenticator</p>
 * <p>Description: Implements system authenticate,
 * using Databse table to store authenticate data</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class DBTableAuthenticator implements AuthenticateInterface
{
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	protected Connection mcn;
	protected String mstrAuthenticateHost;
	protected boolean mbCache = true;
	protected long mlCacheDuration = 3600000; // One hour
	private long mlClearTime = 0;
	public static Vector mvtDayID = new Vector();
	////////////////////////////////////////////////////////
	static
	{
		mvtDayID.addElement("MON");
		mvtDayID.addElement("TUE");
		mvtDayID.addElement("WED");
		mvtDayID.addElement("THU");
		mvtDayID.addElement("FRI");
		mvtDayID.addElement("SAT");
		mvtDayID.addElement("SUN");
	}
	////////////////////////////////////////////////////////
	class PermissionCache
	{
		public long mlExpireTime;
		public String mstrUserID;
		public Vector mvtIPData;
		public Vector mvtModuleData;
		public Vector mvtScheduleData;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return long
	 */
	////////////////////////////////////////////////////////
	public long getCacheDuration()
	{
		return mlCacheDuration;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param lDuration long
	 */
	////////////////////////////////////////////////////////
	public void setCacheDuration(long lDuration)
	{
		mlCacheDuration = lDuration;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean usingCache()
	{
		return mbCache;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param bCache boolean
	 */
	////////////////////////////////////////////////////////
	public void setUsingCache(boolean bCache)
	{
		mbCache = bCache;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return Hashtable
	 */
	////////////////////////////////////////////////////////
	public Hashtable getPermissionCache()
	{
		return null;
	}
	////////////////////////////////////////////////////////
	/**
	 * Set connection used to manipulate database
	 * @param cn Connection
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void setConnection(Connection cn)
	{
		mcn = cn;
	}
	////////////////////////////////////////////////////////
	/**
	 * Get authenticator connection
	 * @return Connection
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public Connection getConnection()
	{
		return mcn;
	}
	////////////////////////////////////////////////////////
	/**
	 * Set authenticate host if using http athenticate
	 * @param strHost String
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void setAuthenticateHost(String strHost)
	{
		mstrAuthenticateHost = strHost;
	}
	////////////////////////////////////////////////////////
	// Override function
	////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strUserID String
	 * @return String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public String queryGroupList(String strUserID) throws Exception
	{
		// Get group list from user group
		PreparedStatement stmt = null;
		try
		{
			Vector vtGroup = new Vector();
			String strSQL = "SELECT GROUP_ID FROM SEC_GROUP_USER WHERE USER_ID=?";
			stmt = mcn.prepareStatement(strSQL);
			stmt.setString(1,strUserID);
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
				vtGroup.addElement(rs.getString(1));
			rs.close();
			stmt.close();

			// Get parent group list
			strSQL = "SELECT PARENT_GROUP FROM SEC_GROUP WHERE GROUP_ID=?";
			stmt = mcn.prepareStatement(strSQL);
			int iSize = vtGroup.size();
			for(int iIndex = 0;iIndex < iSize;iIndex++)
			{
				String strGroupID = (String)vtGroup.elementAt(iIndex);
				do
				{
					stmt.setString(1,strGroupID);
					rs = stmt.executeQuery();
					rs.next();
					strGroupID = StringUtil.nvl(rs.getString(1),"");
					rs.close();
					if(strGroupID.length() > 0 && vtGroup.indexOf(strGroupID) < 0)
					{
						vtGroup.insertElementAt(strGroupID,0);
						iIndex++;
					}
				}
				while(strGroupID.length() > 0);
			}
			stmt.close();

			// Fill group list to statement
			String strGroup = "";
			for(int iIndex = 0;iIndex < vtGroup.size();iIndex++)
				strGroup += (String)vtGroup.elementAt(iIndex) + ",";

			if(strGroup.length() > 0)
				return strGroup.substring(0,strGroup.length() - 1);
			return strGroup;
		}
		finally
		{
			Database.closeObject(stmt);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strUserID String
	 * @param iLevel int
	 * @return Vector
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public Vector queryGroupList(String strUserID,int iLevel) throws Exception
	{
		// Get group list from user group
		PreparedStatement stmt = null;
		try
		{
			Vector vtGroup = new Vector();
			String strSQL = "SELECT GROUP_ID FROM SEC_GROUP_USER WHERE USER_ID=?";
			stmt = mcn.prepareStatement(strSQL);
			stmt.setString(1,strUserID);
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
			{
				Vector vtRow = new Vector();
				vtRow.addElement(rs.getString(1));
				vtRow.addElement(String.valueOf(iLevel));
				vtGroup.addElement(vtRow);
			}
			rs.close();
			stmt.close();

			// Get parent group list
			strSQL = "SELECT PARENT_GROUP FROM SEC_GROUP WHERE GROUP_ID=?";
			stmt = mcn.prepareStatement(strSQL);
			int iSize = vtGroup.size();
			for(int iIndex = 0;iIndex < iSize;iIndex++)
			{
				Vector vtRow = (Vector)vtGroup.elementAt(iIndex);
				String strGroupID = (String)vtRow.elementAt(0);
				int iParentLevel = iLevel;
				do
				{
					stmt.setString(1,strGroupID);
					rs = stmt.executeQuery();
					rs.next();
					strGroupID = StringUtil.nvl(rs.getString(1),"");
					iParentLevel++;
					rs.close();

					if(strGroupID.length() > 0)
					{
						boolean bFound = false;
						for(int iGroupIndex = 0;!bFound && iGroupIndex < vtGroup.size();iGroupIndex++)
						{
							vtRow = (Vector)vtGroup.elementAt(iGroupIndex);
							if(vtRow.elementAt(0).equals(strGroupID))
							{
								if(Integer.parseInt((String)vtRow.elementAt(1)) >= iParentLevel)
									bFound = true;
								else
								{
									vtGroup.removeElementAt(iGroupIndex);
									if(iGroupIndex <= iIndex)
										iIndex--;
								}
							}
						}

						if(!bFound)
						{
							vtRow = new Vector();
							vtRow.addElement(strGroupID);
							vtRow.addElement(String.valueOf(iParentLevel));
							vtGroup.insertElementAt(vtRow,0);
							iIndex++;
						}
					}
				}
				while(strGroupID.length() > 0);
			}
			stmt.close();
			return vtGroup;
		}
		finally
		{
			Database.closeObject(stmt);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param vtGroup Vector
	 * @return String
	 */
	////////////////////////////////////////////////////////
	public static Vector analyseGroupList(Vector vtGroup)
	{
		// Fill group list to statement
		String strGroup = "";
		String strPriority = "";
		for(int iIndex = 0;iIndex < vtGroup.size();iIndex++)
		{
			Vector vtRow = (Vector)vtGroup.elementAt(iIndex);
			strGroup += (String)vtRow.elementAt(0) + ",";
			strPriority += (String)vtRow.elementAt(0) + "," + (String)vtRow.elementAt(1) + ",";
		}
		if(strGroup.length() > 0)
			strGroup = strGroup.substring(0,strGroup.length() - 1);
		if(strPriority.length() > 0)
			strPriority = strPriority.substring(0,strPriority.length() - 1);
		Vector vtReturn = new Vector();
		vtReturn.addElement(strGroup);
		vtReturn.addElement(strPriority);
		return vtReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 */
	////////////////////////////////////////////////////////
	public void clearExpiredCache()
	{
		// Clear expired cache
		Hashtable prt = getPermissionCache();
		if(prt != null)
		{
			synchronized(prt)
			{
				if(shouldCheckCacheNow())
				{
					Enumeration enm = prt.keys();
					while(enm.hasMoreElements())
					{
						String strKey = (String)enm.nextElement();
						PermissionCache cache = (PermissionCache)prt.get(strKey);
						if(cache.mlExpireTime < System.currentTimeMillis())
							prt.remove(strKey);
					}
					updateClearTime();
				}
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 */
	////////////////////////////////////////////////////////
	public void updateClearTime()
	{
		mlClearTime = System.currentTimeMillis() + getCacheDuration() / 12; // 5 minute clear once
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean shouldCheckCacheNow()
	{
		if(System.currentTimeMillis() > mlClearTime)
			return true;
		return false;
	}
	////////////////////////////////////////////////////////
	/**
	 * Use cache mechanism
	 * @param strUserID String
	 * @return Vector
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public Vector getModuleData(String strUserID) throws Exception
	{
		clearExpiredCache();
		Hashtable prt = getPermissionCache();
		if(usingCache() && prt != null)
		{
			// Get cache & insert if not exist
			PermissionCache cache = (PermissionCache)prt.get(strUserID);
			if(cache == null)
			{
				cache = new PermissionCache();
				cache.mstrUserID = strUserID;
				cache.mlExpireTime = System.currentTimeMillis() + getCacheDuration();
				prt.put(cache.mstrUserID,cache);
			}
			if(cache.mvtModuleData == null)
				cache.mvtModuleData = queryModuleData(strUserID);
			return cache.mvtModuleData;
		}
		else
			return queryModuleData(strUserID);
	}
	////////////////////////////////////////////////////////
	/**
	 * Query module data from database
	 * @param strUserID String
	 * @return Vector
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public Vector queryModuleData(String strUserID) throws Exception
	{
		return queryModuleData(strUserID,"");
	}
	////////////////////////////////////////////////////////
	/**
	 * Query module data from database
	 * @param strUserID String
	 * @param strModuleName String
	 * @return Vector
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public Vector queryModuleData(String strUserID,String strModuleName) throws Exception
	{
		Vector vtGroup = analyseGroupList(queryGroupList(strUserID,1));
		String strGroup = (String)vtGroup.elementAt(0);
		String strPriority = (String)vtGroup.elementAt(1);
		return queryModuleData(strUserID,strModuleName,strGroup,strPriority);
	}
	////////////////////////////////////////////////////////
	/**
	 * Query module data from database
	 * @param strUserID String
	 * @param strModuleName String
	 * @param strGroup String
	 * @param strPriority String
	 * @return Vector
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public Vector queryModuleData(String strUserID,String strModuleName,String strGroup,String strPriority) throws Exception
	{
		Statement stmt = null;
		try
		{
			// Prepare sql command
			String strCondition = "";
			if(strModuleName != null && strModuleName.length() > 0)
				strCondition = " AND SM.MODULE_NAME='" + strModuleName + "' ";
			String strSQL = "SELECT SM.MODULE_NAME,SUM.RIGHT_CODE,SUM.ACCESS_TYPE,0 PRIORITY" +
							" FROM SEC_USER_MODULE SUM,SEC_MODULE SM,SEC_MODULE_RIGHT SMR" +
							" WHERE NVL(SM.STATUS,0)>0" + strCondition +
							" AND SMR.MODULE_ID=SM.MODULE_ID" +
							" AND SUM.MODULE_ID=SM.MODULE_ID" +
							" AND SUM.RIGHT_CODE=SMR.RIGHT_CODE" +
							" AND SUM.USER_ID=" + strUserID + " UNION ALL" +
							" SELECT DISTINCT SM.MODULE_NAME,SMR.RIGHT_CODE,SMR.ACCESS_TYPE,100 PRIORITY" +
							" FROM SEC_MODULE SM,SEC_MODULE_RIGHT SMR" +
							" WHERE NVL(SM.STATUS,0)>0" + strCondition +
							" AND SMR.MODULE_ID=SM.MODULE_ID";
			if(strGroup != null && strGroup.length() > 0)
			{
				strSQL = " SELECT DISTINCT SM.MODULE_NAME,SGM.RIGHT_CODE,SGM.ACCESS_TYPE," +
						 " DECODE(SGM.GROUP_ID," + strPriority + ") PRIORITY" +
						 " FROM SEC_GROUP SG,SEC_GROUP_MODULE SGM,SEC_MODULE SM,SEC_MODULE_RIGHT SMR" +
						 " WHERE NVL(SM.STATUS,0)>0 AND NVL(SG.STATUS,0)>0" + strCondition +
						 " AND SMR.MODULE_ID=SM.MODULE_ID" +
						 " AND SGM.MODULE_ID=SM.MODULE_ID" +
						 " AND SGM.RIGHT_CODE=SMR.RIGHT_CODE" +
						 " AND SGM.GROUP_ID=SG.GROUP_ID" +
						 " AND SGM.GROUP_ID IN (" + strGroup + ")" +
						 " UNION ALL " + strSQL;
			}
			strSQL = "SELECT MODULE_NAME,RIGHT_CODE,NVL(ACCESS_TYPE,0),NVL(MIN(PRIORITY),0)" +
					 " FROM (" + strSQL + ")" +
					 " GROUP BY MODULE_NAME,RIGHT_CODE,ACCESS_TYPE" +
					 " HAVING NVL(ACCESS_TYPE,0)=1" +
					 " ORDER BY MIN(PRIORITY),MODULE_NAME,RIGHT_CODE";
			Vector vtUnique = new Vector();
			Vector vtReturn = new Vector();
			stmt = mcn.createStatement();
			ResultSet rs = stmt.executeQuery(strSQL);
			while(rs.next())
			{
				String strUnique = rs.getString(1) + rs.getString(2);
				if(vtUnique.indexOf(strUnique) < 0)
				{
					vtUnique.addElement(strUnique);
					if(rs.getString(3).equals("1"))
					{
						Vector vtRow = new Vector();
						vtRow.addElement(rs.getString(1));
						vtRow.addElement(rs.getString(2));
						vtReturn.addElement(vtRow);
					}
				}
			}
			rs.close();
			return vtReturn;
		}
		finally
		{
			Database.closeObject(stmt);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Use cache mechanism
	 * @param strUserID String
	 * @return Vector
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public Vector getScheduleData(String strUserID) throws Exception
	{
		clearExpiredCache();
		Hashtable prt = getPermissionCache();
		if(usingCache() && prt != null)
		{
			// Get cache & insert if not exist
			PermissionCache cache = (PermissionCache)prt.get(strUserID);
			if(cache == null)
			{
				cache = new PermissionCache();
				cache.mstrUserID = strUserID;
				cache.mlExpireTime = System.currentTimeMillis() + getCacheDuration();
				prt.put(cache.mstrUserID,cache);
			}
			if(cache.mvtScheduleData == null)
				cache.mvtScheduleData = queryScheduleData(strUserID);
			return cache.mvtScheduleData;
		}
		else
			return queryScheduleData(strUserID);
	}
	////////////////////////////////////////////////////////
	/**
	 * Query schedule data from database
	 * @param strUserID String
	 * @return Vector
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public Vector queryScheduleData(String strUserID) throws Exception
	{
		Statement stmt = null;
		try
		{
			// Prepare sql command
			String strSQL = "SELECT SSD.DAY_ID,SSD.START_TIME,SSD.END_TIME" +
							" FROM SEC_SCHEDULE_DETAIL SSD,SEC_SCHEDULE SS,SEC_GROUP SG,SEC_GROUP_SCHEDULE SGS" +
							" WHERE NVL(SS.STATUS,0)>0 AND NVL(SG.STATUS,0)>0" +
							" AND SSD.SCHEDULE_ID=SS.SCHEDULE_ID" +
							" AND SGS.SCHEDULE_ID=SS.SCHEDULE_ID" +
							" AND SGS.GROUP_ID=SG.GROUP_ID" +
							" AND SGS.GROUP_ID IN (SELECT GROUP_ID FROM SEC_GROUP_USER WHERE USER_ID=" + strUserID + ")" +
							" UNION ALL " +
							"SELECT SSD.DAY_ID,SSD.START_TIME,SSD.END_TIME" +
							" FROM SEC_SCHEDULE_DETAIL SSD,SEC_SCHEDULE SS,SEC_USER_SCHEDULE SUS" +
							" WHERE NVL(SS.STATUS,0)>0" +
							" AND SSD.SCHEDULE_ID=SS.SCHEDULE_ID" +
							" AND SUS.SCHEDULE_ID=SS.SCHEDULE_ID" +
							" AND SUS.USER_ID=" + strUserID;
			strSQL = "SELECT DISTINCT DAY_ID,START_TIME,END_TIME" +
					 " FROM (" + strSQL + ")";
			Vector vtReturn = new Vector();
			stmt = mcn.createStatement();
			ResultSet rs = stmt.executeQuery(strSQL);
			while(rs.next())
			{
				Vector vtRow = new Vector();
				vtRow.addElement(rs.getString(1));
				vtRow.addElement(rs.getString(2));
				vtRow.addElement(rs.getString(3));
				vtReturn.addElement(vtRow);
			}
			rs.close();
			return vtReturn;
		}
		finally
		{
			Database.closeObject(stmt);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Use cache mechanism
	 * @param strUserID String
	 * @return Vector
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public Vector getIPData(String strUserID) throws Exception
	{
		clearExpiredCache();
		Hashtable prt = getPermissionCache();
		if(usingCache() && prt != null)
		{
			// Get cache & insert if not exist
			PermissionCache cache = (PermissionCache)prt.get(strUserID);
			if(cache == null)
			{
				cache = new PermissionCache();
				cache.mstrUserID = strUserID;
				cache.mlExpireTime = System.currentTimeMillis() + getCacheDuration();
				prt.put(cache.mstrUserID,cache);
			}
			if(cache.mvtIPData == null)
				cache.mvtIPData = queryIPData(strUserID);
			return cache.mvtIPData;
		}
		else
			return queryIPData(strUserID);
	}
	////////////////////////////////////////////////////////
	/**
	 * Query IP data from database
	 * @param strUserID String
	 * @return Vector
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public Vector queryIPData(String strUserID) throws Exception
	{
		Vector vtGroup = analyseGroupList(queryGroupList(strUserID,1));
		String strGroup = (String)vtGroup.elementAt(0);
		String strPriority = (String)vtGroup.elementAt(1);
		return queryIPData(strUserID,strGroup,strPriority);
	}
	////////////////////////////////////////////////////////
	/**
	 * Query IP data from database
	 * @param strUserID String
	 * @param strGroup String
	 * @param strPriority String
	 * @return Vector
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public Vector queryIPData(String strUserID,String strGroup,String strPriority) throws Exception
	{
		Statement stmt = null;
		try
		{
			// Prepare sql command
			String strSQL = "SELECT SID.IP_ADDRESS,SUI.GRANT_TYPE,0 PRIORITY,SI.ORD" +
							" FROM SEC_IP_USER SUI,SEC_IP SI,SEC_IP_DETAIL SID" +
							" WHERE NVL(SI.STATUS,0)>0" +
							" AND SUI.IP_ID=SI.IP_ID" +
							" AND SID.IP_ID=SI.IP_ID" +
							" AND SUI.USER_ID=" + strUserID + " UNION ALL" +
							" SELECT DISTINCT SID.IP_ADDRESS,SI.GRANT_TYPE,100 PRIORITY,SI.ORD" +
							" FROM SEC_IP SI,SEC_IP_DETAIL SID" +
							" WHERE NVL(SI.STATUS,0)>0 AND SID.IP_ID=SI.IP_ID";
			if(strGroup != null && strGroup.length() > 0)
			{
				strSQL = "SELECT SID.IP_ADDRESS,SGI.GRANT_TYPE," +
						 " DECODE(SGI.GROUP_ID," + strPriority + ") PRIORITY,SI.ORD" +
						 " FROM SEC_IP_GROUP SGI,SEC_IP SI,SEC_GROUP SG,SEC_IP_DETAIL SID" +
						 " WHERE NVL(SI.STATUS,0)>0 AND NVL(SG.STATUS,0)>0" +
						 " AND SGI.IP_ID=SI.IP_ID" +
						 " AND SID.IP_ID=SI.IP_ID" +
						 " AND SGI.GROUP_ID=SG.GROUP_ID" +
						 " AND SGI.GROUP_ID IN (" + strGroup + ")" +
						 " UNION ALL " + strSQL;
			}
			strSQL = "SELECT IP_ADDRESS,NVL(GRANT_TYPE,0)," +
					 "NVL(MIN(PRIORITY),0),NVL(MAX(ORD),0)" +
					 " FROM (" + strSQL + ")" +
					 " GROUP BY IP_ADDRESS,GRANT_TYPE" +
					 " ORDER BY MIN(PRIORITY),MAX(ORD) DESC,LENGTH(IP_ADDRESS) DESC,IP_ADDRESS,GRANT_TYPE";

			Vector vtUnique = new Vector();
			Vector vtReturn = new Vector();
			stmt = mcn.createStatement();
			ResultSet rs = stmt.executeQuery(strSQL);
			while(rs.next())
			{
				String strUnique = rs.getString(1);
				if(vtUnique.indexOf(strUnique) < 0)
				{
					Vector vtRow = new Vector();
					vtRow.addElement(rs.getString(1));
					vtRow.addElement(rs.getString(2));
					vtReturn.addElement(vtRow);
					vtUnique.addElement(strUnique);
				}
			}
			rs.close();
			return vtReturn;
		}
		finally
		{
			Database.closeObject(stmt);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strUserID String
	 * @return Vector
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public Vector queryPermissionData(String strUserID) throws Exception
	{
		return getModuleData(strUserID);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strUserID String
	 * @param strModuleName String
	 * @param strRightCode String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void checkPrivilege(String strUserID,String strModuleName,String strRightCode) throws Exception
	{
		// Check access time first
		if(!checkAccessTime(strUserID))
			throw new AppException("FSS-00015","DBTableAuthenticator.checkPrivilege");

		// Get module right
		Vector vtModule = getModuleData(strUserID);
		for(int iIndex = 0;iIndex < vtModule.size();iIndex++)
		{
			Vector vtRow = (Vector)vtModule.elementAt(iIndex);
			if(vtRow.elementAt(0).equals(strModuleName) &&
			   vtRow.elementAt(1).equals(strRightCode))
				return;
		}
		throw new AppException("FSS-00014","DBTableAuthenticator.checkPrivilege","'" + strRightCode + "','" + strModuleName + "'");
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strUserID String
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean checkAccessTime(String strUserID)
	{
		try
		{
			// Analyse current time
			final java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("HH:mm:ss");
			java.util.Date dt = new java.util.Date();
			String strDayID = String.valueOf(mvtDayID.indexOf(StringUtil.format(dt,"E").toUpperCase()) + 1);
			String strTime = fmt.format(dt);

			// Get schedule data
			Vector vtSchedule = getScheduleData(strUserID);
			for(int iIndex = 0;iIndex < vtSchedule.size();iIndex++)
			{
				Vector vtRow = (Vector)vtSchedule.elementAt(iIndex);
				if(vtRow.elementAt(0).equals(strDayID))
				{
					String strStartTime = (String)vtRow.elementAt(1);
					String strEndTime = (String)vtRow.elementAt(2);
					if((strStartTime.length() == 0 || strTime.compareTo(strStartTime) >= 0) &&
					   (strEndTime.length() == 0 || strTime.compareTo(strEndTime) <= 0))
						return true;
				}
			}
		}
		catch(Exception e)
		{
		}
		return false;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param ipaddress InetAddress
	 * @param strUserID String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void checkIP(InetAddress ipaddress,String strUserID) throws Exception
	{
		// Prepare argument
		String strHostName = ipaddress.getHostName();
		String strHostAddress = ipaddress.getHostAddress();
		if(strHostAddress == null || strHostAddress.length() == 0)
			throw new AppException("FSS-00022","DBTableAuthenticator.checkIP",strHostAddress);

		// Get IP data
		Vector vtIP = getIPData(strUserID);
		for(int iIndex = 0;iIndex < vtIP.size();iIndex++)
		{
			Vector vtRow = (Vector)vtIP.elementAt(iIndex);
			if(WildcardFilter.match((String)vtRow.elementAt(0),strHostName) ||
			   WildcardFilter.match((String)vtRow.elementAt(0),strHostAddress))
			{
				if("1".equals(vtRow.elementAt(1))) // Allow
					return;
				break;
			}
		}
		throw new AppException("FSS-00022","DBTableAuthenticator.checkIP",strHostAddress);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strUserName String
	 * @param strOldPassword String
	 * @param strNewPassword String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void changePassword(String strUserName,String strOldPassword,String strNewPassword) throws Exception
	{
		// Verify old password
		verifyPassword(strUserName,strOldPassword);

		PreparedStatement stmt = null;
		try
		{
			String strSQL = "UPDATE SEC_USER SET PASSWORD=?,LAST_CHANGE_PASSWORD=SYSDATE," +
							"PASSWORD_EXPIRE_STATUS=DECODE(PASSWORD_EXPIRE_STATUS,0,1,PASSWORD_EXPIRE_STATUS)" +
							" WHERE UPPER(USER_NAME)=UPPER(?)";
			stmt = mcn.prepareStatement(strSQL);
			stmt.setString(1,strNewPassword);
			stmt.setString(2,strUserName);
			stmt.executeUpdate();
		}
		finally
		{
			Database.closeObject(stmt);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strUserName String
	 * @param strPassword String
	 * @return String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public String verifyPassword(String strUserName, String strPassword) throws Exception
	{
		// If using http authenticate
		if(mstrAuthenticateHost != null && mstrAuthenticateHost.length() > 0)
			return windowAuthenticate(mcn,strUserName,strPassword,mstrAuthenticateHost);

		// Not using http authenticate
		PreparedStatement stmt = null;
		try
		{
			// Check
			String strSQL = "SELECT USER_ID, PASSWORD FROM SEC_USER " +
							"WHERE UPPER(USER_NAME)=? AND NVL(STATUS,1)>0";
			stmt = mcn.prepareStatement(strSQL);
			stmt.setString(1,strUserName.toUpperCase());
			ResultSet rs = stmt.executeQuery();

			if(!rs.next())
				throw new AppException("FSS-00016","DBTableAuthenticator.verifyPassword");

			String strReturn = rs.getString(1);
			String strDBPassword = rs.getString(2);
			rs.close();
			stmt.close();

			if(strDBPassword == null)
				strDBPassword = "";
			if(!strDBPassword.equals(strPassword))
			{
				strSQL = "UPDATE SEC_USER SET" +
						 " LOGIN_FAILURE_COUNT=NVL(LOGIN_FAILURE_COUNT,0) + 1" +
						 " WHERE USER_ID=?";
				stmt = mcn.prepareStatement(strSQL);
				stmt.setString(1,strReturn);
				stmt.executeUpdate();
				throw new AppException("FSS-00006","DBTableAuthenticator.verifyPassword");
			}
			strSQL = "UPDATE SEC_USER SET" +
					 " LOGIN_FAILURE_COUNT=0" +
					 " WHERE USER_ID=?";
			stmt = mcn.prepareStatement(strSQL);
			stmt.setString(1,strReturn);
			stmt.executeUpdate();
			return strReturn;
		}
		finally
		{
			Database.closeObject(stmt);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Authenticate by window system
	 * @param cn connection to database
	 * @param user username
	 * @param password password
	 * @param strUrl authenticate host
	 * @return UserID
	 * @throws Exception if authenticate failed
	 */
	////////////////////////////////////////////////////////
	public static String windowAuthenticate(Connection cn,String user,String password,String strUrl) throws Exception
	{
		PreparedStatement pstmt = null;
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		try
		{
			// Get userid
			String strSQL = "SELECT USER_ID FROM SEC_USER " +
							"WHERE UPPER(USER_NAME)=? AND NVL(STATUS,1)>0";
			pstmt = cn.prepareStatement(strSQL);
			pstmt.setString(1,user.toUpperCase());
			ResultSet rs = pstmt.executeQuery();

			if(!rs.next())
				throw new AppException("FSS-00016","DBTableAuthenticator.windowAuthenticate");
			String strUserID = rs.getString(1);
			rs.close();

			// Send authenticate request to authenticate service
			URL url = new URL(strUrl + "?user=" + user + "&password=" + password);
			connection = (HttpURLConnection)url.openConnection();

			// Get response
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuffer strResponse = new StringBuffer();
			String strLine;
			while((strLine = reader.readLine()) != null)
			{
				strResponse.append(strLine);
				strResponse.append('\r');
				strResponse.append('\n');
			}

			// Check response
			if(!strResponse.toString().trim().equals("1"))
				throw new AppException("FSS-00006","DBTableAuthenticator.windowAuthenticate");
			return strUserID;
		}
		finally
		{
			Database.closeObject(pstmt);
			try
			{
				reader.close();
				connection.disconnect();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strPassword String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void validatePassword(String strRealPassword) throws Exception
	{
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strUserID String
	 * @return boolean
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public boolean isPasswordExpired(String strUserID) throws Exception
	{
		return false;
	}

}
