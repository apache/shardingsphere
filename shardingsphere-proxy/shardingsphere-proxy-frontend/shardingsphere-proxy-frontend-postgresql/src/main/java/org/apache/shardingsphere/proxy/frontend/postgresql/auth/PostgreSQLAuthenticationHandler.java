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

package org.apache.shardingsphere.proxy.frontend.postgresql.auth;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLErrorCode;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLPasswordMessagePacket;
import org.apache.shardingsphere.infra.auth.ProxyUser;
import org.apache.shardingsphere.proxy.backend.schema.ProxyContext;

import java.security.MessageDigest;
import java.util.Collection;
import java.util.Map;

/**
 * Authentication handler for PostgreSQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLAuthenticationHandler {
    
    /**
     * Login.
     *
     * @param username username
     * @param databaseName database name
     * @param md5Salt MD5 salt
     * @param passwordMessagePacket password message packet
     * @return PostgreSQL login result
     */
    public static PostgreSQLLoginResult loginWithMd5Password(final String username, final String databaseName, final byte[] md5Salt, final PostgreSQLPasswordMessagePacket passwordMessagePacket) {
        ProxyUser proxyUser = null;
        for (Map.Entry<String, ProxyUser> entry : ProxyContext.getInstance().getSchemaContexts().getAuthentication().getUsers().entrySet()) {
            if (entry.getKey().equals(username)) {
                proxyUser = entry.getValue();
                break;
            }
        }
        if (null == proxyUser) {
            return new PostgreSQLLoginResult(PostgreSQLErrorCode.INVALID_AUTHORIZATION_SPECIFICATION, "unknown username: " + username);
        }
        String md5Digest = passwordMessagePacket.getMd5Digest();
        String expectedMd5Digest = md5Encode(username, proxyUser.getPassword(), md5Salt);
        if (!expectedMd5Digest.equals(md5Digest)) {
            return new PostgreSQLLoginResult(PostgreSQLErrorCode.INVALID_PASSWORD, "password authentication failed for user \"" + username + "\"");
        }
        if (!isAuthorizedSchema(proxyUser.getAuthorizedSchemas(), databaseName)) {
            return new PostgreSQLLoginResult(PostgreSQLErrorCode.PRIVILEGE_NOT_GRANTED, String.format("Access denied for user '%s' to database '%s'", username, databaseName));
        }
        return new PostgreSQLLoginResult(PostgreSQLErrorCode.SUCCESSFUL_COMPLETION, null);
    }
    
    private static String md5Encode(final String username, final String password, final byte[] md5Salt) {
        String passwordHash = new String(Hex.encodeHex(DigestUtils.md5(password + username), true));
        MessageDigest messageDigest = DigestUtils.getMd5Digest();
        messageDigest.update(passwordHash.getBytes());
        messageDigest.update(md5Salt);
        return "md5" + new String(Hex.encodeHex(messageDigest.digest(), true));
    }
    
    private static boolean isAuthorizedSchema(final Collection<String> authorizedSchemas, final String schema) {
        return Strings.isNullOrEmpty(schema) || CollectionUtils.isEmpty(authorizedSchemas) || authorizedSchemas.contains(schema);
    }
}
