
/*---------------------------------------------------------------------------
 * Copyright (C) 1999-2003 Dallas Semiconductor Corporation, All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY,  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DALLAS SEMICONDUCTOR BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dallas Semiconductor
 * shall not be used except as stated in the Dallas Semiconductor
 * Branding Policy.
 *---------------------------------------------------------------------------
 */

package com.fss.server.ftp;

import com.fss.server.*;
import java.io.*;
import java.net.*;
import java.util.*;


/**
 * This class encapsulates all functionality for an FTP session.
 * The FTP server listens to port 21 for connection requests.
 * When someone attempts to start an FTP session, they contact
 * port 21.  The server spins off a session (an instance of this
 * class) to handle that session.  The session allows you to log
 * into the system, then handles the communication between that
 * remote computer and the TINI system.  The FTP server and
 * sessions are designed to be multi-threaded, allowing multiple
 * simultaneous FTP connections.
 *
 * NOTE: We fully support RFC 2428 (FTP Extensions for IPv6 and NATs).
 *       We don't support the LPSV of RFC 1639 (to save space). Legacy
 *       IPv6 clients should either be upgraded to support EPSV or use
 *       RFC 1639 active mode (LPRT command).
 *
 * List of FTP return codes:
 * 120 Service ready in nnn minutes
 * 125 Data Connection already opening, transfer starting
 * 150 File Status OK, about to open data connection
 * 200 OK
 * 202 Command not implemented
 * 211 System status, or help reply
 * 212 Directory status
 * 213 File Status
 * 214 Help Message
 * 215 NAME system type
 * 220 Service ready for new user
 * 221 Service closing control connection
 * 225 Data connection open , no transfer in progress
 * 226 Closing data connection
 * 227 Entering passive mode (h1 h2 h3 h4 p1 p2)
 * 228 Entering long passive mode (af, hal, h1, h2, h3, h4 ..., pal, p1, p2 ...)
 * 229 Entering extended passive mode (|||p|)
 * 230 User logged in proceed
 * 250 Requested file action OK completed
 * 257 PATHNAME created
 * 331 User name OK, need password
 * 332 Need account for login
 * 350 Requested action pending further info
 * 421 Service not available, closing control connection
 * 425 Can't open data connection
 * 426 Connection closed, transfer aborted
 * 450 Req file action not taken
 * 451 Req action aborted - local error
 * 452 Req action not taken (out of memory)
 * 500 Syntax
 * 501 Syntax in parameters or arguments
 * 502 Command not implemented
 * 503 Bad sequence of commands
 * 504 Command not implemented for that parameter
 * 521 Supported address families are (af1, af2 ...)
 * 522 Network protocol not supported
 * 530 Not logged in
 * 532 Need acct for storing files
 * 550 Requested action not taken (file not found, no access, etc.)
 */
