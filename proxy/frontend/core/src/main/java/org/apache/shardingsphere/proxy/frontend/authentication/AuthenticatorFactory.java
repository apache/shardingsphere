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

package org.apache.shardingsphere.proxy.frontend.authentication;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.authority.rule.AuthorityRule;

import java.util.Arrays;

/**
 * Authenticator factory.
 */
@RequiredArgsConstructor
public abstract class AuthenticatorFactory<E extends Enum<E> & AuthenticatorType> {
    
    private final Class<E> enumClass;
    
    /**
     * Create new instance of authenticator.
     * 
     * @param authenticationMethod authentication method
     * @param rule authority rule
     * @return new instance of authenticator
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public Authenticator newInstance(final String authenticationMethod, final AuthorityRule rule) {
        E authenticatorType = getAuthenticatorType(authenticationMethod);
        try {
            return authenticatorType.getAuthenticatorClass().getConstructor().newInstance();
        } catch (final NoSuchMethodException ignored) {
            return authenticatorType.getAuthenticatorClass().getConstructor(AuthorityRule.class).newInstance(rule);
        }
    }
    
    private E getAuthenticatorType(final String authenticationMethod) {
        try {
            return E.valueOf(enumClass, authenticationMethod.toUpperCase());
        } catch (final IllegalArgumentException ignored) {
            return Arrays.stream(enumClass.getEnumConstants()).filter(AuthenticatorType::isDefault).findAny().orElseThrow(IllegalArgumentException::new);
        }
    }
}
