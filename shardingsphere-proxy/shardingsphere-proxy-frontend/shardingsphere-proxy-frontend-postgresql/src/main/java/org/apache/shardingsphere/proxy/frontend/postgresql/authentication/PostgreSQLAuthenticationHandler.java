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

package org.apache.shardingsphere.proxy.frontend.postgresql.authentication;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLErrorCode;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLPasswordMessagePacket;
import org.apache.shardingsphere.infra.executor.check.SQLCheckEngine;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import java.security.MessageDigest;
import java.util.Collection;
import java.util.LinkedList;

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
        String md5Digest = passwordMessagePacket.getMd5Digest();
        Grantee grantee = new Grantee(username, "%");
        if (!Strings.isNullOrEmpty(databaseName) && !ProxyContext.getInstance().schemaExists(databaseName)) {
            return new PostgreSQLLoginResult(PostgreSQLErrorCode.INVALID_CATALOG_NAME, String.format("database \"%s\" does not exist", databaseName));
        }
        if (!SQLCheckEngine.check(grantee, getRules(databaseName))) {
            return new PostgreSQLLoginResult(PostgreSQLErrorCode.INVALID_AUTHORIZATION_SPECIFICATION, String.format("unknown username: %s", username));
        }
        if (!SQLCheckEngine.check(grantee, (a, b) -> isPasswordRight((ShardingSphereUser) a, (Object[]) b), new Object[] {md5Digest, md5Salt}, getRules(databaseName))) {
            return new PostgreSQLLoginResult(PostgreSQLErrorCode.INVALID_PASSWORD, String.format("password authentication failed for user \"%s\"", username));
        }
        return null == databaseName || SQLCheckEngine.check(databaseName, getRules(databaseName), grantee)
                ? new PostgreSQLLoginResult(PostgreSQLErrorCode.SUCCESSFUL_COMPLETION, null)
                : new PostgreSQLLoginResult(PostgreSQLErrorCode.PRIVILEGE_NOT_GRANTED, String.format("Access denied for user '%s' to database '%s'", username, databaseName));
    }

    private static boolean isPasswordRight(final ShardingSphereUser user, final Object[] args) {
        String md5Digest = (String) args[0];
        byte[] md5Salt = (byte[]) args[1];
        String expectedMd5Digest = md5Encode(user.getGrantee().getUsername(), user.getPassword(), md5Salt);
        if (!expectedMd5Digest.equals(md5Digest)) {
            return false;
        }
        return true;
    }

    private static String md5Encode(final String username, final String password, final byte[] md5Salt) {
        String passwordHash = new String(Hex.encodeHex(DigestUtils.md5(password + username), true));
        MessageDigest messageDigest = DigestUtils.getMd5Digest();
        messageDigest.update(passwordHash.getBytes());
        messageDigest.update(md5Salt);
        return "md5" + new String(Hex.encodeHex(messageDigest.digest(), true));
    }
    
    private static Collection<ShardingSphereRule> getRules(final String databaseName) {
        Collection<ShardingSphereRule> result = new LinkedList<>();
        if (!Strings.isNullOrEmpty(databaseName) && ProxyContext.getInstance().schemaExists(databaseName)) {
            result.addAll(ProxyContext.getInstance().getMetaDataContexts().getMetaData(databaseName).getRuleMetaData().getRules());
        }
        result.addAll(ProxyContext.getInstance().getMetaDataContexts().getGlobalRuleMetaData().getRules());
        return result;
    }
}