public class FTPSession
   extends Session
{
   Socket socket;       // Command socket.
   Socket datasocket;   // Transient datasocket.  For this session, the normal in, out
						// and err are the transient data streams, NOT the command streams!


   private ServerSocket pasvSocket = null; // We need to make sure this thing is always shut down.

   PrintStream         cmdOut;            // Command Port output
   FTPInputStream      cmdIn;             // Command Port input
   boolean             ASCII    = true;   // MODE of transmission
   private String      lastRNFR = "";     // tells if last command was RNFR
   private InetAddress dataSocketIP;      // Transient datasocket IP.  (Used with PORT commands.)
   private int         dataSocketPort;
   private boolean     loggedIn = false;
   private boolean     fastNAT  = false;  // Entered upon EPSV ALL, see RFC 2428


   private static final String C200_SUCCESSFUL         = "200 Command successful.\r\n";
   private static final String LOGGED_IN_A             = "230 User ";
   private static final String LOGGED_IN_B             = " logged in.\r\n";
   private static final String PASS_REQUIRED           = "331 Password Required for ";
   private static final String OPENING_LIST_CONNECTION = "150 Opening ASCII mode data connection for file list.\r\n";
   private static final String CLOSING_DATA_CONNECTION = "226 Closing data connection.\r\n";
   private static String       LOGIN_READY             = null;
   private static final String GOODBYE                 = "221 Goodbye.\r\n";
   private static final String E504_UNIMPLEMENTED      = "504 Unknown/unimplemented parameter.\r\n";
   private static final String E521_PROTO_UNSUPPORTED  = "521 Supported address families are (4,6).\r\n";
   private static final String E522_PROTO_UNSUPPORTED  = "522 Network protocol not supported, use (1,2).\r\n";
   private static final String ALL = "ALL";

   /**
	* Initializes the session.
	*
	*
	* @param in        InputStream this session should use to get data from user.
	* @param out       OutputStream this session should use to output to user.
	* @param err       ErrorStream this session should use to output errors to user.
	* @param server    The server in charge of this session.
	*/
   FTPSession (FTPInputStream in, SystemPrintStream out,
			   SystemPrintStream err, Socket s, Server server)
   {
	  super(in, out, err, server);

	  password = null;

	  if (LOGIN_READY == null)
		 LOGIN_READY = ("220 Welcome to Telnet server");/* + TINIOS.getShellName() + ".  ("
						+ TINIOS.getShellVersion()
						+ ")  Ready for user login.\r\n");*///HiepTH rem

	  socket = s;
	  cmdIn  = in;
	  cmdOut = out;

	  in.setSession(this);
	  out.setSession(this);
   }

   /**
	* Logs the user into the system.  This function will prompt the user
	* for username and password.
	*/
   public void login ()
	  throws IOException
   {

	  //Most of  the work for login happens when client
	  //sends the USER and PASS commands, so just start
	  //the connection dialog here...
	  sendMessageFile("220-", server.getConnectionMsgFile());
	  cmdOut.print(LOGIN_READY);
   }

   /**
	* Called to clean up when the session is ending.  (i.e. the
	* user typed "exit", or something bad happened that causes the
	* session to terminate.)  Closes all of the sockets associated
	* with this session.
	*/
   protected void sessionEnding ()
   {
	  try
	  {
		 try
		 {
			cmdOut.print(GOODBYE);
		 }
		 catch (Throwable t)
		 {
//            com.dalsemi.system.Debug.debugDump("Snif, didn't say goodbye.");   //DEBUG

			//DRAIN
		 }

		 try
		 {
			 if (pasvSocket != null)
				 pasvSocket.close();
		 }
		 catch(Throwable t)
		 {
			//DRAIN
		 }

		 in.close();
		 out.close();
		 cmdOut.close();
		 cmdIn.close();
		 socket.close();

		 if (datasocket != null)
		 {
			datasocket.close();
			datasocket = null;
		 }

		 cmdOut = null;
		 cmdIn  = null;
		 err    = null;
		 out    = null;
		 in     = null;
	  }
	  catch (Exception e)
	  {

		 // oh well, ending anyway
	  }
   }

   /**
	* Called when the FTP command "USER username" was issued.
	*/
   void setUser (String user)
	  throws IOException
   {
	  userName = user;
	  loggedIn = false;

	  if ((userName.equals("anonymous")) || userName.equals("ftp"))
	  {
		 if (server.isAnonymousAllowed())
		 {
			cmdOut.println("331 Anonymous login, please enter email address.");
		 }
		 else
		 {
			userName = null;
			cmdOut.println("500 Anonymous login not allowed.");
		 }
	  }
	  else
	  {
		 if ((userName.equals("root") && !server.isRootAllowed())) {
			userName = null;
			cmdOut.println("500 root login not allowed.");
		 }
		 else {
			cmdOut.print(PASS_REQUIRED);
			cmdOut.println(user);
		 }
	  }
   }

   /**
	* Called when the FTP command "PASS password" was given.  This function will
	* attempt to log the user into the system.
	*/
   void setPass (String pass)
	  throws IOException
   {

	  //Check anonymous login
	  if (userName.equalsIgnoreCase("anonymous")
			  || userName.equalsIgnoreCase("ftp"))
	  {
		 loggedIn = true;

		 //Log the anonymous login if logging turned on...
		 String ftpLog = server.logAnon();

		 if (ftpLog != null)
		 {
			try
			{
			   PrintStream logFile = null;

			   try
			   {
				  logFile =
					 new PrintStream(new FileOutputStream(ftpLog,
						true));
			   }
			   catch (FileNotFoundException fnfe)
			   {
				  logFile =
					 new PrintStream(new FileOutputStream(ftpLog));
			   }

			   logFile.print(((new Date()).toString()));
			   logFile.write(':');
			   logFile.println(pass);
			   logFile.close();
			}
			catch (Exception e)
			{
/*               com.dalsemi.system.Debug.debugDump("Error logging: "
												  + e.toString());   //DEBUG
*/
			}
		 }
	  }
	  else
	  {
		 /*if (TINIOS.login(userName, pass) == -1)
		 {
			cmdOut.println("530 Login incorrect.");

			return;
		 }*/
		 //else//HiepTH rem
		 {
			loggedIn = true;

			try
			{

				//check to see if the user's home directory
				//exists.  (i.e. a directory in root with the same name
				//as the current user.)  If so, start them out in that
				//directory.
				File file = new File(userName);

				if (file.exists() && file.isDirectory())
				{
					(System.getProperties()).put("user.home", file.getName());

					String directory = file.getAbsolutePath();
					environment.put(CURRENT_DIRECTORY, directory);
				}
			}
			catch (Exception e)
			{
				//oh well, didn't get logged in to your directory
				//out.println("Exception occurred: " + e.toString());
			}





		 }
	  }

	  sendMessageFile("230-", server.getWelcomeMsg());
	  cmdOut.print(LOGGED_IN_A);
	  cmdOut.print(userName);
	  cmdOut.print(LOGGED_IN_B);
	  System.gc();
   }

   /**
	* This function will create a new data connection when the FTP command
	* "PORT xx,xx,xx,xx,xx,xx" is received.  The port will be in the form:
	* "PORT 180,0,54,13,6,17"
	* The IP address in this case is 180.0.54.13
	* The port is (6 * 256 + 17)
	*
	* @param  port   The new IP and port in String form.
	*/
   void setPort (String port)
	  throws IOException
   {
	  int commaCount = 0;
	  int ipEnd      = 0;
	  int portSep    = 0;

	  for (int i = 0; i < port.length(); i++)
	  {
		 if (port.charAt(i) == ',')
		 {
			commaCount++;

			if (commaCount == 4)
			   ipEnd = i;
			else if (commaCount == 5)
			   portSep = i;
		 }
	  }

	  try
	  {
		 if (commaCount == 5)
		 {
			dataSocketIP   = InetAddress.getByName(port.substring(0, ipEnd).replace(',', '.'));
			dataSocketPort =
			   (((Integer.parseInt(port.substring(ipEnd + 1, portSep), 10) & 0x0FF) << 8)
				| (Integer.parseInt(port.substring(portSep + 1), 10) & 0x0FF));

			cmdOut.print(C200_SUCCESSFUL);

			return;
		 }
	  }
	  catch (NumberFormatException nfe)
	  {
		 dataSocketIP = null;
	  }

	  //String looks bad.
	  throw new IOException("Port command syntax error.");
   }


   /**
	* This function will create a new data connection when the FTP command
	* "EPRT" is received.  See RFC 2428.
	*
	* @param  args   The new IP and port in String form.
	*/
   void setEPort (String args)
	  throws IOException
   {
	  String delim = (new Character(args.charAt(0))).toString();
	  StringTokenizer st = new StringTokenizer(args, delim);

	  int af = Integer.parseInt(st.nextToken());

	  if ((af != 1) && (af != 2)) {
		cmdOut.print(E522_PROTO_UNSUPPORTED);
		return;
	  }

	  // Work around Win2K appending the interface number
	  String hostname = st.nextToken();
	  int idIndex = hostname.indexOf('%');
	  if (idIndex > 0) {
		hostname = hostname.substring(0, idIndex);
	  }
	  // ---

	  dataSocketIP   = InetAddress.getByName(hostname);
	  dataSocketPort = Integer.parseInt(st.nextToken());

	  cmdOut.print(C200_SUCCESSFUL);
   }


   /**
	* This function will create a new data connection when the FTP command
	* "LPRT" is received.  See RFC 1639.
	*
	* @param  args   The new IP and port in String form.
	*/
   void setLPort (String args)
	  throws IOException
   {
	  StringTokenizer st = new StringTokenizer(args, ",");

	  int af = Integer.parseInt(st.nextToken());

	  if ((af != 4) && (af != 6)) {
		cmdOut.print(E521_PROTO_UNSUPPORTED);
		return;
	  }

	  int idx = (af == 4) ? 12 : 0; // number of 0 bytes depending on address family

	  byte[] ip = new byte[16];

	  int hal = Integer.parseInt(st.nextToken()); // number of octets

	  if (hal + idx != 16)
		throw new IOException("LPRT command syntax error.");

	  while (hal > 0) {
		ip[idx] = (byte) Integer.parseInt(st.nextToken());

		hal--;
		idx++;
	  }

	  dataSocketIP   = InetAddress.getByAddress(null, ip); // ignore port length, it is always 2

	  hal = Integer.parseInt(st.nextToken());
	  dataSocketPort = (((Integer.parseInt(st.nextToken()) & 0x0FF) << 8) | (Integer.parseInt(st.nextToken()) & 0x0FF));

	  cmdOut.print(C200_SUCCESSFUL);
   }


   /**
	*
	*/
   void openDefaultConnection()
		throws Exception
   {
		dataSocketIP   = socket.getInetAddress();
		dataSocketPort = socket.getPort();
		openDataSocket();
   }

   /**
	*
	*/
   void openDataSocket ()
	  throws Exception
   {
	  if (datasocket != null)
		 datasocket.close();

	  datasocket = new Socket(dataSocketIP, dataSocketPort, socket.getLocalAddress(), 20);

	  in  = new FTPInputStream(datasocket.getInputStream(), true);   // this is a data connection
	  out = new SystemPrintStream(datasocket.getOutputStream());
	  err = out;
   }

   /**
	* Called when the FTP command "PASV" is issued to request us to create
	* a socket on which to listen for the user to contact us.  (As opposed to
	* using the PORT command.)  This is useful for users behind a firewall that
	* won't allow the FTP server to connect back to them.
	*/
   void goPassiveMode ()
	  throws IOException
   {
	  if (datasocket != null)
	  {
		 datasocket.close();
	  }

	  int port = 0;
	  int done = 5;
	  ServerSocket passiveSocket = null;

	  while (done != 0) { // try a few times, port might already be allocated!
		try {
		  // Randomly generate a local port.  We are going to put it in the
		  // range of 30,000 to 60,000.

		  port = 30000 + (/*Security.getRandom()HIEPTH sua*/(int)Math.round(Math.random() % 30000));

		  passiveSocket = new ServerSocket(port);
		  done = 0;
		}
		catch (IOException _) {
		  done--;
		}
	  }

	  if (passiveSocket == null)
		throw new IOException("Cannot allocate passive port");

	  passiveSocket.setSoTimeout(120000);   //Time out at 2 minutes?

	  String passOut = "227 Entering Passive Mode ("
					   + socket.getLocalAddress().getHostAddress().replace('.', ',') + ','
					   + Integer.toString(port >>> 8) + ','
					   + Integer.toString(port & 0xff) + ")\r\n";

	  cmdOut.print(passOut);

	  // Set the global 'pasvSocket' in case the FTP session
	  // is killed here so we can shut this socket down.
	  pasvSocket = passiveSocket;
	  datasocket = passiveSocket.accept();
	  // DOH! gotta close him!
	  passiveSocket.close();
	  pasvSocket = null;

	  in         = new FTPInputStream(datasocket.getInputStream(), true);   // this is a data connection
	  out        = new SystemPrintStream(datasocket.getOutputStream());
	  err        = out;
   }


   /**
	* Called when the FTP command "EPSV" is issued to request us to create
	* a socket on which to listen for the user to contact us.  See RFC 2428.
	*/
   void goEPassiveMode(String args)
	  throws IOException
   {

	  if (datasocket != null)
	  {
		 datasocket.close();
	  }

	  if (args.length() != 0) {
		if (args.equals(ALL))
		  fastNAT = true;
		else {
		  int af = Integer.parseInt(args);
		  if ((af != 1) && (af != 2)) {
			cmdOut.print(E522_PROTO_UNSUPPORTED);
			return;
		  }

		  // From now on, ignore address family. We accept connections with all protocol families.
		  // If the client knows how to connect us with a different protocol family,
		  // we're OK with it.
		}
	  }

	  int port = 0;
	  int done = 5;
	  ServerSocket passiveSocket = null;

	  while (done != 0) { // try a few times, port might already be allocated!
		try {
		  // Randomly generate a local port.  We are going to put it in the
		  // range of 30,000 to 60,000.
		  port = 30000 + (Math.abs((new Random()).nextInt()) % 30000);

		  passiveSocket = new ServerSocket(port);
		  done = 0;
		}
		catch (IOException _) {
		  done--;
		}
	  }
	  if (passiveSocket == null)
		throw new IOException("Cannot allocate passive port");

	  passiveSocket.setSoTimeout(120000);   //Time out at 2 minutes?

	  String passOut = "229 Entering Extended Passive Mode (|||" + Integer.toString(port) + "|)\r\n";

	  cmdOut.print(passOut);

	  // Set the global 'pasvSocket' in case the FTP session
	  // is killed here so we can shut this socket down.
	  pasvSocket = passiveSocket;
	  datasocket = passiveSocket.accept();
	  // DOH! gotta close him!
	  passiveSocket.close();
	  pasvSocket = null;

	  in         = new FTPInputStream(datasocket.getInputStream(), true);   // this is a data connection
	  out        = new SystemPrintStream(datasocket.getOutputStream());
	  err        = out;
   }

   /**
	*
	*/
   void putFile (String fileName)
	  throws Exception
   {
	  File currFile = new File(fileName);

	  if (currFile.exists() &&!currFile.canWrite())
	  {
		 cmdOut.println(("553 " + fileName
					   + ": Permission denied."));
	  }
	  else
	  {
		 cmdOut.println(("150 " + (ASCII ? "ASCII" : "BINARY")
					   + " connection open, putting " + fileName));

		 FileOutputStream fileOutStream = null;

		 try
		 {
			fileOutStream = new FileOutputStream(currFile);

			byte[] buffer    = new byte [512];
			int    bytesRead = 0;

			while (bytesRead != -1)
			{
			   bytesRead = in.read(buffer, 0, 512);

			   if (bytesRead != -1)
			   {
				  fileOutStream.write(buffer, 0, bytesRead);
			   }
			}
		 }
		 catch (Throwable t)
		 {
			if (fileOutStream==null)
			{
				cmdOut.println(("553 Cannot write to the file "+fileName));
			}
			//DRAIN
		 }

		 if (fileOutStream != null)
		 {
			fileOutStream.close();
			cmdOut.print(CLOSING_DATA_CONNECTION);
		 }

		 if (datasocket != null)
		 {
			datasocket.close();

			datasocket   = null;
			dataSocketIP = null;
		 }
	  }
   }

   /**
	*
	*/
   void getFile (String fileName)
	  throws Exception
   {
	  File currFile = new File(fileName);

	  if (!currFile.exists())
	  {
		 cmdOut.println(("550 " + fileName + ": No such file."));
	  }
	  else if (!currFile.canRead())
	  {
		 cmdOut.println(("550 " + fileName + ": Permission denied."));
	  }
	  else if (currFile.isDirectory())
	  {
		 cmdOut.println(("550 " + fileName + ": Not a plain file."));
	  }
	  else
	  {
		 cmdOut.println(("150 " + (ASCII ? "ASCII" : "BINARY")
					   + " connection open, getting " + fileName));

		 FileInputStream fileInStream = null;

		 try
		 {
			fileInStream = new FileInputStream(currFile);

			long   fileLength       = currFile.length();
			long   fileStreamOffset = 0;
			byte[] fileData         = new byte [512];

			while (fileStreamOffset != fileLength)
			{
			   int bytesRead = fileInStream.read(fileData);

			   if (bytesRead <= 0)
				  break;

			   out.write(fileData, 0, bytesRead);

			   fileStreamOffset += bytesRead;
			}
		 }
		 catch (Throwable t)
		 {

			//DRAIN
		 }

		 fileInStream.close();
		 cmdOut.print(CLOSING_DATA_CONNECTION);

		 if (datasocket != null)
		 {
			datasocket.close();

			datasocket   = null;
			dataSocketIP = null;
		 }
	  }
   }

   /**
	*
	*/
   void sendMessageFile (String prepend, String messageFile)
   {
	  DataInputStream inStream = null;

	  try
	  {
		 if (messageFile != null)
		 {
			File message = new File(messageFile);

			if (message.exists())
			{
			   inStream = new DataInputStream(
				  new BufferedInputStream(new FileInputStream(message)));
			   String                line;

			   while ((line = inStream.readLine()) != null)
			   {
				  cmdOut.print(prepend);
				  cmdOut.println(line);
			   }

			   cmdOut.println(prepend);
			   inStream.close();
			}
		 }
	  }
	  catch (Throwable t)
	  {
//         com.dalsemi.system.Debug.debugDump("File error: " + t.toString());   //DEBUG
	  }
   }

   /**
	* Called after each command is completed.  For example, the user
	* types "ls".  The command is received and parsed, then the appropriate
	* command is called in the shell.  Finally, we need send the correct
	* result code.
	*/
   protected void currentCommandFinished ()
   {
	  String currCommand = ( String ) environment.get(CURRENT_COMMAND);

	  if ((currCommand == null) || (currCommand.length() == 0))
		 return;

	  try
	  {
		 if (currCommand.equals("ls"))
		 {
			cmdOut.print(CLOSING_DATA_CONNECTION);

			if (datasocket != null)
			{
			   datasocket.close();

			   datasocket   = null;
			   dataSocketIP = null;
			}
		 }
		 else if (currCommand.equals("move"))
		 {
			cmdOut.println("250 File successfully renamed.");
		 }
		 else if (currCommand.equals("cd"))
		 {
			cmdOut.println("250 CWD command successful.");

			//cmdOut.write("250 CWD command successful. \"/\" is current directory.\r\n".getBytes());
			//cmdOut.write(("250 Current Directory is "
			//            + environment.get(CURRENT_DIRECTORY)
			//            + "\r\n").getBytes());
		 }
		 else if (currCommand.equals("md"))
		 {
			cmdOut.println("257 Directory created.");
		 }
		 else if (currCommand.equals("del"))
		 {
			cmdOut.println("250 File deleted.");
		 }
		 else if (currCommand.equals("rd"))
		 {
			cmdOut.println("250 Directory deleted.");
		 }
	  }
	  catch (IOException ioe)
	  {
		 forceEndSession();
	  }
   }

   /**
	* Called when an exception is thrown in a command.  This function will
	* attempt to notify the user.  Any exceptions raised specifically by
	* shell commands should try to give as descriptive a message to the
	* exception as possible.
	*
	* @param ex  The exception thrown.
	*/
   protected void exceptionThrown (Exception ex)
   {
	  String currCommand = ( String ) environment.get(CURRENT_COMMAND);
	  String message     = ex.getMessage();

	  try
	  {
		 if (currCommand.equals("cd")
				 || currCommand.equals("move") || currCommand.equals("md")
				 || currCommand.equals("del") || currCommand.equals("rd"))
		 {
			if ((currCommand.equals("rd")) && (message.indexOf("is a file")!=-1))
			{
				 cmdOut.println("550 Use 'del' to remove a file.");
			}
			else cmdOut.println(("550 " + message));
		 }
		 else if (currCommand.equals("ls"))
		 {
			if (datasocket != null)
			{
			   try
			   {
				   out.println(message);
//                   datasocket.getOutputStream().write((message + "\r\n").getBytes());
			   }
			   catch(Exception e)
			   {
				   // drain
			   }

			   datasocket.close();

			   datasocket = null;
			}

			cmdOut.print(CLOSING_DATA_CONNECTION);
		 }
		 else
		 {
			cmdOut.println(("500 Command failed with the following reason: " + message));
		 }
	  }
	  catch (IOException ioe)
	  {
		 forceEndSession();
	  }

	  environment.put(CURRENT_COMMAND, "");
   }

   /**
	* Gets the next command from this session's input stream.
	* This effectively performs functionality like a readLine(), but gives
	* the stream a chance to parse the incoming data.  This allows us to
	* translate FTP commands into system commands.
	*
	* @return  the next command from the input stream
	*/
   public String getNextCommand ()
	  throws IOException
   {
	  String returnStr = "";
	  String command   = cmdIn.readLine();

	  if (command == null)
	  {
//         com.dalsemi.system.Debug.debugDump("FS.gNC PANIC trouble.");
		 forceEndSession();

		 return null;
	  }

	  try
	  {
		 String args = null;
		 int    last;

		 if ((last = command.indexOf(' ')) != -1)
		 {
			String temp = command;

			command = command.substring(0, last++);

			if (temp.length() > last)
			   args = temp.substring(last);
		 }

		 /*this fixes the non-allowed r^H problem with a null username*/
		 if (args == null)
			args = "";

		 if (!(command.equalsIgnoreCase("RNTO")))
		 {
			lastRNFR = "";
		 }

		 //Now, translate the FTP command into the correct system
		 //command...
		 if (command.equalsIgnoreCase("QUIT"))
		 {
			returnStr = "quit";
		 }
		 else if (command.equalsIgnoreCase("USER"))
		 {
			setUser(args);
		 }
		 else if (command.equalsIgnoreCase("PASS"))
		 {
			setPass(args);
		 }
		 else if (command.equalsIgnoreCase("NOOP"))
		 {
			cmdOut.print(C200_SUCCESSFUL);
		 }
		 else if (!loggedIn)
		 {
			cmdOut.println("530 Please login with USER and PASS.");
		 }
		 else if (!fastNAT && command.equalsIgnoreCase("PORT"))
		 {
			setPort(args);
		 }
		 else if (!fastNAT && command.equalsIgnoreCase("EPRT"))
		 {
			setEPort(args);
		 }
		 else if (!fastNAT && command.equalsIgnoreCase("LPRT"))
		 {
			setLPort(args);
		 }
		 else if (command.equalsIgnoreCase("XPWD") || command.equalsIgnoreCase("PWD"))
		 {
			cmdOut.println(("257 \"" + getFromEnvironment(CURRENT_DIRECTORY)
						  + "\" is current directory."));
		 }
		 else if (command.equalsIgnoreCase("NLST"))
		 {
			if ((dataSocketIP == null) && (datasocket == null))
			{
			   cmdOut.println("425 Can't build data connection.");
			}
			else
			{
			   cmdOut.print(OPENING_LIST_CONNECTION);

			   if ((args != null) && (args.length() != 0))
			   {
				  returnStr = "ls " + args;
			   }
			   else
			   {
				  returnStr = "ls";
			   }

			   if (dataSocketIP != null)
			   {
				  openDataSocket();
			   }
			}
		 }
		 else if (command.equalsIgnoreCase("LIST"))
		 {
			if ((dataSocketIP == null) && (datasocket == null))
			{
			   cmdOut.println("425 Can't build data connection.");
			}
			else
			{
			   cmdOut.print(OPENING_LIST_CONNECTION);

			   if ((args != null) && (args.length() > 1) && args.substring(0,2).equalsIgnoreCase("-L")) {
				  // fix for gftp client
				  args = args.substring(2);
			   }

			   if ((args != null) && (args.length() != 0))
			   {
				  returnStr = "ls -l " + args;
			   }
			   else
			   {
				  returnStr = "ls -l";
			   }

			   if (dataSocketIP != null)
			   {
				  openDataSocket();
			   }
			}
		 }
		 else if (command.equalsIgnoreCase("TYPE"))
		 {

			// if args is null there's something wrong
			if (args.equalsIgnoreCase("I"))
			{
			   ASCII = false;

			   cmdOut.println("200 Type set to Binary");
			}
			else if (args.equalsIgnoreCase("A"))
			{
			   ASCII = true;

			   cmdOut.println("200 Type set to ASCII");
			}
			else
			{
			   cmdOut.print(E504_UNIMPLEMENTED);
			}
		 }
		 else if (command.equalsIgnoreCase("STRU"))
		 {

			if (args.equalsIgnoreCase("F"))
			{
			   cmdOut.print(C200_SUCCESSFUL);
			}
			else
			{
			   cmdOut.print(E504_UNIMPLEMENTED);
			}
		 }
		 else if (command.equalsIgnoreCase("MODE"))
		 {

			if (args.equalsIgnoreCase("S"))
			{
			   cmdOut.print(C200_SUCCESSFUL);
			}
			else
			{
			   cmdOut.print(E504_UNIMPLEMENTED);
			}
		 }
		 else if (command.equalsIgnoreCase("CWD"))
		 {
			returnStr = "cd " + args;
		 }
		 else if (command.equalsIgnoreCase("CDUP"))
		 {
			returnStr = "cd ..";
		 }
		 else if (command.equalsIgnoreCase("RETR"))
		 {
			if ((dataSocketIP == null) && (datasocket == null))
			{
			   cmdOut.println("425 Can't build data connection.");
			   // openDefaultConnection();
			}
			else
			{
			   if (dataSocketIP != null)
				  openDataSocket();

			   getFile(args);
			}
		 }
		 else if (command.equalsIgnoreCase("STOR"))
		 {
			if ((dataSocketIP == null) && (datasocket == null))
			{
			   cmdOut.println("425 Can't build data connection.");
			}
			else
			{
			   if (dataSocketIP != null)
				  openDataSocket();
			   putFile(args);
			}
		 }
		 else if (command.equalsIgnoreCase("RNTO"))
		 {
			if (lastRNFR.length() == 0)
			{
			   cmdOut.println("503 Bad command sequence");

			   returnStr = "";
			}
			else
			{
			   returnStr = "move " + lastRNFR + ' ' + args;
			   lastRNFR  = "";
			}
		 }
		 else if (command.equalsIgnoreCase("XMKD") || command.equalsIgnoreCase("MKD"))
		 {
			returnStr = "md " + args;
		 }
		 else if (command.equalsIgnoreCase("RNFR"))
		 {
			File temp = new File(args);

			if (temp.exists())
			{
			   cmdOut.println("350 File found, ready to RNTO...");

			   lastRNFR = args;
			}
			else
			{
			   cmdOut.println("550 File not found.");

			   lastRNFR = "";
			}
		 }
		 else if (command.equalsIgnoreCase("DELE"))
		 {
			returnStr = "del " + args;
		 }
		 else if (command.equalsIgnoreCase("XRMD") || command.equalsIgnoreCase("RMD"))
		 {
			returnStr = "rd " + args;
		 }
		 else if (!fastNAT && command.equalsIgnoreCase("PASV"))
		 {
			goPassiveMode();
		 }
		 else if (command.equalsIgnoreCase("EPSV"))
		 {
			goEPassiveMode(args);
		 }
		 else if (command.equalsIgnoreCase("SYST"))
		 {
			cmdOut.println("215 UNIX Type: L8");
		 }
		 else
		 {
			cmdOut.println(
			   "500 Unrecognized/Unsupported Command or Bad Syntax");
		 }
	  }
	  catch (Exception e)
	  {
		  //Try to inform client that death and destruction has occurred.
		  returnStr = "";
		  cmdOut.println("550 Unknown internal failure: " + e.toString());
		  throw new IOException(e.toString());
	  }

	  return returnStr;
   }
}
