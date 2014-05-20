package com.fss.util;

import java.util.*;

/**
 * <p>Title: LogInterface</p>
 * <p>Description: frame work for system logging</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public interface LogInterface
{
	////////////////////////////////////////////////////////
	/**
	 * Put log data into log header (Using user id)
	 * @param strModuleName Module was changed
	 * @param strUserName changed user
	 * @param strActionType type of action
	 * @return LogID
	 * @throws Exception if error occured
	 * @author Thai Hoang Hiep - Date: 22/04/2003
	 */
	////////////////////////////////////////////////////////
	String logHeader(String strModuleName,String strUserName,String strActionType) throws Exception;
	////////////////////////////////////////////////////////
	/**
	 * Save data before update
	 * @param strLogID id of log header
	 * @param strTableName table to update
	 * @param strCondition update id
	 * @return Vector contain list of change id
	 * @throws Exception if error occured
	 * @author Thai Hoang Hiep - Date: 22/04/2003
	 */
	////////////////////////////////////////////////////////
	Vector logBeforeUpdate(String strLogID,String strTableName,String strCondition) throws Exception;
	////////////////////////////////////////////////////////
	/**
	 * Save data after update
	 * @param vtChangeID list of update changeid
	 * @throws Exception if error occured
	 * @author Thai Hoang Hiep - Date: 22/04/2003
	 */
	////////////////////////////////////////////////////////
	void logAfterUpdate(Vector vtChangeID) throws Exception;
	////////////////////////////////////////////////////////
	/**
	 * Save data after insert
	 * @param strLogID id of log header
	 * @param strTableName table to insert
	 * @param strCondition insert id
	 * @throws Exception if error occured
	 * @author Thai Hoang Hiep - Date: 22/04/2003
	 */
	////////////////////////////////////////////////////////
	void logAfterInsert(String strLogID,String strTableName,String strCondition) throws Exception;
	////////////////////////////////////////////////////////
	/**
	 * Save data before delete
	 * @param strLogID id of log header
	 * @param strTableName table to delete
	 * @param strCondition delete id
	 * @throws Exception if error occured
	 * @author Thai Hoang Hiep - Date: 22/04/2003
	 */
	////////////////////////////////////////////////////////
	void logBeforeDelete(String strLogID,String strTableName,String strCondition) throws Exception;
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strChangeID String
	 * @param strColumnName String
	 * @param strOldValue String
	 * @param strNewValue String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	void logColumnChange(String strChangeID,String strColumnName,String strOldValue,String strNewValue) throws Exception;
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strLogID String
	 * @param strTableName String
	 * @param strRowID String
	 * @param strActionType String
	 * @return String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	String logTableChange(String strLogID,String strTableName,String strRowID,String strActionType) throws Exception;
}
