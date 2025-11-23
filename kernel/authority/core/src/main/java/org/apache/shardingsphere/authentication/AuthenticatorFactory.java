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

package org.apache.shardingsphere.authentication;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;

import java.util.Arrays;

/**
 * Authenticator factory.
 * 
 * @param <E> type of enum
 */
@RequiredArgsConstructor
public final class AuthenticatorFactory<E extends Enum<E> & AuthenticatorType> {
    
    private final Class<E> authenticatorTypeClass;
    
    private final AuthorityRule rule;
    
    /**
     * Create new instance of authenticator.
     *
     * @param user user
     * @return new instance of authenticator
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public Authenticator newInstance(final ShardingSphereUser user) {
        E authenticatorType = getAuthenticatorType(rule.getAuthenticatorType(user));
        return authenticatorType.getAuthenticatorClass().getConstructor().newInstance();
    }
    
    private E getAuthenticatorType(final String authenticationMethodName) {
        try {
            return Enum.valueOf(authenticatorTypeClass, authenticationMethodName.toUpperCase());
        } catch (final IllegalArgumentException ignored) {
            return Arrays.stream(authenticatorTypeClass.getEnumConstants()).filter(AuthenticatorType::isDefault).findAny().orElseThrow(IllegalArgumentException::new);
        }
    }
}
