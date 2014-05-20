package com.fss.queue;

import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public interface Attributable
{
	////////////////////////////////////////////////////////
	/**
	 * Get message attribute
	 * @param strKey String
	 * @return String
	 */
	////////////////////////////////////////////////////////
	Object getAttribute(String strKey);
	////////////////////////////////////////////////////////
	/**
	 * Set message attribute
	 * @param strKey String
	 * @param objValue Object
	 */
	////////////////////////////////////////////////////////
	void setAttribute(String strKey,Object objValue);
	////////////////////////////////////////////////////////
	/**
	 * Compare message attribute
	 * @return Hashtable
	 */
	////////////////////////////////////////////////////////
	Hashtable getAttributes();
	////////////////////////////////////////////////////////
	/**
	 * Set message attribute
	 * @param prt Hashtable
	 */
	////////////////////////////////////////////////////////
	void setAttributes(Hashtable prt);
}
