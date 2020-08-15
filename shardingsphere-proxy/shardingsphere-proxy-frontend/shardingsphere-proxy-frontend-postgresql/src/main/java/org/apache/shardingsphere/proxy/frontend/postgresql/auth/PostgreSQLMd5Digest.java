/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright (c) 2003, PostgreSQL Global Development Group
 * See the LICENSE file in the project root for more information.
 */

package org.apache.shardingsphere.proxy.frontend.postgresql.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5-based utility function to obfuscate passwords before network transmission.
 *
 * @author Jeremy Wohl
 */
public final class PostgreSQLMd5Digest {
    
    private PostgreSQLMd5Digest() {
    }
    
    /**
     * Encodes user/password/salt information in the following way: MD5(MD5(password + user) + salt).
     *
     * @param user     The connecting user.
     * @param password The connecting user's password.
     * @param salt     A four-salt sent by the server.
     * @return A 35-byte array, comprising the string "md5" and an MD5 digest.
     */
    public static byte[] encode(final byte[] user, final byte[] password, final byte[] salt) {
        MessageDigest md;
        byte[] tempDigest;
        byte[] passDigest;
        byte[] hexDigest = new byte[35];
        
        try {
            md = MessageDigest.getInstance("MD5");
            
            md.update(password);
            md.update(user);
            tempDigest = md.digest();
            
            bytesToHex(tempDigest, hexDigest, 0);
            md.update(hexDigest, 0, 32);
            md.update(salt);
            passDigest = md.digest();
            
            bytesToHex(passDigest, hexDigest, 3);
            hexDigest[0] = (byte) 'm';
            hexDigest[1] = (byte) 'd';
            hexDigest[2] = (byte) '5';
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to encode password with MD5", e);
        }
        
        return hexDigest;
    }
    
    /*
     * Turn 16-byte stream into a human-readable 32-byte hex string
     */
    private static void bytesToHex(final byte[] bytes, final byte[] hex, final int offset) {
        final char[] lookup =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        
        int i;
        int c;
        int j;
        int pos = offset;
        
        for (i = 0; i < 16; i++) {
            c = bytes[i] & 0xFF;
            j = c >> 4;
            hex[pos++] = (byte) lookup[j];
            j = c & 0xF;
            hex[pos++] = (byte) lookup[j];
        }
    }
}
