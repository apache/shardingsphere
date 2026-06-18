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

package org.apache.shardingsphere.proxy.frontend.postgresql.authentication.authenticator.impl;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.database.protocol.postgresql.constant.PostgreSQLAuthenticationMethod;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.authenticator.PostgreSQLAuthenticator;

import java.security.MessageDigest;

/**
 * MD5 password authenticator for PostgreSQL.
 */
public final class PostgreSQLMD5PasswordAuthenticator implements PostgreSQLAuthenticator {
    
    @Override
    public boolean authenticate(final ShardingSphereUser user, final Object[] authInfo) {
        String md5Digest = (String) authInfo[0];
        byte[] md5Salt = (byte[]) authInfo[1];
        String expectedMd5Digest = md5Encode(user.getGrantee().getUsername(), user.getPassword(), md5Salt);
        return expectedMd5Digest.equals(md5Digest);
    }
    
    private String md5Encode(final String username, final String password, final byte[] md5Salt) {
        String passwordHash = new String(Hex.encodeHex(DigestUtils.md5(password + username), true));
        MessageDigest messageDigest = DigestUtils.getMd5Digest();
        messageDigest.update(passwordHash.getBytes());
        messageDigest.update(md5Salt);
        return "md5" + new String(Hex.encodeHex(messageDigest.digest(), true));
    }
    
    @Override
    public String getAuthenticationMethodName() {
        return PostgreSQLAuthenticationMethod.MD5.getMethodName();
    }
}
