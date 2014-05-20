package com.fss.sql;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Nguyen Truong Giang
 * @version 1.0
 */

public class BatchError
{
	private int errorRow = -1;
	private String errorMessage = null;
	private Object errorData = null;

	public BatchError()
	{
	}

	public BatchError(int errorRow, String errorMessage, Object errorData)
	{
		this.errorRow = errorRow;
		this.errorMessage = errorMessage;
		this.errorData = errorData;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public String getFullMessage()
	{
		return "Line: " + this.errorRow + ", Error: " + this.errorMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public int getErrorRow()
	{
		return errorRow;
	}

	public void setErrorRow(int errorRow)
	{
		this.errorRow = errorRow;
	}

	public Object getErrorData()
	{
		return errorData;
	}

	public void setErrorData(Object errorData)
	{
		this.errorData = errorData;
	}
}