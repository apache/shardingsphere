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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLAuthenticationPluginData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySQLNativePasswordAuthenticatorTest {
    
    private final MySQLNativePasswordAuthenticator authenticator = new MySQLNativePasswordAuthenticator();
    
    @Test
    void assertAuthenticateWithEmptyPassword() {
        ShardingSphereUser user = new ShardingSphereUser("root", "", "localhost");
        MySQLAuthenticationPluginData pluginData = new MySQLAuthenticationPluginData(new byte[8], new byte[12]);
        assertTrue(authenticator.authenticate(user, new Object[]{new byte[20], pluginData}));
    }
    
    @Test
    void assertAuthenticateWithPassword() {
        ShardingSphereUser user = new ShardingSphereUser("root", "pwd", "localhost");
        byte[] pluginDataPart1 = {21, 22, 23, 24, 25, 26, 27, 28};
        byte[] pluginDataPart2 = {29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40};
        MySQLAuthenticationPluginData pluginData = new MySQLAuthenticationPluginData(pluginDataPart1, pluginDataPart2);
        byte[] authCipher = getAuthCipherBytes("pwd", pluginData.getAuthenticationPluginData());
        assertTrue(authenticator.authenticate(user, new Object[]{authCipher, pluginData}));
        authCipher[0] = (byte) (authCipher[0] ^ 0xFF);
        assertFalse(authenticator.authenticate(user, new Object[]{authCipher, pluginData}));
    }
    
    private byte[] getAuthCipherBytes(final String password, final byte[] authenticationPluginData) {
        byte[] sha1Password = DigestUtils.sha1(password);
        byte[] doubleSha1Password = DigestUtils.sha1(sha1Password);
        byte[] concatBytes = new byte[authenticationPluginData.length + doubleSha1Password.length];
        System.arraycopy(authenticationPluginData, 0, concatBytes, 0, authenticationPluginData.length);
        System.arraycopy(doubleSha1Password, 0, concatBytes, authenticationPluginData.length, doubleSha1Password.length);
        byte[] sha1ConcatBytes = DigestUtils.sha1(concatBytes);
        byte[] result = new byte[sha1Password.length];
        for (int index = 0; index < sha1Password.length; index++) {
            result[index] = (byte) (sha1Password[index] ^ sha1ConcatBytes[index]);
        }
        return result;
    }
}
