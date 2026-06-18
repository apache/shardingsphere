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

package org.apache.shardingsphere.proxy.frontend.mysql.authentication.authenticator.impl;

import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLAuthenticationPluginData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.junit.jupiter.api.Test;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySQLCachingSha2PasswordAuthenticatorTest {
    
    private final MySQLCachingSha2PasswordAuthenticator authenticator = new MySQLCachingSha2PasswordAuthenticator();
    
    @Test
    void assertAuthenticateWithEmptyPassword() {
        ShardingSphereUser user = new ShardingSphereUser("root", "", "localhost");
        MySQLAuthenticationPluginData pluginData = new MySQLAuthenticationPluginData(new byte[8], new byte[12]);
        assertTrue(authenticator.authenticate(user, new Object[]{new byte[32], pluginData}));
    }
    
    @Test
    void assertAuthenticateWithPassword() throws NoSuchAlgorithmException, DigestException {
        ShardingSphereUser user = new ShardingSphereUser("root", "pwd", "localhost");
        byte[] pluginDataPart1 = {1, 2, 3, 4, 5, 6, 7, 8};
        byte[] pluginDataPart2 = {9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20};
        MySQLAuthenticationPluginData pluginData = new MySQLAuthenticationPluginData(pluginDataPart1, pluginDataPart2);
        byte[] authenticationPluginData = pluginData.getAuthenticationPluginData();
        byte[] expectedResponse = scramble256("pwd".getBytes(), authenticationPluginData);
        assertTrue(authenticator.authenticate(user, new Object[]{expectedResponse, pluginData}));
        expectedResponse[0] = (byte) ~expectedResponse[0];
        assertFalse(authenticator.authenticate(user, new Object[]{expectedResponse, pluginData}));
    }
    
    private byte[] scramble256(final byte[] password, final byte[] authenticationPluginData) throws NoSuchAlgorithmException, DigestException {
        int digestLength = 32;
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] dig1 = new byte[digestLength];
        byte[] dig2 = new byte[digestLength];
        messageDigest.update(password, 0, password.length);
        messageDigest.digest(dig1, 0, digestLength);
        messageDigest.reset();
        messageDigest.update(dig1, 0, dig1.length);
        messageDigest.digest(dig2, 0, digestLength);
        messageDigest.reset();
        messageDigest.update(dig2, 0, dig1.length);
        messageDigest.update(authenticationPluginData, 0, authenticationPluginData.length);
        byte[] scramble1 = new byte[digestLength];
        messageDigest.digest(scramble1, 0, digestLength);
        byte[] result = new byte[digestLength];
        xorString(dig1, result, scramble1);
        return result;
    }
    
    private void xorString(final byte[] from, final byte[] to, final byte[] scramble) {
        for (int index = 0; index < to.length; index++) {
            to[index] = (byte) (from[index] ^ scramble[index % scramble.length]);
        }
    }
}
