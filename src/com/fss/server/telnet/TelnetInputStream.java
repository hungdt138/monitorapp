/*---------------------------------------------------------------------------
 * Copyright (C) 1999,2000 Dallas Semiconductor Corporation, All Rights Reserved.
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

package com.fss.server.telnet;

import com.fss.server.*;
import java.io.*;

/**
 * This is an input stream to be used by a TelnetSession.
 *
 * @author Stephen Hess, Lorne Smith, Kris Ardis
 * @version 1.0
 */
public class TelnetInputStream extends SystemInputStream
{
	int last = -1;
	int oops = -1;
	int thisline = 0;

	/**
	 * Initialize the stream.
	 *
	 * @param  in  Underlying InputStream to use.
	 * @param  out PrintStream to echo characters
	 */
	public TelnetInputStream(InputStream in,PrintStream out)
	{
		super(in,out);
	}

	public int rawAvailable() throws IOException
	{
		return root.available() + (oops == -1 ? 0 : 1);
	}

	/**
	 * Read the next character from the stream.  This should
	 * only be called from within a synchronized block.
	 *
	 * @return  the next character from the stream, or
	 * <code>-1</code> if the end of the stream has been
	 * reached.
	 *
	 * @throws IOException if an IO problem occurs during
	 * the read.
	 */
	protected int rawRead() throws IOException
	{
		try
		{
			int r;
			if(oops != -1)
			{
				r = oops;
				oops = -1;
			}
			else
			{
				r = root.read();
				if(r == 9) // HiepTH sua TAB
					r = ' ';
			}

			if(r == 0) // some unix send \r\0 for EOLN
				if(last == 0x0d)
					r = 0x0a;

			//Handle possible control sequence...
			while((r == 0x0ff) || (r == 0x1b))
			{
				if(r == 0x0ff)
				{
					if(escape())
						return 0x0ff; // escape returns true if next byte is 0xff

					r = root.read(); // else we want to process the next non escaped byte
					if(r == 9) // HiepTH sua TAB
						r = ' ';
				}
				else // this is an arrow key/escaped character
				{
					if(root.available() == 0)
					{
						return 0x1b;
					}

					root.read(); // read the 4f

					r = root.read();
					if(r == 9) // HiepTH sua TAB
						r = ' ';

					byte[] b;

					switch(r)
					{

						case 0x44: // the left arrow=bkspace
							r = ASCII_BS;
							break;

						case 0x43: // the right arrow=normal space
							r = ASCII_SPACE;
							break;

						case 0x41: // go UP one command
						case 0x42: // go DOWN one command
							if((session == null) || (session.inCommand()))
							{
								r = root.read();
								if(r == 9) // HiepTH sua TAB
									r = ' ';
								break;
							}

							String newCommand = null;

							if(r == 0x41)
							{
								if(session != null)
									newCommand = session.stepUpHistory();

								if(newCommand == null)
								{
									r = root.read();
									if(r == 9) // HiepTH sua TAB
										r = ' ';
									break;
								}
							}
							else
								newCommand = session.stepDownHistory();

							byte[] command_bytes = newCommand.getBytes();

							//Clear what was in the buffer
							startPos = 0;
							endPos = 0;

							if(ECHO)
							{
								increaseBuffer(3 * thisline);
								java.util.Arrays.fill(buffer,0,thisline,(byte)ASCII_BS);
								java.util.Arrays.fill(buffer,thisline,thisline * 2,(byte)ASCII_SPACE);
								java.util.Arrays.fill(buffer,thisline * 2,thisline * 3,(byte)ASCII_BS);
								out.write(buffer,0,thisline * 3);
							}

							//Now insert the new command into the buffer.
							increaseBuffer(command_bytes.length);
							System.arraycopy(command_bytes,0,buffer,0,command_bytes.length);

							endPos = command_bytes.length;

							thisline = command_bytes.length;
							if(ECHO)
								out.write(buffer,0,thisline);
							else
								break;
						default:
							r = root.read();
							if(r == 9) // HiepTH sua TAB
								r = ' ';
					}
				}
			}

			if(r == -1)
				return -1;

			if(ECHO || (r == '\n') || (r == '\r'))
			{
				if((r != ASCII_BS) && (r != ASCII_DEL) && (r != 0x0FF))
				{
					thisline++;
					if(ECHO)
						out.write(r);
				}
				else if((r == ASCII_BS) || (r == ASCII_DEL))
				{
					if(thisline > 0)
					{
						thisline--;
						if(ECHO)
							out.write(ERASE);
					}
				}
			}

			if(r == '\r')
			{
				thisline = 0;

				if(root.available() > 0)
				{
					oops = root.read();
					if(oops == 9) // HiepTH sua TAB
						oops = ' ';
				}

				if(ECHO && ((oops != '\n') && (oops != 0)))
					out.write('\n');
			}

			if(r == '\n')
				thisline = 0;
			last = r;
			return r;
		}
		catch(IOException e)
		{
			if(session != null)
				session.forceEndSession();
			throw e;
		}
	}

	/**
	 *
	 */
	static byte[] RESPONSE = {(byte)0xff,0x00,0x00};

	public synchronized void negotiateEcho() throws IOException
	{
		if(alreadyGotEcho)return;
		byte[] data = {(byte)0x0ff,(byte)0xfb,(byte)0x01,
					  (byte)0x0ff,(byte)0xfb,(byte)0x03};
		out.write(data);
	}

	private boolean alreadyGotEcho = false;

	public synchronized boolean escape() throws IOException // returns true if the next byte is 255
	{
		// means we just received a 255
		int o1 = root.read();
		if(o1 == 9) // HiepTH sua TAB
			o1 = ' ';

		if(o1 == 0x0ff)
			return true;

		//this gets destroyed when we deny things, let's restore it
		RESPONSE[1] = (byte)0xfb;

		if(o1 > 0x0fa) // if its a will/wont/do/dont
		{
			int o2 = root.read(); // this will be the option
			if(o2 == 9) // HiepTH sua TAB
				o2 = ' ';

			RESPONSE[2] = (byte)o2;
			//if it is a WILL or a DO request, see what it is...
			if((o1 == 0xfb) || (o1 == 0xfd))
			{
				/*
				 1 = ECHO MODE
				 3 = SUPPRESS GO AHEAD
				 31 = WINDOW SIZE NEGOTIATION
				 34 = LINE MODE
				 */
				if(o2 == 3)
				{
					if(o1 == 0xfb)
						out.write(RESPONSE);
					RESPONSE[2] = 0x01;
					//i realize this falls through, and it should...
				}
				if((o2 == 1) || (o2 == 3) || (o2 == 31) || (o2 == 34))
				{
					if(o1 == 0xfb)
						out.write(RESPONSE);
					if(o2 == 1)
					{
						ECHO = true;
						alreadyGotEcho = true;
					}
					return false;
				}
			}
			else if(o2 == 1)
			{
				ECHO = false;
				alreadyGotEcho = true;
			}

			// automatically deny everything, means first byte == FC or FE
			RESPONSE[1] = (byte)((o1 > 0xfc) ? 0xfc : 0xfe);

			// already set RESPONSE[2] to o2 above
			out.write(RESPONSE);

			return false;
		}
		return false;
	}
}
