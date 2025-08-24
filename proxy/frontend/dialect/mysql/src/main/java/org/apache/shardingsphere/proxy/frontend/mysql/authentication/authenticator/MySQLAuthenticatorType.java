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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.authentication.AuthenticatorType;
import org.apache.shardingsphere.proxy.frontend.mysql.authentication.authenticator.impl.MySQLCachingSha2PasswordAuthenticator;
import org.apache.shardingsphere.proxy.frontend.mysql.authentication.authenticator.impl.MySQLClearPasswordAuthenticator;
import org.apache.shardingsphere.proxy.frontend.mysql.authentication.authenticator.impl.MySQLNativePasswordAuthenticator;

/**
 * Authenticator type for MySQL.
 */
@RequiredArgsConstructor
@Getter
public enum MySQLAuthenticatorType implements AuthenticatorType {
    
    // TODO impl OLD_PASSWORD Authenticator
    OLD_PASSWORD(MySQLNativePasswordAuthenticator.class),
    
    NATIVE(MySQLNativePasswordAuthenticator.class, true),
    
    CLEAR_TEXT(MySQLClearPasswordAuthenticator.class),
    
    // TODO impl WINDOWS_NATIVE Authenticator
    WINDOWS_NATIVE(MySQLNativePasswordAuthenticator.class),
    
    // TODO impl SHA256 Authenticator
    SHA256(MySQLNativePasswordAuthenticator.class),
    
    CACHING_SHA2_PASSWORD(MySQLCachingSha2PasswordAuthenticator.class);
    
    private final Class<? extends MySQLAuthenticator> authenticatorClass;
    
    private final boolean isDefault;
    
    MySQLAuthenticatorType(final Class<? extends MySQLAuthenticator> authenticatorClass) {
        this(authenticatorClass, false);
    }
}
