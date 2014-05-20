
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

package com.fss.server.ftp;



import com.fss.server.*;
import java.io.*;


/**
 * This is an input stream for an FTPSession.  It gives the
 * ability to toggle character echo, as well as providing a readLine
 * method.
 *
 * @author Stephen Hess, Lorne Smith, Kris Ardis
 * @version 1.0
 */
class FTPInputStream
   extends SystemInputStream
{
   boolean DATA = false;   // false for CONTROL
   int     last = -1;

   /**
	* Initialize the FTP input stream.
	*
	* @param i     root input stream
	* @param data  <true> if this is a data input stream,
	* <code>false</code if this is a command input stream.
	*/
   FTPInputStream (InputStream i, boolean data)
   {
	  super(i, null);

	  DATA    = data;
	  rawMode = true;
   }

   /**
	* Read the next character from the stream.
	*
	* @return  the next character from the stream, or
	* <code>-1</code> if the end of the stream has been
	* reached.
	*
	* @throws IOException if an I/O error occurs during
	* the read.
	*/
   public int rawRead ()
	  throws IOException
   {
	  if (DATA)
		 return root.read();

	  int r = root.read();

	  // some unix send \r\0 for EOLN
	  if ((r == 0) && (last == 0x0d))
		 r = 0x0a;

	  last = r;

	  return r;
   }

   /**
	* Method read
	*
	*
	* @param b
	* @param off
	* @param len
	*
	* @return
	*
	* @throws IOException
	*
	*/
   public int read (byte[] b, int off, int len)
	  throws IOException
   {
	  return root.read(b, off, len);
   }
}
