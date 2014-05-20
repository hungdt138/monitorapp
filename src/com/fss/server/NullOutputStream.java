
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

package com.fss.server;



/**
 * <p>This class is used as an outlet when data is available for output but
 * a port is not currently available. <code>NullOutputStream</code>s are used
 * when the <code>java.lang.System</code> class initializes.  <code>System.out</code>
 * and <code>System.err</code> are initialized to this class so that system
 * initialization routines do not have to open any devices.</p>
 *
 * <p>This class is also useful in other instances.  In Slush, if a telnet session
 * runs a process in the background and the telnet session is closed, the
 * background process now holds an invalid <code>OutputStream</code>.  Slush changes the
 * process' <code>OutputStream</code> to a <code>NullOutputStream</code> so the background process
 * can continue to run.</p>
 *
 * @see NullInputStream
 */
public class NullOutputStream
   extends java.io.OutputStream
{

   /**
	* Writes the specified byte to this <code>NullOutputStream</code>.  The data is not
	* actually written to any port or device.
	*
	* @param b data to be sent
	*/
   public void write (int b)
   {
   }

   /**
	* Writes the specified array to this <code>NullOutputStream</code>.  The data is not
	* actually written to any port or device.  This method is
	* included so writes to this <code>NullOutputStream</code> will occur faster.
	*
	* @param barr the array containing data to be output
	* @param offset offset into array where data starts
	* @param length number of bytes to be sent
	*/
   public void write (byte[] barr, int offset, int length)
   {
	  if (barr == null) throw new NullPointerException();
	  if ((offset < 0) || (length < 0) || ((offset+length) > barr.length))
		throw new ArrayIndexOutOfBoundsException();
   }
}
