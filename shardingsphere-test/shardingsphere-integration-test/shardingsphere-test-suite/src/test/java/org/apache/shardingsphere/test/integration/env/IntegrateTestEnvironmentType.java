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

package org.apache.shardingsphere.test.integration.env;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Integrate test environment type.
 */
@RequiredArgsConstructor
public enum IntegrateTestEnvironmentType {
    
    JDBC_LOCAL("jdbc-local", "integrate/env-jdbc-local.properties"),
    
    JDBC_CI("jdbc-ci", "integrate/env-jdbc-ci.properties"),
    
    PROXY("proxy", "integrate/env-proxy.properties");
    
    private final String profileName;
    
    @Getter
    private final String envFileName;
    
    /**
     * Get enum value from profile name.
     * 
     * @param profileName profile name
     * @return enum value
     */
    public static IntegrateTestEnvironmentType valueFromProfileName(final String profileName) {
        for (IntegrateTestEnvironmentType each : values()) {
            if (each.profileName.equals(profileName)) {
                return each;
            }
        }
        return JDBC_LOCAL;
    }
}
