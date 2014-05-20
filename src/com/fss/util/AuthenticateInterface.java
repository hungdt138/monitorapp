package com.fss.util;

import java.util.*;
import java.net.*;

/**
 * <p>Title: AuthenticateInterface</p>
 * <p>Description: Frame work for system authenticate</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public interface AuthenticateInterface
{
	////////////////////////////////////////////////////////
	/**
	 * Check accesstime of a user
	 * @param strUserID ID of user
	 * @return true if user is allowed to login at the moment
	 * otherswise false
	 * @author Thai Hoang Hiep - Date: 22/04/2003
	 */
	////////////////////////////////////////////////////////
	boolean checkAccessTime(String strUserID);
	////////////////////////////////////////////////////////
	/**
	 * Check access right of a user
	 * @param strUserID ID of user
	 * @param strModuleName name of module
	 * @param strRightCode right code
	 * @throws Exception if user does not has access right
	 * on module or exception occured
	 * @author Thai Hoang Hiep - Date: 22/04/2003
	 */
	////////////////////////////////////////////////////////
	void checkPrivilege(String strUserID,String strModuleName,String strRightCode) throws Exception;
	////////////////////////////////////////////////////////
	/**
	 * Verify ip address
	 * @param address InetAddress
	 * @param strUserID String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	void checkIP(InetAddress address,String strUserID) throws Exception;
	////////////////////////////////////////////////////////
	/**
	 * Verify user password
	 * @param strUserName name of user
	 * @param strPassword password to verify
	 * @return ID of user
	 * @throws Exception if user does not exist or verification failed
	 * or exception occured
	 * @author Thai Hoang Hiep - Date: 22/04/2003
	 */
	////////////////////////////////////////////////////////
	String verifyPassword(String strUserName, String strPassword) throws Exception;
	////////////////////////////////////////////////////////
	/**
	 * Change password of a user
	 * @param strUserName name of user
	 * @param strOldPassword password to verify
	 * @param strNewPassword password to change
	 * @throws Exception if user does not exist or verification failed
	 * or exception occured
	 * @author Thai Hoang Hiep - Date: 22/04/2003
	 */
	////////////////////////////////////////////////////////
	void changePassword(String strUserName,String strOldPassword,String strNewPassword) throws Exception;
	////////////////////////////////////////////////////////
	/**
	 * Return permission data
	 * @param strUserID String
	 * @return Vector
	 * @throws Exception
	 */
	Vector queryPermissionData(String strUserID) throws Exception;
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strPassword String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	void validatePassword(String strRealPassword) throws Exception;
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strUserID String
	 * @return boolean
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	boolean isPasswordExpired(String strUserID) throws Exception;
}
