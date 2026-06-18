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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.authentication.AuthenticatorType;
import org.apache.shardingsphere.proxy.frontend.opengauss.authentication.authenticator.impl.OpenGaussMD5PasswordAuthenticator;
import org.apache.shardingsphere.proxy.frontend.opengauss.authentication.authenticator.impl.OpenGaussSCRAMSha256PasswordAuthenticator;

/**
 * Authenticator type for openGauss.
 */
@RequiredArgsConstructor
@Getter
public enum OpenGaussAuthenticatorType implements AuthenticatorType {
    
    MD5(OpenGaussMD5PasswordAuthenticator.class),
    
    SCRAM_SHA256(OpenGaussSCRAMSha256PasswordAuthenticator.class, true);
    
    private final Class<? extends OpenGaussAuthenticator> authenticatorClass;
    
    private final boolean isDefault;
    
    OpenGaussAuthenticatorType(final Class<? extends OpenGaussAuthenticator> authenticatorClass) {
        this(authenticatorClass, false);
    }
}
