package com.fss.thread;

import java.io.*;
import java.sql.*;
import java.util.*;

import com.fss.sql.*;
import com.fss.util.*;

import org.apache.commons.net.smtp.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class SMTPAlerter extends ManageableThread
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	// SMTP variables
	protected String mstrHost;
	protected String mstrSender;
	protected String mstrRecipient;
	protected String mstrSubjectFormat;
	protected int miBatchSize;
	protected int miStoreDate;

	// Common used variables
	protected int miSuccessCount;

	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public void fillParameter() throws AppException
	{
		// Fill parameter
		mstrHost = loadMandatory("Host");
		mstrSender = loadMandatory("Sender");
		mstrRecipient = loadMandatory("Recipient");
		mstrRecipient = StringUtil.replaceAll(mstrRecipient,"'","");
		mstrRecipient = StringUtil.replaceAll(mstrRecipient,";",",");
		mstrSubjectFormat = loadMandatory("SubjectFormat");
		miBatchSize = loadUnsignedInteger("BatchSize");
		miStoreDate = loadUnsignedInteger("StoreDate");
		super.fillParameter();
		mbAutoConnectDB = true;
	}
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public Vector getParameterDefinition()
	{
		Vector vtReturn = new Vector();

		vtReturn.addElement(createParameterDefinition("Host","",ParameterType.PARAM_TEXTBOX_FILTER,ParameterType.FILTER_REGULAR,""));
		vtReturn.addElement(createParameterDefinition("Sender","",ParameterType.PARAM_TEXTBOX_MAX,"400",""));
		vtReturn.addElement(createParameterDefinition("Recipient","",ParameterType.PARAM_TEXTBOX_MAX,"4000",""));
		vtReturn.addElement(createParameterDefinition("SubjectFormat","",ParameterType.PARAM_TEXTBOX_MAX,"400","Mail subject format, can use $ApplicationName, $IPAddress, $ThreadID, $ThreadName as parameter"));
		vtReturn.addElement(createParameterDefinition("BatchSize","",ParameterType.PARAM_TEXTBOX_MASK,"99990",""));
		vtReturn.addElement(createParameterDefinition("StoreDate","",ParameterType.PARAM_TEXTBOX_MASK,"99990",""));
		vtReturn.addAll(super.getParameterDefinition());

		for(int iIndex = vtReturn.size() - 1;iIndex >= 0;iIndex--)
		{
			String strParameterName = (String)((Vector)vtReturn.elementAt(iIndex)).elementAt(0);
			if(strParameterName.equals("ConnectDB"))
				vtReturn.removeElementAt(iIndex);
		}

		return vtReturn;
	}
	////////////////////////////////////////////////////////
	public void beforeSession() throws Exception
	{
		try
		{
			super.beforeSession();
		}
		catch(Exception e)
		{
			sendError(e);
			throw e;
		}
	}
	////////////////////////////////////////////////////////
	public void processSession() throws Exception
	{
		// Delete unused
		String strSQL = "DELETE THREAD_SMTP_BATCH WHERE NVL(STATUS,0)>0 AND" +
						" SYSDATE-PROCESS_DATE>" + String.valueOf(miStoreDate);
		Statement stmt = mcnMain.createStatement();
		stmt.executeUpdate(strSQL);

		// List of batch to sent
		strSQL = "SELECT BATCH_ID FROM THREAD_SMTP_BATCH B WHERE NVL(STATUS,0)=0" +
				 " AND EXISTS (SELECT * FROM THREAD_SMTP_QUEUE Q WHERE Q.BATCH_ID=B.BATCH_ID)";
		ResultSet rs = stmt.executeQuery(strSQL);
		Vector vtBatch = new Vector();
		while(rs.next())
			vtBatch.addElement(StringUtil.nvl(rs.getString(1),""));
		rs.close();
		if(vtBatch.size() <= 0)
			return;

		// Log start
		logMonitor("Start sending message to user");

		// Send all mail
		for(int iBatchIndex = 0;iBatchIndex < vtBatch.size();iBatchIndex++)
		{
			// Create transaction
			mcnMain.setAutoCommit(false);

			// Update batch
			String strBatchID = (String)vtBatch.elementAt(iBatchIndex);
			strSQL = "UPDATE THREAD_SMTP_BATCH SET PROCESS_DATE=SYSDATE," +
					 "RECIPIENT='" + mstrRecipient + "',STATUS=1" +
					 " WHERE BATCH_ID=" + strBatchID;
			stmt.executeUpdate(strSQL);

			// Get list of message to send
			strSQL = "SELECT SOURCE,MESSAGE,COUNT(*)," +
					 "TO_CHAR(MIN(MESSAGE_DATE),'" + Global.FORMAT_DB_DATE_TIME + "')" +
					 " FROM THREAD_SMTP_QUEUE" +
					 " WHERE BATCH_ID=" + strBatchID +
					 " GROUP BY SOURCE,MESSAGE";
			rs = stmt.executeQuery(strSQL);
			Vector vtData = Database.convertToVector(rs);
			rs.close();

			SMTPClient client = null;
			try
			{
				// Connect to smtp server
				client = new SMTPClient();
				client.connect(mstrHost);
				if(!SMTPReply.isPositiveCompletion(client.getReplyCode()))
					throw new Exception("SMTP server refused connection");
				if(!client.login())
					throw new Exception("Not loged in");

				// Create new mail
				if(!client.setSender(mstrSender))
					throw new Exception("Sender not found");
				String[] strRecipientList = StringUtil.toStringArray(mstrRecipient,",");
				int iRecipientCount = 0;
				for(int iIndex = 0;iIndex < strRecipientList.length;iIndex++)
				{
					if(client.addRecipient(strRecipientList[iIndex]))
						iRecipientCount++;
					else
						logMonitor("Could not use '" + strRecipientList[iIndex] + "' as a receipient");
				}
				if(iRecipientCount <= 0)
					throw new Exception("No recipient added");

				// Format & send mail message
				String strSubject = formatSubject();
				SimpleSMTPHeader header = new SimpleSMTPHeader(mstrSender,mstrRecipient,strSubject);
				Writer writer = client.sendMessageData();
				if(writer == null)
					throw new Exception("Sending data failed");
				writer.write(header.toString());
				String strThreadName = null;
				writer.write("Dear All,\r\n\tThis message contains error information of " + Global.APP_NAME +
							 " which was sent automaticcally by " + mstrThreadName + "\r\n");
				for(int iIndex = 0;iIndex < vtData.size();iIndex++)
				{
					Vector vtRowData = (Vector)vtData.elementAt(iIndex);
					if(!vtRowData.elementAt(0).equals(strThreadName))
					{
						strThreadName = (String)vtRowData.elementAt(0);
						writer.write("\r\n" + strThreadName);
					}
					writer.write("\r\n\tError description: " + (String)vtRowData.elementAt(1));
					writer.write("\r\n\t\tOccurrence times: " + (String)vtRowData.elementAt(2));
					writer.write("\r\n\t\tFirst occurrence: " + (String)vtRowData.elementAt(3));
				}
				writer.write("\r\n\r\nBest regards,");
				writer.close();
				if(!client.completePendingCommand())
					throw new Exception("Sending data failed");

				// Log out
				client.logout();

				// Commit transaction
				mcnMain.commit();
			}
			catch(Exception e)
			{
				sendError(e);
				throw e;
			}
			finally
			{
				try
				{
					client.disconnect();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		// Log completed
		logMonitor("Sending message to user completed");
	}
	////////////////////////////////////////////////////////
	public void sendError(Exception e)
	{
		SMTPClient client = null;
		try
		{
			// Connect to smtp server
			client = new SMTPClient();
			client.connect(mstrHost);
			if(!SMTPReply.isPositiveCompletion(client.getReplyCode()))
				throw new Exception("SMTP server refused connection");
			if(!client.login())
				throw new Exception("Not loged in");

			// Create new mail
			if(!client.setSender(mstrSender))
				throw new Exception("Sender not found");
			String[] strRecipientList = StringUtil.toStringArray(mstrRecipient,",");
			int iRecipientCount = 0;
			for(int iIndex = 0;iIndex < strRecipientList.length;iIndex++)
			{
				if(client.addRecipient(strRecipientList[iIndex]))
					iRecipientCount++;
				else
					logMonitor("Could not use '" + strRecipientList[iIndex] + "' as a receipient");
			}
			if(iRecipientCount <= 0)
				throw new Exception("No recipient added");

			// Format & send mail message
			SimpleSMTPHeader header = new SimpleSMTPHeader(mstrSender,mstrRecipient,"Alert from " + Global.APP_NAME);
			Writer writer = client.sendMessageData();
			if(writer == null)
				throw new Exception("Sending data failed");
			writer.write(header.toString());
			writer.write("Dear All,\r\n\tThis message contains error information of " + Global.APP_NAME +
						 " which was sent automaticcally by " + mstrThreadName + "\r\n");
			writer.write("\r\n" + mstrThreadName);
			writer.write("\r\n\tError description: " + e.getMessage());
			writer.write("\r\n\r\nBest regards,");
			writer.close();
			if(!client.completePendingCommand())
				throw new Exception("Sending data failed");

			// Log out
			client.logout();
		}
		catch(Exception e1)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				client.disconnect();
			}
			catch(Exception e1)
			{
				e1.printStackTrace();
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public String formatSubject() throws Exception
	{
		String strSubject = mstrSubjectFormat;
		strSubject = StringUtil.replaceAllIgnoreCase(strSubject,"$ApplicationName",Global.APP_NAME);
		strSubject = StringUtil.replaceAllIgnoreCase(strSubject,"$IPAddress",
			java.net.Inet4Address.getLocalHost().getCanonicalHostName() +
			"[" + java.net.Inet4Address.getLocalHost().getHostAddress() + "]");
		strSubject = StringUtil.replaceAllIgnoreCase(strSubject,"$ThreadID",getThreadID());
		strSubject = StringUtil.replaceAllIgnoreCase(strSubject,"$ThreadName",getThreadName());
		return strSubject;
	}
}
