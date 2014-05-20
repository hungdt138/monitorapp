package com.fss.queue;

import java.io.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public interface Message extends Attributable
{
	////////////////////////////////////////////////////////
	/**
	 * Load message from input stream
	 * @param is InputStream
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	void load(InputStream is) throws Exception;
	////////////////////////////////////////////////////////
	/**
	 * Store message to output stream
	 * @param os OutputStream
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	void store(OutputStream os) throws Exception;
	////////////////////////////////////////////////////////
	/**
	 * Get message content
	 * @return String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	String getContent() throws Exception;
}
