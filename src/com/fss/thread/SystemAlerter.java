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

public class SystemAlerter extends ManageableThread
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	// SMTP variables
	protected String mstrHost;
	protected String mstrSender;
	protected String mstrRecipient;
	protected String mstrSubjectFormat;
	protected String mstrSendingCase;
	protected String mstrDBUrl;
	protected String mstrDBUserName;
	protected String mstrDBPassword;
	protected int miRemainFileSize;
	protected Vector mvtVolumeInformation;
	protected Vector mvtTablespaceInformation;
	protected Vector mvtTablePartitionInformation;
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
		mstrSendingCase = loadMandatory("SendingCase");
		mstrDBUrl = loadMandatory("DBUrl");
		mstrDBUserName = loadMandatory("DBUserName");
		mstrDBPassword = loadMandatory("DBPassword");
		miRemainFileSize = 0;
		if(StringUtil.nvl(mprtParam.get("RemainFileSize"),"").length() > 0)
			miRemainFileSize = loadUnsignedInteger("RemainFileSize");
		Object obj = mprtParam.get("VolumeCare");
		if(obj != null && obj instanceof Vector)
			mvtVolumeInformation = (Vector)((Vector)obj).clone();
		else
			mvtVolumeInformation = new Vector();
		obj = mprtParam.get("TablespaceCare");
		if(obj != null && obj instanceof Vector)
			mvtTablespaceInformation = (Vector)((Vector)obj).clone();
		else
			mvtTablespaceInformation = new Vector();
		obj = mprtParam.get("PartitionCare");
		if(obj != null && obj instanceof Vector)
			mvtTablePartitionInformation = (Vector)((Vector)obj).clone();
		else
			mvtTablePartitionInformation = new Vector();
		super.fillParameter();
		mbAutoConnectDB = false;
	}
	////////////////////////////////////////////////////////
	public void validateParameter() throws Exception
	{
		super.validateParameter();
		Vector vtUnique = new Vector();
		for(int iIndex = 0;iIndex < mvtVolumeInformation.size();iIndex++)
		{
			Vector vtRow = (Vector)((Vector)mvtVolumeInformation.elementAt(iIndex)).clone();
			if(vtUnique.indexOf((String)vtRow.elementAt(0)) >= 0)
				throw new AppException("FSS-00010","SystemAlerter.validateParameter","VolumeCare.Volume");
			vtUnique.addElement((String)vtRow.elementAt(0));
			loadMandatory("VolumeCare.Volume",(String)vtRow.elementAt(0));
			vtRow.setElementAt(new Integer(loadUnsignedInteger("VolumeCare.WarningValue",(String)vtRow.elementAt(1))),1);
			vtRow.setElementAt(new Integer(loadUnsignedInteger("VolumeCare.AlertValue",(String)vtRow.elementAt(2))),2);
			mvtVolumeInformation.setElementAt(vtRow,iIndex);
		}

		vtUnique = new Vector();
		for(int iIndex = 0;iIndex < mvtTablespaceInformation.size();iIndex++)
		{
			Vector vtRow = (Vector)((Vector)mvtTablespaceInformation.elementAt(iIndex)).clone();
			if(vtUnique.indexOf((String)vtRow.elementAt(0)) >= 0)
				throw new AppException("FSS-00010","SystemAlerter.validateParameter","TablespaceCare.TablespaceName");
			vtUnique.addElement((String)vtRow.elementAt(0));
			loadMandatory("TablespaceCare.TablespaceName",(String)vtRow.elementAt(0));
			vtRow.setElementAt(new Integer(loadUnsignedInteger("TablespaceCare.WarningValue",(String)vtRow.elementAt(1))),1);
			vtRow.setElementAt(new Integer(loadUnsignedInteger("TablespaceCare.AlertValue",(String)vtRow.elementAt(2))),2);
			mvtTablespaceInformation.setElementAt(vtRow,iIndex);
		}

		vtUnique = new Vector();
		for(int iIndex = 0;iIndex < mvtTablePartitionInformation.size();iIndex++)
		{
			Vector vtRow = (Vector)((Vector)mvtTablePartitionInformation.elementAt(iIndex)).clone();
			if(vtUnique.indexOf((String)vtRow.elementAt(0)) >= 0)
				throw new AppException("FSS-00010","SystemAlerter.validateParameter","PartitionCare.TableName");
			vtUnique.addElement((String)vtRow.elementAt(0));
			loadMandatory("PartitionCare.TableName",(String)vtRow.elementAt(0));
			vtRow.setElementAt(new Integer(loadUnsignedInteger("PartitionCare.WarningDistance",(String)vtRow.elementAt(1))),1);
			vtRow.setElementAt(new Integer(loadUnsignedInteger("PartitionCare.AlertDistance",(String)vtRow.elementAt(2))),2);
			mvtTablePartitionInformation.setElementAt(vtRow,iIndex);
		}
	}
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public Vector getParameterDefinition()
	{
		Vector vtReturn = new Vector();
		vtReturn.addElement(createParameterDefinition("Host","",ParameterType.PARAM_TEXTBOX_FILTER,ParameterType.FILTER_REGULAR,"SMTP host"));
		vtReturn.addElement(createParameterDefinition("Sender","",ParameterType.PARAM_TEXTBOX_MAX,"400",""));
		vtReturn.addElement(createParameterDefinition("Recipient","",ParameterType.PARAM_TEXTBOX_MAX,"4000",""));
		vtReturn.addElement(createParameterDefinition("SubjectFormat","",ParameterType.PARAM_TEXTBOX_MAX,"400","Mail subject format, can use $ApplicationName, $IPAddress, $ThreadID, $ThreadName as parameter"));
		Vector vtDefinition = new Vector();
		vtDefinition.addElement("Alway");
		vtDefinition.addElement("When alert or warning generated");
		vtDefinition.addElement("When alert generated only");
		vtReturn.addElement(createParameterDefinition("SendingCase","",ParameterType.PARAM_COMBOBOX,vtDefinition,""));
		vtReturn.addElement(createParameterDefinition("DBUrl","",ParameterType.PARAM_TEXTBOX_MAX,"256","Connection url of database"));
		vtReturn.addElement(createParameterDefinition("DBUserName","",ParameterType.PARAM_TEXTBOX_MAX,"256","DBA user name"));
		vtReturn.addElement(createParameterDefinition("DBPassword","",ParameterType.PARAM_PASSWORD,"100","Password of DBA user name"));
		vtReturn.addElement(createParameterDefinition("RemainFileSize","",ParameterType.PARAM_TEXTBOX_MASK,"9990","Remain size of each data file (In MB)"));
		vtDefinition = new Vector();
		vtDefinition.addElement(createParameterDefinition("Volume","",ParameterType.PARAM_TEXTBOX_MAX,"1024","Path to volume need to care","0"));
		vtDefinition.addElement(createParameterDefinition("WarningValue","",ParameterType.PARAM_TEXTBOX_MASK,"9999999990","When volume available space less than this value (in MB), a warning will be generated","1"));
		vtDefinition.addElement(createParameterDefinition("AlertValue","",ParameterType.PARAM_TEXTBOX_MASK,"9999999990","When volume available space less than this value (in MB), a alert will be generated","2"));
		vtReturn.addElement(createParameterDefinition("VolumeCare","",ParameterType.PARAM_TABLE,vtDefinition,"Contain volume care information"));
		vtDefinition = new Vector();
		vtDefinition.addElement(createParameterDefinition("Tablespace","",ParameterType.PARAM_TEXTBOX_MAX,"1024","Name of tablesapce need to care","0"));
		vtDefinition.addElement(createParameterDefinition("WarningValue","",ParameterType.PARAM_TEXTBOX_MASK,"9999999990","When available space of tablespace less than this value (in MB), a warning will be generated","1"));
		vtDefinition.addElement(createParameterDefinition("AlertValue","",ParameterType.PARAM_TEXTBOX_MASK,"9999999990","When available space of tablespace less than this value (in MB), a alert will be generated","2"));
		vtReturn.addElement(createParameterDefinition("TablespaceCare","",ParameterType.PARAM_TABLE,vtDefinition,"Contain tablespace care information"));
		vtDefinition = new Vector();
		vtDefinition.addElement(createParameterDefinition("TableName","",ParameterType.PARAM_TEXTBOX_MAX,"1024","Name of partition table need to care","0"));
		vtDefinition.addElement(createParameterDefinition("WarningDistance","",ParameterType.PARAM_TEXTBOX_MASK,"9990","When distance between max partition date and current date (in day) less than this value, a warning will be generated","1"));
		vtDefinition.addElement(createParameterDefinition("AlertDistance","",ParameterType.PARAM_TEXTBOX_MASK,"9990","When distance between max partition date and current date (in day) less than this value, a alert will be generated","2"));
		vtReturn.addElement(createParameterDefinition("PartitionCare","",ParameterType.PARAM_TABLE,vtDefinition,"Contain partition care information"));
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
		// Create volume information
		int iLevel = 0;
		StringBuffer bufVolumeInformation = new StringBuffer();
		if(mvtVolumeInformation.size() > 0)
		{
			try
			{
				bufVolumeInformation.append("<table bgcolor=\"#5A8698\" cellspacing=\"1\" cellpadding=\"2\">\n");

				// Title & header
				bufVolumeInformation.append(
					"\t<tr bgcolor=\"#D8E0E8\" style=\"color:#0066CC;font-family:Tahoma,Verdana;font-weight:bold;font-size:12px;\">\n");
				bufVolumeInformation.append("\t\t<td align=\"center\" colspan=\"6\">System volume information</td>\n");
				bufVolumeInformation.append("\t</tr>\n");
				bufVolumeInformation.append(
					"\t<tr bgcolor=\"#D8E0E8\" style=\"color:#271F98;font-family:Tahoma,Verdana;font-weight:bold;font-size:11px;\">\n");
				bufVolumeInformation.append("\t\t<td align=\"center\">Volume</td>\n");
				bufVolumeInformation.append("\t\t<td align=\"center\">Availble</td>\n");
				bufVolumeInformation.append("\t\t<td align=\"center\">Used</td>\n");
				bufVolumeInformation.append("\t\t<td align=\"center\" colspan=\"2\">% Used</td>\n");
				bufVolumeInformation.append("\t\t<td align=\"center\">Note</td>\n");
				bufVolumeInformation.append("\t</tr>\n");

				// Data
				for(int iIndex = 0;iIndex < mvtVolumeInformation.size();iIndex++)
				{
					Vector vtRow = (Vector)mvtVolumeInformation.elementAt(iIndex);
					String strVolumeName = StringUtil.nvl(vtRow.elementAt(0),"");
					int iWarningSize = ((Integer)vtRow.elementAt(1)).intValue();
					int iAlertSize = ((Integer)vtRow.elementAt(2)).intValue();
					long[] lSpaceInfo = getFreeSpace(strVolumeName);
					int iRealSize = (int)(lSpaceInfo[1] / (1024 * 1024));
					int iUsedSize = (int)(lSpaceInfo[0] / (1024 * 1024));
					double dblUsedRate = -1;
					try
					{
						dblUsedRate = (double)lSpaceInfo[0] / (double)(lSpaceInfo[0] + lSpaceInfo[1]);
					}
					catch(Exception e)
					{
					}
					String strClass = "";
					String strNote = "";
					if(iRealSize <= iAlertSize)
					{
						iLevel = 2;
						strClass = "bgcolor=\"#FFBBCC\" style=\"color:#004433;font-size:12px;font-family:Tahoma,Verdana;\"";
						if(iRealSize == 0)
							strNote = "System volume is limited. Please clean up immediately!";
						else
							strNote = "System volume is about to limit. Please clean up immediately!";
					}
					else if(iRealSize <= iWarningSize)
					{
						if(iLevel < 1)
							iLevel = 1;
						strClass = "bgcolor=\"#EEEEAA\" style=\"color:#111155;font-size:12px;font-family:Tahoma,Verdana;\"";
						strNote = "System volume is low. Please prepare to clean up.";
					}
					else
						strClass = "bgcolor=\"#FFFFFF\" style=\"color:#000000;font-size:12px;font-family:Tahoma,Verdana;\"";
					bufVolumeInformation.append("\t<tr " + strClass + ">\n");
					bufVolumeInformation.append("\t\t<td align=\"left\">" + strVolumeName + "</td>\n");
					bufVolumeInformation.append("\t\t<td align=\"right\">" + iRealSize + "MB</td>\n");
					bufVolumeInformation.append("\t\t<td align=\"right\">" + ((lSpaceInfo[0] >= 0) ? String.valueOf(iUsedSize) + "MB" : "") +
												"</td>\n");
					bufVolumeInformation.append("\t\t<td align=\"right\">" + ((lSpaceInfo[0] >= 0) ? StringUtil.format(dblUsedRate,"#,##0.00%") : "") +
												"</td>\n");
					bufVolumeInformation.append("\t\t<td align=\"left\" width=\"100\">\n");
					bufVolumeInformation.append("\t\t\t<table width=\"" + ((lSpaceInfo[0] >= 0) ? StringUtil.format(dblUsedRate,"#,##0.00%") : "") +
												"\" bgcolor=\"#000000\" cellspacing=\"0\" cellpadding=\"0\">\n");
					bufVolumeInformation.append("\t\t\t\t<tr valign=\"middle\"><td height=\"12\"></td></tr>\n");
					bufVolumeInformation.append("\t\t\t</table>\n");
					bufVolumeInformation.append("\t\t</td>\n");
					bufVolumeInformation.append("\t\t<td align=\"left\">" + strNote + "</td>\n");
					bufVolumeInformation.append("\t</tr>\n");
				}
				bufVolumeInformation.append("</table>\n");
			}
			catch(Exception e)
			{
				e.printStackTrace();
				bufVolumeInformation = new StringBuffer();
				bufVolumeInformation.append("Could not get volume information, reason is: " + e.getMessage());
			}
		}

		StringBuffer bufTablespaceInformation = new StringBuffer();
		StringBuffer bufPartitionInformation = new StringBuffer();
		Connection cn = null;
		try
		{
			// Create connection
			cn = Database.getConnection(mstrDBUrl,mstrDBUserName,mstrDBPassword);

			// Create tablespace information
			if(mvtTablespaceInformation.size() > 0)
			{
				// Title & header
				bufTablespaceInformation.append("<table bgcolor=\"#5A8698\" cellspacing=\"1\" cellpadding=\"2\">\n");
				bufTablespaceInformation.append("\t<tr bgcolor=\"#D8E0E8\" style=\"color:#0066CC;font-family:Tahoma,Verdana;font-weight:bold;font-size:12px;\">\n");
				bufTablespaceInformation.append("\t\t<td align=\"center\" colspan=\"6\">Tablespace information</td>\n");
				bufTablespaceInformation.append("\t</tr>\n");
				bufTablespaceInformation.append("\t<tr bgcolor=\"#D8E0E8\" style=\"color:#271F98;font-family:Tahoma,Verdana;font-weight:bold;font-size:11px;\">\n");
				bufTablespaceInformation.append("\t\t<td align=\"center\">Tablespace</td>\n");
				bufTablespaceInformation.append("\t\t<td align=\"center\">Availble</td>\n");
				bufTablespaceInformation.append("\t\t<td align=\"center\">Used</td>\n");
				bufTablespaceInformation.append("\t\t<td align=\"center\" colspan=\"2\">% Used</td>\n");
				bufTablespaceInformation.append("\t\t<td align=\"center\">Note</td>\n");
				bufTablespaceInformation.append("\t</tr>\n");

				// Data
				for(int iIndex = 0;iIndex < mvtTablespaceInformation.size();iIndex++)
				{
					Vector vtRow = (Vector)mvtTablespaceInformation.elementAt(iIndex);
					String strTablespaceName = StringUtil.nvl(vtRow.elementAt(0),"");
					int iWarningSize = ((Integer)vtRow.elementAt(1)).intValue();
					int iAlertSize = ((Integer)vtRow.elementAt(2)).intValue();
					Vector vt = getTBFreeSpace(cn,strTablespaceName,miRemainFileSize);
					for(int iTSIndex = 0;iTSIndex < vt.size();iTSIndex++)
					{
						Vector vtTSRow = (Vector)vt.elementAt(iTSIndex);
						strTablespaceName = StringUtil.nvl(vtTSRow.elementAt(0),"");
						long lFree = Long.parseLong(StringUtil.nvl(vtTSRow.elementAt(1),"0"));
						long lTotal = Long.parseLong(StringUtil.nvl(vtTSRow.elementAt(2),"0"));
						int iRealSize = (int)(lFree / (1024 * 1024));
						int iUsedSize = (int)((lTotal - lFree) / (1024 * 1024));
						double dblUsedRate = -1;
						try
						{
							dblUsedRate = (double)((lTotal - lFree)) / (double)lTotal;
						}
						catch(Exception e)
						{
						}
						String strClass = "";
						String strNote = "";
						if(iRealSize <= iAlertSize)
						{
							iLevel = 2;
							strClass = "bgcolor=\"#FFBBCC\" style=\"color:#004433;font-size:12px;font-family:Tahoma,Verdana;\"";
							if(iRealSize == 0)
								strNote = "Tablespace is limited. Please extend immediately!";
							else
								strNote = "Tablespace is about to limit. Please extend immediately!";
						}
						else if(iRealSize <= iWarningSize)
						{
							if(iLevel < 1)
								iLevel = 1;
							strClass = "bgcolor=\"#EEEEAA\" style=\"color:#111155;font-size:12px;font-family:Tahoma,Verdana;\"";
							strNote = "Tablespace is low. Please prepare to extend.";
						}
						else
							strClass = "bgcolor=\"#FFFFFF\" style=\"color:#000000;font-size:12px;font-family:Tahoma,Verdana;\"";
						bufTablespaceInformation.append("\t<tr " + strClass + ">\n");
						bufTablespaceInformation.append("\t\t<td align=\"left\">" + strTablespaceName + "</td>\n");
						bufTablespaceInformation.append("\t\t<td align=\"right\">" + iRealSize + "MB</td>\n");
						bufTablespaceInformation.append("\t\t<td align=\"right\">" + ((dblUsedRate >= 0)?String.valueOf(iUsedSize) + "MB":"") + "</td>\n");
						bufTablespaceInformation.append("\t\t<td align=\"right\">" + ((dblUsedRate >= 0)?StringUtil.format(dblUsedRate,"#,##0.00%"):"") + "</td>\n");
						bufTablespaceInformation.append("\t\t<td align=\"left\" width=\"100\">\n");
						bufTablespaceInformation.append("\t\t\t<table width=\"" + ((dblUsedRate >= 0)?StringUtil.format(dblUsedRate,"#,##0.00%"):"") + "\" bgcolor=\"#000000\" cellspacing=\"0\" cellpadding=\"0\">\n");
						bufTablespaceInformation.append("\t\t\t\t<tr valign=\"middle\"><td height=\"12\"></td></tr>\n");
						bufTablespaceInformation.append("\t\t\t</table>\n");
						bufTablespaceInformation.append("\t\t</td>\n");
						bufTablespaceInformation.append("\t\t<td align=\"left\">" + strNote + "</td>\n");
						bufTablespaceInformation.append("\t</tr>\n");
					}
				}
				bufTablespaceInformation.append("</table>\n");
			}

			// Create table partition information
			if(mvtTablePartitionInformation.size() > 0)
			{
				// Title & header
				bufPartitionInformation.append("<table bgcolor=\"#5A8698\" cellspacing=\"1\" cellpadding=\"2\">\n");
				bufPartitionInformation.append("\t<tr bgcolor=\"#D8E0E8\" style=\"color:#0066CC;font-family:Tahoma,Verdana;font-weight:bold;font-size:12px;\">\n");
				bufPartitionInformation.append("\t\t<td align=\"center\" colspan=3>Partition information</td>\n");
				bufPartitionInformation.append("\t</tr>\n");
				bufPartitionInformation.append("\t<tr bgcolor=\"#D8E0E8\" style=\"color:#271F98;font-family:Tahoma,Verdana;font-weight:bold;font-size:11px;\">\n");
				bufPartitionInformation.append("\t\t<td align=\"center\">Table</td>\n");
				bufPartitionInformation.append("\t\t<td align=\"center\">Max partition date</td>\n");
				bufPartitionInformation.append("\t\t<td align=\"center\">Note</td>\n");
				bufPartitionInformation.append("\t</tr>\n");

				// Data
				for(int iIndex = 0;iIndex < mvtTablePartitionInformation.size();iIndex++)
				{
					Vector vtRow = (Vector)mvtTablePartitionInformation.elementAt(iIndex);
					String strPartitionName = StringUtil.nvl(vtRow.elementAt(0),"");
					int iWarningDistance = ((Integer)vtRow.elementAt(1)).intValue();
					int iAlertDistance = ((Integer)vtRow.elementAt(2)).intValue();
					Vector vtPartition = getDistanceFromLastTablePartition(cn,strPartitionName);
					for(int iPIndex = 0;iPIndex < vtPartition.size();iPIndex++)
					{
						Vector vtPRow = (Vector)vtPartition.elementAt(iPIndex);
						strPartitionName = StringUtil.nvl(vtPRow.elementAt(0),"");
						String strLastPartition = StringUtil.nvl(vtPRow.elementAt(1),"");
						int iRealDistance = ((Integer)vtPRow.elementAt(2)).intValue();
						String strClass = "";
						String strNote = "";

						if(iRealDistance <= iAlertDistance)
						{
							iLevel = 2;
							strClass = "bgcolor=\"#FFBBCC\" style=\"color:#004433;font-size:12px;font-family:Tahoma,Verdana;\"";
							if(iRealDistance <= 0)
								strNote = "Current date is equals or greater than last partition date. Please create new partition immediately!";
							else
								strNote = "Last partition date is very close to current date. Please create new partition immediately!";
						}
						else if(iRealDistance <= iWarningDistance)
						{
							if(iLevel < 1)
								iLevel = 1;
							strClass = "bgcolor=\"#EEEEAA\" style=\"color:#111155;font-size:12px;font-family:Tahoma,Verdana;\"";
							strNote = "Last partition date is quite close to current date. Please prepare to create new partition.";
						}
						else
							strClass = "bgcolor=\"#FFFFFF\" style=\"color:#000000;font-size:12px;font-family:Tahoma,Verdana;\"";
						bufPartitionInformation.append("\t<tr " + strClass + ">\n");
						bufPartitionInformation.append("\t\t<td align=\"left\">" + strPartitionName + "</td>\n");
						bufPartitionInformation.append("\t\t<td align=\"right\">" + strLastPartition + "</td>\n");
						bufPartitionInformation.append("\t\t<td align=\"left\">" + strNote + "</td>\n");
						bufPartitionInformation.append("\t</tr>\n");
					}
				}
				bufPartitionInformation.append("</table>\n");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			bufTablespaceInformation = new StringBuffer();
			bufPartitionInformation = new StringBuffer();
			bufTablespaceInformation.append("Could not get tablespace & partition information, reason is: " + e.getMessage());
		}
		finally
		{
			Database.closeObject(cn);
		}

		if(mstrSendingCase.startsWith("Alway") ||
		   (iLevel > 0 && mstrSendingCase.startsWith("When alert or warning generated")) ||
		   (iLevel > 1 && mstrSendingCase.startsWith("When alert generated only")))
		{
			// Log start
			logMonitor("Start sending message to user");

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
				if(iLevel == 1)
					strSubject += " (warning)";
				else if(iLevel == 2)
					strSubject += " (alert)";
				SimpleSMTPHeader header = new SimpleSMTPHeader(mstrSender,mstrRecipient,strSubject);
				header.addHeaderField("Content-Type","text/html;charset=\"utf-8\"");
				Writer writer = client.sendMessageData();
				if(writer == null)
					throw new Exception("Sending data failed");

				writer.write(header.toString());
				writer.write("<html>\n");
				if(bufVolumeInformation.length() > 0)
				{
					writer.write(bufVolumeInformation.toString());
					writer.write("<br>\n");
				}
				if(bufTablespaceInformation.length() > 0)
				{
					writer.write(bufTablespaceInformation.toString());
					writer.write("<br>\n");
				}
				if(bufPartitionInformation.length() > 0)
				{
					writer.write(bufPartitionInformation.toString());
					writer.write("<br>\n");
				}
				writer.write("</html>\n");
				writer.close();
				if(!client.completePendingCommand())
					throw new Exception("Sending data failed");

				// Log out
				client.logout();
			}
			catch(Exception e)
			{
				logMonitor("Error occured: " + e.getMessage());
				e.printStackTrace();
				sendError(e);
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

			// Log completed
			logMonitor("Sending message to user completed");
		}
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
	 * @param cn Connection
	 * @param strTableSpaceName String
	 * @param iRemainFileSize int
	 * @return Vector
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public static Vector getTBFreeSpace(Connection cn,String strTableSpaceName,int iRemainFileSize) throws Exception
	{
		String str = "SELECT TS.TABLESPACE_NAME,SUM(NVL(FS.BYTES,0))-" +
					 String.valueOf(iRemainFileSize) + "*1024*1024*" +
					 "(SELECT COUNT(*) FROM GV$DATAFILE,GV$TABLESPACE" +
					 " WHERE GV$DATAFILE.TS#=GV$TABLESPACE.TS#" +
					 " AND GV$TABLESPACE.NAME=TS.TABLESPACE_NAME)," +
					 "NVL((SELECT SUM(BYTES) FROM DBA_DATA_FILES WHERE" +
					 " TABLESPACE_NAME=TS.TABLESPACE_NAME),0)" +
					 " FROM USER_TABLESPACES TS,USER_FREE_SPACE FS" +
					 " WHERE TS.TABLESPACE_NAME=FS.TABLESPACE_NAME(+)" +
					 " AND STATUS NOT IN ('OFFLINE','READ ONLY')" +
					 " AND UPPER(TS.TABLESPACE_NAME) LIKE UPPER('" + strTableSpaceName + "')" +
					 " GROUP BY TS.TABLESPACE_NAME";
		return Database.executeQuery(cn,str);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param cn Connection
	 * @param strTableName String
	 * @return Vector
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public static Vector getDistanceFromLastTablePartition(Connection cn,String strTableName) throws Exception
	{
		Statement stmt = null;
		ResultSet rs = null;
		Vector vtReturn = new Vector();
		try
		{
			// Get last partition high value
			String strSQL = "SELECT TP.TABLE_NAME,HIGH_VALUE FROM USER_TAB_PARTITIONS TP" +
							" WHERE PARTITION_POSITION=(SELECT MAX(PARTITION_POSITION)" +
							" FROM USER_TAB_PARTITIONS TP1 WHERE TP1.TABLE_NAME=TP.TABLE_NAME)" +
							" AND UPPER(TP.TABLE_NAME) LIKE UPPER('" + strTableName + "')";
			stmt = cn.createStatement();
			rs = stmt.executeQuery(strSQL);
			while(rs.next())
			{
				Vector vtRow = new Vector();
				vtRow.addElement(rs.getString(2));
				vtRow.addElement(rs.getString(1));
				vtReturn.addElement(vtRow);
			}
			Database.closeObject(rs);

			for(int iIndex = 0;iIndex < vtReturn.size();iIndex++)
			{
				// Get distance
				Vector vtRow = (Vector)vtReturn.elementAt(iIndex);
				strSQL = "SELECT TO_CHAR(" + (String)vtRow.elementAt(0) + " - 1,'DD/MM/YYYY')," + (String)vtRow.elementAt(0) + "-TRUNC(SYSDATE) FROM DUAL";
				rs = stmt.executeQuery(strSQL);
				rs.next();
				vtRow.removeElementAt(0);
				vtRow.addElement(rs.getString(1));
				vtRow.addElement(new Integer(rs.getInt(2)));
				Database.closeObject(rs);
			}

			return vtReturn;
		}
		finally
		{
			Database.closeObject(rs);
			Database.closeObject(stmt);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param path String
	 * @return long[]
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public static long[] getFreeSpace(String path) throws Exception
	{
		if(System.getProperty("os.name").startsWith("Windows"))
			return getFreeSpaceOnWindows(path);
		else if(System.getProperty("os.name").startsWith("Linux"))
			return getFreeSpaceOnLinux(path);
		else if(System.getProperty("os.name").startsWith("SunOS"))
			return getFreeSpaceOnSunOS(path);
		throw new UnsupportedOperationException("The method getFreeSpace(String path) has not been implemented for this operating system.");
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param path String
	 * @return long[]
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	private static long[] getFreeSpaceOnWindows(String path) throws Exception
	{
		long bytesFree = -1;
		File script = new File(System.getProperty("java.io.tmpdir"),"script.bat");
		PrintWriter writer = new PrintWriter(new FileWriter(script,false));
		writer.println("dir \"" + path + "\"");
		writer.close();

		// get the output from running the .bat file
		Process p = Runtime.getRuntime().exec(script.getAbsolutePath());
		InputStream reader = new BufferedInputStream(p.getInputStream());
		StringBuffer buffer = new StringBuffer();
		for(;;)
		{
			int c = reader.read();
			if(c == -1)
				break;
			buffer.append((char)c);
		}
		String outputText = buffer.toString();
		reader.close();

		// parse the output text for the bytes free info
		StringTokenizer tokenizer = new StringTokenizer(outputText,"\n");
		while(tokenizer.hasMoreTokens())
		{
			String line = tokenizer.nextToken().trim();
			if(line.endsWith("bytes free"))
			{
				tokenizer = new StringTokenizer(line," ");
				tokenizer.nextToken();
				tokenizer.nextToken();
				bytesFree = Long.parseLong(tokenizer.nextToken().replaceAll(",",""));
			}
		}
		long[] lReturn = new long[2];
		lReturn[0] = -1;
		lReturn[1] = bytesFree;
		return lReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param path String
	 * @return long[]
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	private static long[] getFreeSpaceOnLinux(String path) throws Exception
	{
		long bytesFree = -1;
		long bytesUsed = -1;
		Process p = Runtime.getRuntime().exec("df " + "/" + path);
		InputStream reader = new BufferedInputStream(p.getInputStream());
		StringBuffer buffer = new StringBuffer();
		for(;;)
		{
			int c = reader.read();
			if(c == -1)
				break;
			buffer.append((char)c);
		}
		String outputText = buffer.toString();
		reader.close();

		// Parse the output text for the bytes free info
		StringTokenizer tokenizer = new StringTokenizer(outputText,"\n");
		tokenizer.nextToken();
		if(tokenizer.hasMoreTokens())
		{
			String line2 = tokenizer.nextToken();
			StringTokenizer tokenizer2 = new StringTokenizer(line2," ");
			if(tokenizer2.countTokens() >= 4)
			{
				tokenizer2.nextToken();
				tokenizer2.nextToken();
				bytesUsed = Long.parseLong(tokenizer2.nextToken()) * 1024;
				bytesFree = Long.parseLong(tokenizer2.nextToken()) * 1024;
				long[] lReturn = new long[2];
				lReturn[0] = bytesUsed;
				lReturn[1] = bytesFree;
				return lReturn;
			}
		}
		throw new Exception("Can not get the free space of " + path + " path");
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param path String
	 * @return long[]
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	private static long[] getFreeSpaceOnSunOS(String path) throws Exception
	{
		long bytesFree = -1;
		long bytesUsed = -1;
		Process p = Runtime.getRuntime().exec("df -k " + "/" + path);
		InputStream reader = new BufferedInputStream(p.getInputStream());
		StringBuffer buffer = new StringBuffer();
		for(;;)
		{
			int c = reader.read();
			if(c == -1)
				break;
			buffer.append((char)c);
		}
		String outputText = buffer.toString();
		reader.close();

		// Parse the output text for the bytes free info
		StringTokenizer tokenizer = new StringTokenizer(outputText,"\n");
		tokenizer.nextToken();
		if(tokenizer.hasMoreTokens())
		{
			String line2 = tokenizer.nextToken();
			StringTokenizer tokenizer2 = new StringTokenizer(line2," ");
			if(tokenizer2.countTokens() >= 4)
			{
				tokenizer2.nextToken();
				tokenizer2.nextToken();
				bytesUsed = Long.parseLong(tokenizer2.nextToken()) * 1024;
				bytesFree = Long.parseLong(tokenizer2.nextToken()) * 1024;
				long[] lReturn = new long[2];
				lReturn[0] = bytesUsed;
				lReturn[1] = bytesFree;
				return lReturn;
			}
		}
		throw new Exception("Can not get the free space of " + path + " path");
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
