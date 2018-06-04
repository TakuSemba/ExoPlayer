/* Copyright (C) 2009 "Michael B Allen" <jcifs at samba dot org>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.google.android.exoplayer2.upstream;

class RC4
{

  private byte[] s;
  private int i, j;

  RC4(byte[] key) {

    int keyLength = key.length;

    s = new byte[256];

    for (i = 0; i < 256; i++)
      s[i] = (byte)i;

    for (i = j = 0; i < 256; i++) {
      j = (j + key[i % keyLength] + s[i]) & 0xff;
      byte t = s[i];
      s[i] = s[j];
      s[j] = t;
    }

    i = j = 0;
  }

  public void update(byte[] src, byte[] dst)
  {
    int soff = 0;
    int doff = 0;
    int slen = src.length;

    while (soff < slen) {
      i = (i + 1) & 0xff;
      j = (j + s[i]) & 0xff;
      byte t = s[i];
      s[i] = s[j];
      s[j] = t;
      dst[doff++] = (byte)(src[soff++] ^ s[(s[i] + s[j]) & 0xff]);
    }
  }
}
