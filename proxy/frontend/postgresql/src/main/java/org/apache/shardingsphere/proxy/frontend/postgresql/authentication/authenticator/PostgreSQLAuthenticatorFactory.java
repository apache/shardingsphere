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

package org.apache.shardingsphere.proxy.frontend.postgresql.authentication.authenticator;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLAuthenticationMethod;

import java.util.Arrays;
import java.util.Optional;

/**
 * Authenticator factory for PostgreSQL.
 */
@RequiredArgsConstructor
public enum PostgreSQLAuthenticatorFactory {
    
    MD5(PostgreSQLAuthenticationMethod.MD5, PostgreSQLMD5PasswordAuthenticator.class, true),
    
    PASSWORD(PostgreSQLAuthenticationMethod.PASSWORD, PostgreSQLPasswordAuthenticator.class),
    
    // TODO impl SCRAM_SHA256 Authenticator
    SCRAM_SHA256(PostgreSQLAuthenticationMethod.SCRAM_SHA256, PostgreSQLMD5PasswordAuthenticator.class);
    
    private final PostgreSQLAuthenticationMethod authenticationMethod;
    
    private final Class<? extends PostgreSQLAuthenticator> authenticatorClass;
    
    private final boolean isDefault;
    
    PostgreSQLAuthenticatorFactory(final PostgreSQLAuthenticationMethod authenticationMethod, final Class<? extends PostgreSQLAuthenticator> authenticatorClass) {
        this(authenticationMethod, authenticatorClass, false);
    }
    
    /**
     * Create authenticator.
     * 
     * @param authenticationMethod authentication method
     * @param rule authority rule
     * @return created authenticator
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static PostgreSQLAuthenticator createAuthenticator(final String authenticationMethod, final AuthorityRule rule) {
        PostgreSQLAuthenticatorFactory factory = findAuthenticatorFactory(getAuthenticator(authenticationMethod));
        try {
            return factory.authenticatorClass.getConstructor().newInstance();
        } catch (final NoSuchMethodException ignored) {
            return factory.authenticatorClass.getConstructor(AuthorityRule.class).newInstance(rule);
        }
    }
    
    private static PostgreSQLAuthenticationMethod getAuthenticator(final String authenticationMethod) {
        try {
            return PostgreSQLAuthenticationMethod.valueOf(authenticationMethod.toUpperCase());
        } catch (final IllegalArgumentException ignored) {
            return getDefaultAuthenticatorFactory().authenticationMethod;
        }
    }
    
    private static PostgreSQLAuthenticatorFactory findAuthenticatorFactory(final PostgreSQLAuthenticationMethod authenticationMethod) {
        Optional<PostgreSQLAuthenticatorFactory> matchedFactory = Arrays.stream(PostgreSQLAuthenticatorFactory.values()).filter(each -> each.authenticationMethod == authenticationMethod).findAny();
        return matchedFactory.orElseGet(PostgreSQLAuthenticatorFactory::getDefaultAuthenticatorFactory);
    }
    
    private static PostgreSQLAuthenticatorFactory getDefaultAuthenticatorFactory() {
        Optional<PostgreSQLAuthenticatorFactory> defaultFactory = Arrays.stream(PostgreSQLAuthenticatorFactory.values()).filter(each -> each.isDefault).findAny();
        return defaultFactory.orElseThrow(IllegalArgumentException::new);
    }
}
