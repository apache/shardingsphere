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
import org.apache.shardingsphere.authority.checker.AuthorityChecker;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.db.protocol.postgresql.packet.handshake.PostgreSQLPasswordMessagePacket;
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.dialect.postgresql.exception.authority.InvalidPasswordException;
import org.apache.shardingsphere.dialect.postgresql.exception.authority.PrivilegeNotGrantedException;
import org.apache.shardingsphere.dialect.postgresql.exception.authority.UnknownUsernameException;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.authenticator.PostgreSQLAuthenticator;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.authenticator.PostgreSQLMD5PasswordAuthenticator;

import java.util.Optional;

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
     */
    public void login(final String username, final String databaseName, final byte[] md5Salt, final PostgreSQLPasswordMessagePacket passwordMessagePacket) {
        ShardingSpherePreconditions.checkState(Strings.isNullOrEmpty(databaseName) || ProxyContext.getInstance().databaseExists(databaseName), () -> new UnknownDatabaseException(databaseName));
        AuthorityRule authorityRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(AuthorityRule.class);
        Grantee grantee = new Grantee(username, "%");
        Optional<ShardingSphereUser> user = authorityRule.findUser(grantee);
        ShardingSpherePreconditions.checkState(user.isPresent(), () -> new UnknownUsernameException(username));
        ShardingSpherePreconditions.checkState(getAuthenticator(grantee).authenticate(user.get(), new Object[]{passwordMessagePacket.getDigest(), md5Salt}),
                () -> new InvalidPasswordException(username));
        ShardingSpherePreconditions.checkState(null == databaseName || new AuthorityChecker(authorityRule, grantee).isAuthorized(databaseName),
                () -> new PrivilegeNotGrantedException(username, databaseName));
    }
    
    /**
     * Get authenticator.
     *
     * @param grantee username
     * @return authenticator
     */
    public PostgreSQLAuthenticator getAuthenticator(final Grantee grantee) {
        // TODO get authenticator by grantee
        return new PostgreSQLMD5PasswordAuthenticator();
    }
}
