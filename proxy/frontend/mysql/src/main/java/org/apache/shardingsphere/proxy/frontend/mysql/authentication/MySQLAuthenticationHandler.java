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

package org.apache.shardingsphere.proxy.frontend.mysql.authentication;

import lombok.Getter;
import org.apache.shardingsphere.authority.checker.AuthorityChecker;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLAuthPluginData;
import org.apache.shardingsphere.dialect.mysql.vendor.MySQLVendorError;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.mysql.authentication.authenticator.MySQLAuthenticator;
import org.apache.shardingsphere.proxy.frontend.mysql.authentication.authenticator.MySQLNativePasswordAuthenticator;

import java.util.Optional;

/**
 * Authentication handler for MySQL.
 */
@Getter
public final class MySQLAuthenticationHandler {
    
    private final MySQLAuthPluginData authPluginData = new MySQLAuthPluginData();
    
    /**
     * Login.
     *
     * @param username username
     * @param hostname hostname
     * @param authenticationResponse authentication response
     * @param databaseName database name
     * @return login success or failure
     */
    public Optional<MySQLVendorError> login(final String username, final String hostname, final byte[] authenticationResponse, final String databaseName) {
        AuthorityRule authorityRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(AuthorityRule.class);
        Grantee grantee = new Grantee(username, hostname);
        Optional<ShardingSphereUser> user = authorityRule.findUser(grantee);
        if (!user.isPresent() || !getAuthenticator(grantee).authenticate(user.get(), authenticationResponse)) {
            return Optional.of(MySQLVendorError.ER_ACCESS_DENIED_ERROR);
        }
        return null == databaseName || new AuthorityChecker(authorityRule, grantee).isAuthorized(databaseName) ? Optional.empty() : Optional.of(MySQLVendorError.ER_DBACCESS_DENIED_ERROR);
    }
    
    /**
     * Get authenticator.
     *
     * @param grantee grantee
     * @return authenticator
     */
    public MySQLAuthenticator getAuthenticator(final Grantee grantee) {
        // TODO get authenticator by grantee
        return new MySQLNativePasswordAuthenticator(authPluginData);
    }
}
