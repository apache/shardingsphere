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

package org.apache.shardingsphere.database.protocol.firebird.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.constant.AuthenticationMethod;

/**
 * Authentication method for Firebird.
 */
@RequiredArgsConstructor
@Getter
public enum FirebirdAuthenticationMethod implements AuthenticationMethod {
    
    SRP("Srp", "SHA-1"),
    
    SRP224("Srp224", "SHA-224"),
    
    SRP256("Srp256", "SHA-256"),
    
    SRP384("Srp384", "SHA-384"),
    
    SRP512("Srp512", "SHA-512"),
    
    LEGACY_AUTH("Legacy_Auth", null);
    
    private final String methodName;
    
    private final String hashAlgorithm;
}
