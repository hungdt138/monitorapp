package com.fss.sql;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Nguyen Truong Giang
 * @version 1.0
 */

public class BatchField
{
	//constants
	public static final int TYPE_STRING = 1;
	public static final int TYPE_INTEGER = 2;
	public static final int TYPE_FLOAT = 3;
	public static final int TYPE_LONG = 4;
	public static final int TYPE_DOUBLE = 5;
	public static final int TYPE_DATE = 6;

	private int fieldType;
	private int fieldIndex;
	private Object fieldValue;

	public BatchField()
	{
	}

	public BatchField(int fieldType, int fieldIndex, Object fieldValue)
	{
		this.fieldType = fieldType;
		this.fieldIndex = fieldIndex;
		this.fieldValue = fieldValue;
	}

	public BatchField(int fieldType, int fieldIndex, long fieldValue)
	{
		this(fieldType,fieldIndex,String.valueOf(fieldValue));
	}

	public BatchField(int fieldType, int fieldIndex, double fieldValue)
	{
		this(fieldType,fieldIndex,String.valueOf(fieldValue));
	}

	public BatchField(int fieldType, int fieldIndex, int fieldValue)
	{
		this(fieldType,fieldIndex,String.valueOf(fieldValue));
	}

	public BatchField(int fieldType, int fieldIndex, float fieldValue)
	{
		this(fieldType,fieldIndex,String.valueOf(fieldValue));
	}

	public Object getFieldValue()
	{
		return fieldValue;
	}

	public void setFieldValue(Object fieldValue)
	{
		this.fieldValue = fieldValue;
	}

	public int getFieldType()
	{
		return fieldType;
	}

	public void setFieldType(int fieldType)
	{
		this.fieldType = fieldType;
	}

	public int getFieldIndex()
	{
		return fieldIndex;
	}

	public void setFieldIndex(int fieldIndex)
	{
		this.fieldIndex = fieldIndex;
	}
}