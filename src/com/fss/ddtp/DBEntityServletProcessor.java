package com.fss.ddtp;

import java.util.*;
import com.fss.util.*;

/**
 * <p>Title: DBEntityServletProcessor</p>
 * <p>Description: Process request relate to database table</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public abstract class DBEntityServletProcessor extends DBServletProcessor
{
	/////////////////////////////////////////////////////////////////
	// Variables
	/////////////////////////////////////////////////////////////////
	protected String mstrPrimaryKey = null;
	protected String mstrLogID = null;
	////////////////////////////////////////////////////////
	/**
	 * Insert event
	 * @throws Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	protected abstract void onInsert() throws Exception;
	////////////////////////////////////////////////////////
	/**
	 * Before commit insert event
	 * @throws Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	protected void beforeCommitInsert() throws Exception
	{
	}
	////////////////////////////////////////////////////////
	/**
	 * Update event
	 * @throws Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	protected abstract void onUpdate() throws Exception;
	////////////////////////////////////////////////////////
	/**
	 * Before commit update event
	 * @throws Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	protected void beforeCommitUpdate() throws Exception
	{
	}
	////////////////////////////////////////////////////////
	/**
	 * Delete event
	 * @throws Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	protected abstract void onDelete() throws Exception;
	////////////////////////////////////////////////////////
	/**
	 * Before commit delete event
	 * @throws Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	protected void beforeCommitDelete() throws Exception
	{
	}
	////////////////////////////////////////////////////////
	/**
	 * Fetch event
	 * @throws Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	protected void onFetch() throws Exception
	{
	}
	////////////////////////////////////////////////////////
	/**
	 * Query event
	 * @return query data
	 * @throws Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	protected Vector onQuery() throws Exception
	{
		return null;
	}
	////////////////////////////////////////////////////////
	/**
	 * Load cac public variable tu request parameter
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	protected void loadParameter()
	{
		mstrPrimaryKey = StringUtil.nvl(request.getString("mstrPrimaryKey"),"");
	}
	////////////////////////////////////////////////////////
	/**
	 * Store cac public variable vao response parameter
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	protected void storeParameter()
	{
		response.setString("mstrPrimaryKey",mstrPrimaryKey);
	}
	////////////////////////////////////////////////////////
	/**
	 * Insert new entity data into database
	 * @throws java.lang.Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void insert() throws Exception
	{
		try
		{
			// Open connection & create transaction
			open(false);

			// Check priviledge
			if(authenticator != null)
				authenticator.checkPrivilege(server.sessionUserID,getModuleName(),"I");

			// Log header
			if(log != null)
				mstrLogID = log.logHeader(getModuleName(),server.sessionUserName,"I");

			// Execute insert
			onInsert();

			// Commit transaction
			mcnMain.commit();
			mcnMain.setAutoCommit(true);
		}
		finally
		{
			close();
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Update entity data
	 * @throws java.lang.Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void update() throws Exception
	{
		try
		{
			// Open connection & create transaction
			open(false);

			// Check priviledge
			if(authenticator != null)
				authenticator.checkPrivilege(server.sessionUserID,getModuleName(),"U");

			// Log header
			if(log != null)
				mstrLogID = log.logHeader(getModuleName(),server.sessionUserName,"U");

			// Execute update
			onUpdate();

			// Commit transaction
			mcnMain.commit();
			mcnMain.setAutoCommit(true);
		}
		finally
		{
			close();
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Delete entity data
	 * @throws java.lang.Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void delete() throws Exception
	{
		try
		{
			// Open connection & create transaction
			open(false);

			// Check priviledge
			if(authenticator != null)
				authenticator.checkPrivilege(server.sessionUserID,getModuleName(),"D");

			// Log header
			if(log != null)
				mstrLogID = log.logHeader(getModuleName(),server.sessionUserName,"D");

			// Execute delete
			onDelete();

			// Commit transaction
			mcnMain.commit();
			mcnMain.setAutoCommit(true);
		}
		finally
		{
			close();
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Fetch entity data
	 * @throws java.lang.Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void fetch() throws Exception
	{
		try
		{
			// Create connection
			open(true);

			// Check priviledge
			if(authenticator != null)
				authenticator.checkPrivilege(server.sessionUserID,getModuleName(),"S");

			// Execute fetch
			onFetch();
		}
		finally
		{
			close();
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Query entity data list
	 * @return entity data list
	 * @throws java.lang.Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public Vector query() throws Exception
	{
		try
		{
			// Open connection
			open(true);

			// Check priviledge
			if(authenticator != null)
				authenticator.checkPrivilege(server.sessionUserID,getModuleName(),"S");

			// Execute query
			return onQuery();
		}
		finally
		{
			close();
		}
	}
	////////////////////////////////////////////////////////
	// Override function
	////////////////////////////////////////////////////////
	public void setRequest(DDTP request)
	{
		super.setRequest(request);
		loadParameter();
	}
	////////////////////////////////////////////////////////
	public DDTP getResponse()
	{
		storeParameter();
		return super.getResponse();
	}
}
