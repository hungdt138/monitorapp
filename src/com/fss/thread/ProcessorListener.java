package com.fss.thread;

import java.sql.*;

/**
 * <p>Title: Dictionary</p>
 * <p>Description: Object contain dictionary data</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public interface ProcessorListener
{
	/**
	 * Get connection to database
	 * @return connection to database
	 * @throws java.lang.Exception
	 */
	Connection getConnection() throws Exception;
	/**
	 * Event raised when caller is created
	 * @param objSource caller
	 * @throws java.lang.Exception
	 */
	void onCreate(ThreadProcessor processor) throws Exception;
	/**
	 * Event raised when caller open connection to database
	 * @param objSource caller
	 * @throws java.lang.Exception
	 */
	void onOpen(ThreadProcessor processor) throws Exception;
}
