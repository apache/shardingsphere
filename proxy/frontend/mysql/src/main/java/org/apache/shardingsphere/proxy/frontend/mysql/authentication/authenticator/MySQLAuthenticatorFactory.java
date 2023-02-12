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

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLAuthenticationMethod;

import java.util.Arrays;
import java.util.Optional;

/**
 * Authenticator factory for MySQL.
 */
@RequiredArgsConstructor
public enum MySQLAuthenticatorFactory {
    
    // TODO impl OLD_PASSWORD Authenticator
    OLD_PASSWORD(MySQLAuthenticationMethod.OLD_PASSWORD, MySQLNativePasswordAuthenticator.class),
    
    NATIVE(MySQLAuthenticationMethod.NATIVE, MySQLNativePasswordAuthenticator.class, true),
    
    CLEAR_TEXT(MySQLAuthenticationMethod.CLEAR_TEXT, MySQLClearPasswordAuthenticator.class),
    
    // TODO impl WINDOWS_NATIVE Authenticator
    WINDOWS_NATIVE(MySQLAuthenticationMethod.WINDOWS_NATIVE, MySQLNativePasswordAuthenticator.class),
    
    // TODO impl SHA256 Authenticator
    SHA256(MySQLAuthenticationMethod.SHA256, MySQLNativePasswordAuthenticator.class);
    
    private final MySQLAuthenticationMethod authenticationMethod;
    
    private final Class<? extends MySQLAuthenticator> authenticatorClass;
    
    private final boolean isDefault;
    
    MySQLAuthenticatorFactory(final MySQLAuthenticationMethod authenticationMethod, final Class<? extends MySQLAuthenticator> authenticatorClass) {
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
    public static MySQLAuthenticator createAuthenticator(final String authenticationMethod, final AuthorityRule rule) {
        MySQLAuthenticatorFactory factory = findAuthenticatorFactory(getAuthenticator(authenticationMethod));
        try {
            return factory.authenticatorClass.getConstructor().newInstance();
        } catch (final NoSuchMethodException ignored) {
            return factory.authenticatorClass.getConstructor(AuthorityRule.class).newInstance(rule);
        }
    }
    
    private static MySQLAuthenticationMethod getAuthenticator(final String authenticationMethod) {
        try {
            return MySQLAuthenticationMethod.valueOf(authenticationMethod.toUpperCase());
        } catch (final IllegalArgumentException ignored) {
            return getDefaultAuthenticatorFactory().authenticationMethod;
        }
    }
    
    private static MySQLAuthenticatorFactory findAuthenticatorFactory(final MySQLAuthenticationMethod authenticationMethod) {
        Optional<MySQLAuthenticatorFactory> matchedFactory = Arrays.stream(MySQLAuthenticatorFactory.values()).filter(each -> each.authenticationMethod == authenticationMethod).findAny();
        return matchedFactory.orElseGet(MySQLAuthenticatorFactory::getDefaultAuthenticatorFactory);
    }
    
    private static MySQLAuthenticatorFactory getDefaultAuthenticatorFactory() {
        Optional<MySQLAuthenticatorFactory> defaultFactory = Arrays.stream(MySQLAuthenticatorFactory.values()).filter(each -> each.isDefault).findAny();
        return defaultFactory.orElseThrow(IllegalArgumentException::new);
    }
}
