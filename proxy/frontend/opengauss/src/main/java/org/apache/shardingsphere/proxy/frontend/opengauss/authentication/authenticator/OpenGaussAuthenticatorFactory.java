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

package org.apache.shardingsphere.proxy.frontend.opengauss.authentication.authenticator;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.db.protocol.opengauss.constant.OpenGaussAuthenticationMethod;

import java.util.Arrays;
import java.util.Optional;

/**
 * Authenticator factory for openGauss.
 */
@RequiredArgsConstructor
public enum OpenGaussAuthenticatorFactory {
    
    SCRAM_SHA256(OpenGaussAuthenticationMethod.SCRAM_SHA256, OpenGaussSCRAMSha256PasswordAuthenticator.class, true);
    
    private final OpenGaussAuthenticationMethod authenticationMethod;
    
    private final Class<? extends OpenGaussAuthenticator> authenticatorClass;
    
    private final boolean isDefault;
    
    /**
     * Create authenticator.
     * 
     * @param authenticationMethod authentication method
     * @param rule authority rule
     * @return created authenticator
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static OpenGaussAuthenticator createAuthenticator(final String authenticationMethod, final AuthorityRule rule) {
        OpenGaussAuthenticatorFactory factory = findAuthenticatorFactory(getAuthenticator(authenticationMethod));
        try {
            return factory.authenticatorClass.getConstructor().newInstance();
        } catch (final NoSuchMethodException ignored) {
            return factory.authenticatorClass.getConstructor(AuthorityRule.class).newInstance(rule);
        }
    }
    
    private static OpenGaussAuthenticationMethod getAuthenticator(final String authenticationMethod) {
        try {
            return OpenGaussAuthenticationMethod.valueOf(authenticationMethod.toUpperCase());
        } catch (final IllegalArgumentException ignored) {
            return getDefaultAuthenticatorFactory().authenticationMethod;
        }
    }
    
    private static OpenGaussAuthenticatorFactory findAuthenticatorFactory(final OpenGaussAuthenticationMethod authenticationMethod) {
        Optional<OpenGaussAuthenticatorFactory> matchedFactory = Arrays.stream(OpenGaussAuthenticatorFactory.values()).filter(each -> each.authenticationMethod == authenticationMethod).findAny();
        return matchedFactory.orElseGet(OpenGaussAuthenticatorFactory::getDefaultAuthenticatorFactory);
    }
    
    private static OpenGaussAuthenticatorFactory getDefaultAuthenticatorFactory() {
        Optional<OpenGaussAuthenticatorFactory> defaultFactory = Arrays.stream(OpenGaussAuthenticatorFactory.values()).filter(each -> each.isDefault).findAny();
        return defaultFactory.orElseThrow(IllegalArgumentException::new);
    }
}
