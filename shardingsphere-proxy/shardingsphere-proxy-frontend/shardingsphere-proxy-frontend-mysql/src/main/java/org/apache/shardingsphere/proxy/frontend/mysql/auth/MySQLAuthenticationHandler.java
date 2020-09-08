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

package org.apache.shardingsphere.proxy.frontend.mysql.auth;

import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerErrorCode;
import org.apache.shardingsphere.db.protocol.mysql.packet.handshake.MySQLAuthPluginData;
import org.apache.shardingsphere.infra.auth.ProxyUser;
import org.apache.shardingsphere.proxy.backend.schema.ProxyContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Authentication handler for MySQL.
 */
@Getter
public final class MySQLAuthenticationHandler {
    
    private static final ProxyContext PROXY_SCHEMA_CONTEXTS = ProxyContext.getInstance();
    
    private final MySQLAuthPluginData authPluginData = new MySQLAuthPluginData();
    
    /**
     * Login.
     *
     * @param username username.
     * @param authResponse auth response
     * @param database database
     * @return login success or failure
     */
    public Optional<MySQLServerErrorCode> login(final String username, final byte[] authResponse, final String database) {
        Optional<ProxyUser> user = getUser(username);
        if (!user.isPresent() || !isPasswordRight(user.get().getPassword(), authResponse)) {
            return Optional.of(MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR);
        }
        if (!isAuthorizedSchema(user.get().getAuthorizedSchemas(), database)) {
            return Optional.of(MySQLServerErrorCode.ER_DBACCESS_DENIED_ERROR);
        }
        return Optional.empty();
    }
    
    private Optional<ProxyUser> getUser(final String username) {
        for (Entry<String, ProxyUser> entry : PROXY_SCHEMA_CONTEXTS.getSchemaContexts().getAuthentication().getUsers().entrySet()) {
            if (entry.getKey().equals(username)) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }
    
    private boolean isPasswordRight(final String password, final byte[] authResponse) {
        return Strings.isNullOrEmpty(password) || Arrays.equals(getAuthCipherBytes(password), authResponse);
    }
    
    private boolean isAuthorizedSchema(final Collection<String> authorizedSchemas, final String schema) {
        return Strings.isNullOrEmpty(schema) || CollectionUtils.isEmpty(authorizedSchemas) || authorizedSchemas.contains(schema);
    }
    
    private byte[] getAuthCipherBytes(final String password) {
        byte[] sha1Password = DigestUtils.sha1(password);
        byte[] doubleSha1Password = DigestUtils.sha1(sha1Password);
        byte[] concatBytes = new byte[authPluginData.getAuthPluginData().length + doubleSha1Password.length];
        System.arraycopy(authPluginData.getAuthPluginData(), 0, concatBytes, 0, authPluginData.getAuthPluginData().length);
        System.arraycopy(doubleSha1Password, 0, concatBytes, authPluginData.getAuthPluginData().length, doubleSha1Password.length);
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
}
