package com.fss.thread;

import java.net.*;
import java.util.*;

import com.fss.util.*;
import com.fss.server.*;
import com.fss.server.telnet.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class TelnetServer extends ManageableThread
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	protected ServerSocket msck;
	protected int miPort;
	protected String mstrPromptFormat;
	protected int miMaxConnection;
	protected int miExpireDuration;
	protected long mlMinSessionSeqVal;
	protected long mlMaxSessionSeqVal;
	protected long mlExpectedSessionSeq;
	protected Vector mvtChannel = new Vector();
	protected Thread mthrAccept;
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public Vector getParameterDefinition()
	{
		Vector vtReturn = new Vector();

		vtReturn.addElement(createParameterDefinition("Port","",ParameterType.PARAM_TEXTBOX_MASK,"99990",""));
		vtReturn.addElement(createParameterDefinition("PromptFormat","",ParameterType.PARAM_TEXTBOX_MAX,"50","Used to display prompt, can use $UserID, $UserName as parameter"));
		vtReturn.addElement(createParameterDefinition("MaxConnection","",ParameterType.PARAM_TEXTBOX_MASK,"99990",""));
		vtReturn.addElement(createParameterDefinition("ExpireDuration","",ParameterType.PARAM_TEXTBOX_MASK,"9999999990",""));
		vtReturn.addElement(createParameterDefinition("MinSessionSeqVal","",ParameterType.PARAM_TEXTBOX_MASK,"9999999990",""));
		vtReturn.addElement(createParameterDefinition("MaxSessionSeqVal","",ParameterType.PARAM_TEXTBOX_MASK,"9999999990",""));
		vtReturn.addElement(createParameterDefinition("ExpectedSessionSeq","",ParameterType.PARAM_TEXTBOX_MASK,"9999999990",""));
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
	/**
	 *
	 * @throws AppException
	 */
	////////////////////////////////////////////////////////
	public void fillParameter() throws AppException
	{
		miPort = loadUnsignedInteger("Port");
		mstrPromptFormat = loadMandatory("PromptFormat");
		miMaxConnection = loadUnsignedInteger("MaxConnection");
		miExpireDuration = loadUnsignedInteger("ExpireDuration");
		mlMinSessionSeqVal = loadUnsignedLong("MinSessionSeqVal");
		mlMaxSessionSeqVal = loadUnsignedLong("MaxSessionSeqVal");
		mlExpectedSessionSeq = loadUnsignedLong("ExpectedSessionSeq");
		super.fillParameter();
		mbAutoConnectDB = false;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void validateParameter() throws Exception
	{
		super.validateParameter();
		if(mlMaxSessionSeqVal < mlMinSessionSeqVal)
			throw new AppException("Value of 'MaxSessionSeqVal' can not be smaller than value of 'MinSessionSeqVal'","ASNConverter.validateParameter","MinSessionSeqVal");
		if(mlExpectedSessionSeq < mlMinSessionSeqVal)
			throw new AppException("Value of 'ExpectedSessionSeq' can not be smaller than value of 'MinSessionSeqVal'","ASNConverter.validateParameter","MinSessionSeqVal");
		if(mlExpectedSessionSeq > mlMaxSessionSeqVal)
			throw new AppException("Value of 'ExpectedSessionSeq' can not be greater than value of 'MaxSessionSeqVal'","ASNConverter.validateParameter","MaxSeqVal");
		if(miExpireDuration <= 0)
			throw new Exception("ExpireDuration must greater than zero");
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void destroy()
	{
		try
		{
			mthrAccept.stop();
		}
		catch(Exception e)
		{
		}
		super.destroy();
	}
	////////////////////////////////////////////////////////
	/**
	 * Run thread
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void processSession() throws Exception
	{
		// Open port to listen to
		open(miPort);

		// Start listener thread
		mthrAccept = new Thread()
		{
			public void run()
			{
				try
				{
					while(msck != null)
					{
						// Accept a new connection
						Socket sck = msck.accept();
						sck.setSoLinger(true,0);
						sck.setSoTimeout(miExpireDuration * 1000);
						if(mvtChannel.size() >= miMaxConnection)
						{
							try
							{
								String strError = "Number of opened connection (" + mvtChannel.size() +
												  ") exceeded the maximum connection (" + miMaxConnection + ")\r\n";
								TelnetCommander channel = prepareChannel(sck);
								channel.close(strError);
							}
							catch(Exception e)
							{
								e.printStackTrace();
							}
							finally
							{
								try
								{
									sck.close();
								}
								catch(Exception e)
								{
								}
							}
						}
						else
						{
							// Prepare and run channel for socket
							TelnetCommander channel = prepareChannel(sck);
							addChannel(channel);
							new Thread(channel).start();
						}
					}
				}
				catch(SocketException e) // Easy to understand
				{
				}
				catch(Exception e)
				{
					e.printStackTrace();
					logMonitor("Error occured: " + e.getMessage(),true);
				}
				finally
				{
					close();
				}
			}
		};
		mthrAccept.start();

		// Wait stop command
		try
		{
			while(miThreadCommand != ThreadConstant.THREAD_STOP &&
				  msck != null && !msck.isClosed() &&
				  mthrAccept != null && mthrAccept.isAlive())
			{
				// Fill log file
				fillLogFile();

				// Check expire
				long lCurrentTime = System.currentTimeMillis();
				for(int iIndex = 0;iIndex < mvtChannel.size();iIndex++)
				{
					TelnetCommander channel = (TelnetCommander)mvtChannel.elementAt(iIndex);
					if(channel.mlExpire > 0 && lCurrentTime > channel.mlExpire)
						channel.close("Session expired");
				}

				// Sleep
				for(int iIndex = 0;iIndex < miDelayTime && miThreadCommand != ThreadConstant.THREAD_STOP;iIndex++)
					Thread.sleep(1000); // Time unit is second
			}
		}
		catch(Exception e)
		{
			logMonitor("Error occured: " + e.getMessage(),true);
			e.printStackTrace();
		}
		finally
		{
			// Stop accept
			close();

			// Wait thread accept stop
			try
			{
				mthrAccept.join();
			}
			catch(Exception e)
			{
			}

			// Wait all channel stop
			try
			{
				long lExpire = System.currentTimeMillis() + 30000;
				while(System.currentTimeMillis() < lExpire &&
					  mvtChannel.size() > 0)
				{
					TelnetCommander channel = (TelnetCommander)mvtChannel.elementAt(0);
					channel.close("Server stopped");
				}
			}
			catch(Exception e)
			{
			}
			mvtChannel.removeAllElements();
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param iPort int
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void open(int iPort) throws Exception
	{
		msck = new ServerSocket(iPort);
	}
	////////////////////////////////////////////////////////
	/**
	 * Close gateway
	 */
	////////////////////////////////////////////////////////
	public void close()
	{
		try
		{
			msck.close();
		}
		catch(Exception e)
		{
		}
		msck = null;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param sck Socket
	 * @throws Exception
	 * @return MessageChannel
	 */
	////////////////////////////////////////////////////////
	protected TelnetCommander prepareChannel(Socket sck) throws Exception
	{
		sck.setSoTimeout(0);
		SystemPrintStream sout = new SystemPrintStream(sck.getOutputStream());
		TelnetInputStream sin = new TelnetInputStream(sck.getInputStream(),sout);
		TelnetCommander commander = new TelnetCommander(this,sin,sout,sout,sck);
		sin.setSession(commander);
		return commander;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param channel MessageChannel
	 */
	////////////////////////////////////////////////////////
	public void addChannel(TelnetCommander channel)
	{
		mvtChannel.addElement(channel);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param channel MessageChannel
	 */
	////////////////////////////////////////////////////////
	public void removeChannel(TelnetCommander channel)
	{
		mvtChannel.removeElement(channel);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return Vector
	 */
	////////////////////////////////////////////////////////
	public Vector getChannelList()
	{
		return mvtChannel;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return int
	 */
	////////////////////////////////////////////////////////
	public int getExpireDuration()
	{
		return miExpireDuration;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	public String getPromptFormat()
	{
		return mstrPromptFormat;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public String generateSessionID() throws Exception
	{
		// Get current seq
		if(mlExpectedSessionSeq < mlMinSessionSeqVal)
			mlExpectedSessionSeq = mlMinSessionSeqVal;
		if(mlExpectedSessionSeq > mlMaxSessionSeqVal)
			mlExpectedSessionSeq = mlMaxSessionSeqVal;
		long lReturn = mlExpectedSessionSeq;

		// Increase expected seq
		if(mlExpectedSessionSeq >= mlMaxSessionSeqVal)
			mlExpectedSessionSeq = mlMinSessionSeqVal;
		else
			mlExpectedSessionSeq++;
		mprtParam.put("ExpectedSessionSeq",String.valueOf(mlExpectedSessionSeq));
		storeConfig();
		return String.valueOf(lReturn);
	}
}
