package com.fss.ddtp;


/**
 * <p>Title: ExpireListener</p>
 * <p>Description: Used to catch expire event</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public interface ExpireListener
{
	/////////////////////////////////////////////////////////////////
	/**
	 * Event raised after SESSION_EXPIRE exception was throwed
	 * @param objSource object which throw exception
	 * @param request ddtp package cause exception
	 * @return DDTP if reconnect successfully
	 * @throws Exception
	 * @author Thai Hoang Hiep - Date: 3/3/2004
	 */
	/////////////////////////////////////////////////////////////////
	DDTP onExpire(Object objSource,DDTP request) throws Exception;
}