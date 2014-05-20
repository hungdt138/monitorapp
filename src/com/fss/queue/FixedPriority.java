package com.fss.queue;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class FixedPriority implements Priority,ImportantObject
{
	////////////////////////////////////////////////////////
	/**
	 *
	 */
	////////////////////////////////////////////////////////
	private int miValue = 1;
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param iValue int
	 */
	////////////////////////////////////////////////////////
	public void setValue(int iValue)
	{
		if(iValue <= 0)
			throw new IllegalArgumentException("Priority value have to be greater than zero");
		miValue = iValue;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return int
	 */
	////////////////////////////////////////////////////////
	public int getValue()
	{
		return miValue;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return Priority
	 */
	////////////////////////////////////////////////////////
	public Priority getPriority()
	{
		return this;
	}
}
