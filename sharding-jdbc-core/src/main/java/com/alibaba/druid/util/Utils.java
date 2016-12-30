/*
 * Copyright 1999-2101 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.druid.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {
    
    public static String md5(final String text) {
        MessageDigest msgDigest;
        try {
            msgDigest = MessageDigest.getInstance("MD5");
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalStateException("System doesn't support MD5 algorithm.", ex);
        }
        msgDigest.update(text.getBytes());
        return HexBin.encode(msgDigest.digest(), false);
    }
    
    public static void putLong(final byte[] b, final int off, final long val) {
        b[off + 7] = (byte) val;
        b[off + 6] = (byte) (val >>> 8);
        b[off + 5] = (byte) (val >>> 16);
        b[off + 4] = (byte) (val >>> 24);
        b[off + 3] = (byte) (val >>> 32);
        b[off + 2] = (byte) (val >>> 40);
        b[off + 1] = (byte) (val >>> 48);
        b[off] = (byte) (val >>> 56);
    }
}
