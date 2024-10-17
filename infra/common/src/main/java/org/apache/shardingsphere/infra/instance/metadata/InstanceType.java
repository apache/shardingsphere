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

package org.apache.shardingsphere.infra.instance.metadata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Instance type.
 */
@RequiredArgsConstructor
@Getter
public enum InstanceType {
    
    JDBC('j'),
    
    PROXY('p');
    
    private final char code;
    
    /**
     * Value of instance type.
     *
     * @param code instance type code
     * @return instance type
     * @throws IllegalArgumentException unknown instance type code
     */
    public static InstanceType valueOf(final char code) {
        for (InstanceType each : values()) {
            if (each.code == code) {
                return each;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown instance type code: '%s'.", code));
    }
}
