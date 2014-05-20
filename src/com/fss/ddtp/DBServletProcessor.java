package com.fss.ddtp;

import java.sql.*;

import com.fss.sql.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public abstract class DBServletProcessor extends ServletProcessor
{
	/////////////////////////////////////////////////////////////////
	// Variables
	/////////////////////////////////////////////////////////////////
	protected Connection mcnMain = null; // Used to update database
	////////////////////////////////////////////////////////
	/**
	 * Open conection to database
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public abstract void open() throws Exception;
	////////////////////////////////////////////////////////
	/**
	 * Open conection to database
	 * @param bAutoCommit if true then new transaction created
	 * @throws java.lang.Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void open(boolean bAutoCommit) throws Exception
	{
		open();
		mcnMain.setAutoCommit(bAutoCommit);
	}
	////////////////////////////////////////////////////////
	/**
	 * Release database connection
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void close()
	{
		if(mcnMain != null)
		{
			Database.closeObject(mcnMain);
			mcnMain = null;
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @throws Throwable
	 */
	////////////////////////////////////////////////////////
	public void finalize() throws Throwable
	{
		close();
		super.finalize();
	}
}
