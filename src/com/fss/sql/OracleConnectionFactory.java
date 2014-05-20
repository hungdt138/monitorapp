package com.fss.sql;

import java.sql.*;
import oracle.jdbc.pool.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class OracleConnectionFactory
{
	////////////////////////////////////////////////////////
	// Constant
	////////////////////////////////////////////////////////
	public static final int DYNAMIC_SCHEME = OracleConnectionCacheImpl.DYNAMIC_SCHEME;
	public static final int FIXED_RETURN_NULL_SCHEME = OracleConnectionCacheImpl.FIXED_RETURN_NULL_SCHEME;
	public static final int FIXED_WAIT_SCHEME = OracleConnectionCacheImpl.FIXED_WAIT_SCHEME;
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
    private OracleConnectionCacheImpl cache = null;
    private OracleConnectionPoolDataSource pool = null;
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strUrl String
	 * @param strUserName String
	 * @param strPassword String
	 * @param iMaxConnection int
	 * @throws SQLException
	 */
	////////////////////////////////////////////////////////
	public OracleConnectionFactory(String strUrl,
								   String strUserName,
								   String strPassword,
								   int iMaxConnection) throws SQLException
	{
		this(strUrl,strUserName,strPassword,iMaxConnection,0);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strUrl String
	 * @param strUserName String
	 * @param strPassword String
	 * @param iMaxConnection int
	 * @param iMinConnection int
	 * @throws SQLException
	 */
	////////////////////////////////////////////////////////
	public OracleConnectionFactory(String strUrl,
								   String strUserName,
								   String strPassword,
								   int iMaxConnection,
								   int iMinConnection) throws SQLException
	{
		this(strUrl,strUserName,strPassword,iMaxConnection,
			 iMinConnection,DYNAMIC_SCHEME);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strUrl String
	 * @param strUserName String
	 * @param strPassword String
	 * @param iMaxConnection int
	 * @param iMinConnection int
	 * @param iScheme int
	 * @throws SQLException
	 */
	////////////////////////////////////////////////////////
	public OracleConnectionFactory(String strUrl,
								   String strUserName,
								   String strPassword,
								   int iMaxConnection,
								   int iMinConnection,
								   int iScheme) throws SQLException
	{
		pool = createConnectionPool(strUrl,strUserName,strPassword);
		cache = createConnectionCache(pool,iMinConnection,
									  iMaxConnection,iScheme);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strUrl String
	 * @param strUserName String
	 * @param strPassword String
	 * @return OracleConnectionPoolDataSource
	 * @throws SQLException
	 */
	////////////////////////////////////////////////////////
	private static OracleConnectionPoolDataSource createConnectionPool(
		   String strUrl,String strUserName,String strPassword) throws SQLException
	{
		OracleConnectionPoolDataSource ods = new OracleConnectionPoolDataSource();
		ods.setURL(strUrl);
		ods.setUser(strUserName);
		ods.setPassword(strPassword);
		return ods;
	}
	////////////////////////////////////////////////////////
	/**
	 * Create connection cache
	 * @param pool OracleConnectionPoolDataSource
	 * @param iMinConnection int
	 * @param iMaxConnection int
	 * @param iScheme int
	 * @return OracleConnectionCacheImpl
	 * @throws SQLException
	 */
	////////////////////////////////////////////////////////
	private static OracleConnectionCacheImpl createConnectionCache(
		   OracleConnectionPoolDataSource pool,
		   int iMinConnection,int iMaxConnection,int iScheme) throws SQLException
	{
		OracleConnectionCacheImpl cache = new OracleConnectionCacheImpl(pool);
		cache.setMaxLimit(iMaxConnection);
		cache.setMinLimit(iMinConnection);
		cache.setCacheScheme(iScheme);
		return cache;
	}
	////////////////////////////////////////////////////////
	/**
	 * Get connection from cache
	 * @return Connection
	 * @throws SQLException
	 */
	////////////////////////////////////////////////////////
	public Connection getConnection() throws SQLException
	{
		return cache.getConnection();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @throws SQLException
	 */
	////////////////////////////////////////////////////////
	public void close() throws SQLException
	{
		cache.close();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @throws Throwable
	 */
	////////////////////////////////////////////////////////
	protected void finalize() throws Throwable
    {
        cache.close();
        super.finalize();
    }
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return OracleConnectionCacheImpl
	 */
	////////////////////////////////////////////////////////
	public OracleConnectionCacheImpl getOracleConnectionCache()
	{
		return cache;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return OracleConnectionPoolDataSource
	 */
	////////////////////////////////////////////////////////
	public OracleConnectionPoolDataSource getOracleConnectionPoolDataSource()
	{
		return pool;
	}
}
