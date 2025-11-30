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

import com.google.common.base.Strings;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLAuthenticationMethod;
import org.apache.shardingsphere.database.protocol.mysql.packet.handshake.MySQLAuthenticationPluginData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.proxy.frontend.mysql.authentication.authenticator.MySQLAuthenticator;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Caching sha2 password authenticator for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_caching_sha2_authentication_exchanges.html">Caching_sha2_password information</a>
 */
public final class MySQLCachingSha2PasswordAuthenticator implements MySQLAuthenticator {
    
    @Override
    public boolean authenticate(final ShardingSphereUser user, final Object[] authInfo) {
        byte[] authResponse = (byte[]) authInfo[0];
        MySQLAuthenticationPluginData authPluginData = (MySQLAuthenticationPluginData) authInfo[1];
        return Strings.isNullOrEmpty(user.getPassword()) || Arrays.equals(scramble256(user.getPassword().getBytes(), authPluginData.getAuthenticationPluginData()), authResponse);
    }
    
    @SneakyThrows({NoSuchAlgorithmException.class, DigestException.class})
    private byte[] scramble256(final byte[] pass, final byte[] authenticationPluginData) {
        int cachingSha2DigestLength = 32;
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] dig1 = new byte[cachingSha2DigestLength];
        byte[] dig2 = new byte[cachingSha2DigestLength];
        messageDigest.update(pass, 0, pass.length);
        messageDigest.digest(dig1, 0, cachingSha2DigestLength);
        messageDigest.reset();
        messageDigest.update(dig1, 0, dig1.length);
        messageDigest.digest(dig2, 0, cachingSha2DigestLength);
        messageDigest.reset();
        messageDigest.update(dig2, 0, dig1.length);
        messageDigest.update(authenticationPluginData, 0, authenticationPluginData.length);
        byte[] scramble1 = new byte[cachingSha2DigestLength];
        messageDigest.digest(scramble1, 0, cachingSha2DigestLength);
        byte[] result = new byte[cachingSha2DigestLength];
        xorString(dig1, result, scramble1, cachingSha2DigestLength);
        return result;
    }
    
    private void xorString(final byte[] from, final byte[] to, final byte[] scramble, final int length) {
        int pos = 0;
        int scrambleLength = scramble.length;
        while (pos < length) {
            to[pos] = (byte) (from[pos] ^ scramble[pos % scrambleLength]);
            pos++;
        }
    }
    
    @Override
    public String getAuthenticationMethodName() {
        return MySQLAuthenticationMethod.CACHING_SHA2_PASSWORD.getMethodName();
    }
}
