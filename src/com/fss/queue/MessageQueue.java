package com.fss.queue;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 * @deprecated You should not use or extend this class,
 * use or extend class Queue intead
 */

public class MessageQueue extends Queue
{
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strIndex String[]
	 */
	////////////////////////////////////////////////////////
	public MessageQueue(String[] strIndex)
	{
		super(strIndex);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param msg Message
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void attach(Message msg) throws Exception
	{
		super.attach(msg);
	}
	////////////////////////////////////////////////////////
	protected Message onMessageTimedOut(Message msg)
	{
		return (Message)super.onMessageTimedOut(msg);
	}
	////////////////////////////////////////////////////////
	protected void onRemoveMessage(Message msg,Exception excReason)
	{
		super.onRemoveMessage(msg,excReason);
	}
}
