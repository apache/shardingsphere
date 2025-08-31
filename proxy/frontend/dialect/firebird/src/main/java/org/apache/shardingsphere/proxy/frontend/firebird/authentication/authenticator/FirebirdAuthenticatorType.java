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

package org.apache.shardingsphere.proxy.frontend.firebird.authentication.authenticator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.authentication.AuthenticatorType;
import org.apache.shardingsphere.proxy.frontend.firebird.authentication.authenticator.impl.FirebirdLegacyAuthenticator;
import org.apache.shardingsphere.proxy.frontend.firebird.authentication.authenticator.impl.FirebirdSRPAuthenticator;

/**
 * Authenticator type for Firebird.
 */
@RequiredArgsConstructor
@Getter
public enum FirebirdAuthenticatorType implements AuthenticatorType {
    
    // SRP authenticators uses the same auth algorithm but different hash algorithms
    SRP(FirebirdSRPAuthenticator.class),
    
    SRP224(FirebirdSRPAuthenticator.class),
    
    SRP256(FirebirdSRPAuthenticator.class, true),
    
    SRP384(FirebirdSRPAuthenticator.class),
    
    SRP512(FirebirdSRPAuthenticator.class),
    
    LEGACY_AUTH(FirebirdLegacyAuthenticator.class);
    
    private final Class<? extends FirebirdAuthenticator> authenticatorClass;
    
    private final boolean isDefault;
    
    FirebirdAuthenticatorType(final Class<? extends FirebirdAuthenticator> authenticatorClass) {
        this(authenticatorClass, false);
    }
}
