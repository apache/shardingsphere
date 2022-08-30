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
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLPasswordMessagePacket;
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.dialect.postgresql.exception.InvalidPasswordException;
import org.apache.shardingsphere.dialect.postgresql.exception.UnknownUsernameException;
import org.apache.shardingsphere.dialect.postgresql.vendor.PostgreSQLVendorError;
import org.apache.shardingsphere.infra.executor.check.SQLCheckEngine;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.authenticator.PostgreSQLAuthenticator;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.authenticator.PostgreSQLMD5PasswordAuthenticator;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Authentication handler for PostgreSQL.
 */
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
    public PostgreSQLLoginResult login(final String username, final String databaseName, final byte[] md5Salt, final PostgreSQLPasswordMessagePacket passwordMessagePacket) {
        String digest = passwordMessagePacket.getDigest();
        Grantee grantee = new Grantee(username, "%");
        if (!Strings.isNullOrEmpty(databaseName) && !ProxyContext.getInstance().databaseExists(databaseName)) {
            throw new UnknownDatabaseException(databaseName);
        }
        if (!SQLCheckEngine.check(grantee, getRules(databaseName))) {
            throw new UnknownUsernameException(username);
        }
        PostgreSQLAuthenticator authenticator = getAuthenticator(username, grantee.getHostname());
        if (!SQLCheckEngine.check(grantee, (a, b) -> authenticator.authenticate((ShardingSphereUser) a, (Object[]) b), new Object[]{digest, md5Salt}, getRules(databaseName))) {
            throw new InvalidPasswordException(username);
        }
        return null == databaseName || SQLCheckEngine.check(databaseName, getRules(databaseName), grantee)
                ? new PostgreSQLLoginResult(PostgreSQLVendorError.SUCCESSFUL_COMPLETION, null)
                : new PostgreSQLLoginResult(PostgreSQLVendorError.PRIVILEGE_NOT_GRANTED, String.format("Access denied for user '%s' to database '%s'", username, databaseName));
    }
    
    private Collection<ShardingSphereRule> getRules(final String databaseName) {
        Collection<ShardingSphereRule> result = new LinkedList<>();
        if (!Strings.isNullOrEmpty(databaseName) && ProxyContext.getInstance().databaseExists(databaseName)) {
            result.addAll(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName).getRuleMetaData().getRules());
        }
        result.addAll(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules());
        return result;
    }
    
    /**
     * Get authenticator.
     *
     * @param username username
     * @param hostname hostname
     * @return authenticator
     */
    public PostgreSQLAuthenticator getAuthenticator(final String username, final String hostname) {
        // TODO get authenticator by username and hostname
        return new PostgreSQLMD5PasswordAuthenticator();
    }
}
