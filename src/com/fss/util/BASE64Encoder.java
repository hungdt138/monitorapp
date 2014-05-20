// Decompiled by DJ v2.8.8.54 Copyright 2000 Atanas Neshkov  Date: 30/05/2003 1:02:29 PM
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3)
// Source File Name:   BASE64Encoder.java

package com.fss.util;

import java.io.IOException;
import java.io.OutputStream;

// Referenced classes of package weblogic.utils.encoders:
//            CharacterEncoder

public class BASE64Encoder extends CharacterEncoder
{

	int bytesPerAtom()
	{
		return 3;
	}

	int bytesPerLine()
	{
		return 57;
	}

	void encodeAtom(OutputStream outputstream, byte abyte0[], int i, int j)
		throws IOException
	{
		if(j == 1)
		{
			byte byte0 = abyte0[i];
			int k = 0;
			boolean flag = false;
			outputstream.write(pem_array[byte0 >>> 2 & 0x3f]);
			outputstream.write(pem_array[(byte0 << 4 & 0x30) + (k >>> 4 & 0xf)]);
			outputstream.write(61);
			outputstream.write(61);
		} else
		if(j == 2)
		{
			byte byte1 = abyte0[i];
			byte byte3 = abyte0[i + 1];
			int l = 0;
			outputstream.write(pem_array[byte1 >>> 2 & 0x3f]);
			outputstream.write(pem_array[(byte1 << 4 & 0x30) + (byte3 >>> 4 & 0xf)]);
			outputstream.write(pem_array[(byte3 << 2 & 0x3c) + (l >>> 6 & 3)]);
			outputstream.write(61);
		} else
		{
			byte byte2 = abyte0[i];
			byte byte4 = abyte0[i + 1];
			byte byte5 = abyte0[i + 2];
			outputstream.write(pem_array[byte2 >>> 2 & 0x3f]);
			outputstream.write(pem_array[(byte2 << 4 & 0x30) + (byte4 >>> 4 & 0xf)]);
			outputstream.write(pem_array[(byte4 << 2 & 0x3c) + (byte5 >>> 6 & 3)]);
			outputstream.write(pem_array[byte5 & 0x3f]);
		}
	}

	void encodeLineSuffix(OutputStream outputstream)
		throws IOException
	{
	}

	private static final char pem_array[] = {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
		'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
		'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
		'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
		'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
		'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', '+', '/'
	};

}
