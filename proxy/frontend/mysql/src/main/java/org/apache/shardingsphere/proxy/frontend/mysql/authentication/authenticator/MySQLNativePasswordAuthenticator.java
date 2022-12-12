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

package org.apache.shardingsphere.proxy.frontend.mysql.authentication.authenticator;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLAuthenticationMethod;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLAuthPluginData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;

import java.util.Arrays;

/**
 * Native password authenticator for MySQL.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/secure-password-authentication.html">Secure Password Authentication</a>
 */
@RequiredArgsConstructor
public final class MySQLNativePasswordAuthenticator implements MySQLAuthenticator {
    
    private final MySQLAuthPluginData authPluginData;
    
    @Override
    public boolean authenticate(final ShardingSphereUser user, final byte[] authResponse) {
        return Strings.isNullOrEmpty(user.getPassword()) || Arrays.equals(getAuthCipherBytes(user.getPassword()), authResponse);
    }
    
    private byte[] getAuthCipherBytes(final String password) {
        byte[] sha1Password = DigestUtils.sha1(password);
        byte[] doubleSha1Password = DigestUtils.sha1(sha1Password);
        byte[] concatBytes = new byte[authPluginData.getAuthenticationPluginData().length + doubleSha1Password.length];
        System.arraycopy(authPluginData.getAuthenticationPluginData(), 0, concatBytes, 0, authPluginData.getAuthenticationPluginData().length);
        System.arraycopy(doubleSha1Password, 0, concatBytes, authPluginData.getAuthenticationPluginData().length, doubleSha1Password.length);
        byte[] sha1ConcatBytes = DigestUtils.sha1(concatBytes);
        return xor(sha1Password, sha1ConcatBytes);
    }
    
    private byte[] xor(final byte[] input, final byte[] secret) {
        byte[] result = new byte[input.length];
        for (int i = 0; i < input.length; ++i) {
            result[i] = (byte) (input[i] ^ secret[i]);
        }
        return result;
    }
    
    @Override
    public String getAuthenticationMethodName() {
        return MySQLAuthenticationMethod.SECURE_PASSWORD_AUTHENTICATION.getMethodName();
    }
}
